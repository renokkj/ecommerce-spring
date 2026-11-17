import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { api, formatPrice } from "../../api";
import type { Category, Page, Product } from "../../types";
import Pagination from "../../components/Pagination";
import ErrorMessage from "../../components/ErrorMessage";

interface FormState {
  name: string;
  description: string;
  price: string;
  stock: string;
  categoryId: string;
  active: boolean;
}

const EMPTY: FormState = { name: "", description: "", price: "", stock: "", categoryId: "", active: true };

export default function AdminProductsPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [data, setData] = useState<Page<Product> | null>(null);
  const [page, setPage] = useState(0);
  const [form, setForm] = useState<FormState>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    api<Page<Product>>(`/api/admin/products?page=${page}`).then(setData).catch((e) => setError(e.message));
  }, [page]);

  useEffect(() => {
    api<Category[]>("/api/categories").then(setCategories).catch((e) => setError(e.message));
  }, []);

  useEffect(load, [load]);

  const edit = (p: Product) => {
    setEditingId(p.id);
    setForm({
      name: p.name,
      description: p.description ?? "",
      price: String(p.price),
      stock: String(p.stock),
      categoryId: String(p.categoryId),
      active: p.active,
    });
    setImage(null);
  };

  const reset = () => {
    setEditingId(null);
    setForm(EMPTY);
    setImage(null);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const body = JSON.stringify({
      name: form.name,
      description: form.description || null,
      price: Number(form.price),
      stock: Number(form.stock),
      categoryId: Number(form.categoryId),
      active: form.active,
    });
    try {
      const saved = editingId
        ? await api<Product>(`/api/admin/products/${editingId}`, { method: "PUT", body })
        : await api<Product>("/api/admin/products", { method: "POST", body });
      if (image) {
        const data = new FormData();
        data.append("file", image);
        await api<Product>(`/api/admin/products/${saved.id}/image`, { method: "POST", body: data });
      }
      reset();
      load();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  };

  const deactivate = async (id: number) => {
    setError(null);
    try {
      await api<void>(`/api/admin/products/${id}`, { method: "DELETE" });
      load();
    } catch (err) {
      setError((err as Error).message);
    }
  };

  return (
    <>
      <div className="row space-between">
        <h1>Produtos (admin)</h1>
        <Link to="/admin/pedidos">Ver pedidos</Link>
      </div>

      <form className="form wide" onSubmit={submit}>
        <h2>{editingId ? `Editar produto #${editingId}` : "Novo produto"}</h2>
        <label>Nome<input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>Descrição
          <textarea rows={3} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </label>
        <div className="row">
          <label>Preço<input type="number" min="0" step="0.01" required value={form.price}
                             onChange={(e) => setForm({ ...form, price: e.target.value })} /></label>
          <label>Estoque<input type="number" min="0" required value={form.stock}
                               onChange={(e) => setForm({ ...form, stock: e.target.value })} /></label>
          <label>Categoria
            <select required value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">Selecione</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </label>
        </div>
        <label>Imagem (JPEG, PNG ou WebP, até 5 MB)
          <input type="file" accept="image/jpeg,image/png,image/webp"
                 onChange={(e) => setImage(e.target.files?.[0] ?? null)} />
        </label>
        <label className="checkbox">
          <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
          Ativo no catálogo
        </label>
        <ErrorMessage message={error} />
        <div className="row">
          <button type="submit" disabled={busy}>{editingId ? "Salvar alterações" : "Cadastrar"}</button>
          {editingId && <button type="button" className="secondary" onClick={reset}>Cancelar</button>}
        </div>
      </form>

      <table className="table">
        <thead>
          <tr><th>#</th><th>Nome</th><th>Categoria</th><th>Preço</th><th>Estoque</th><th>Ativo</th><th /></tr>
        </thead>
        <tbody>
          {data?.content.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.categoryName}</td>
              <td>{formatPrice(p.price)}</td>
              <td>{p.stock}</td>
              <td>{p.active ? "Sim" : "Não"}</td>
              <td className="actions">
                <button className="link-button" onClick={() => edit(p)}>Editar</button>
                {p.active && <button className="link-button" onClick={() => deactivate(p.id)}>Desativar</button>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  );
}
