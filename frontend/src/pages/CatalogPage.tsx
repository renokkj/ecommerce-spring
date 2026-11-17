import { useEffect, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api, formatPrice } from "../api";
import type { Category, Page, Product } from "../types";
import Pagination from "../components/Pagination";
import ErrorMessage from "../components/ErrorMessage";

const SORT_OPTIONS = [
  { value: "name:asc", label: "Nome (A-Z)" },
  { value: "price:asc", label: "Menor preço" },
  { value: "price:desc", label: "Maior preço" },
  { value: "createdAt:desc", label: "Mais recentes" },
];

export default function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const [categories, setCategories] = useState<Category[]>([]);
  const [data, setData] = useState<Page<Product> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [q, setQ] = useState(params.get("q") ?? "");
  const [minPrice, setMinPrice] = useState(params.get("minPrice") ?? "");
  const [maxPrice, setMaxPrice] = useState(params.get("maxPrice") ?? "");

  useEffect(() => {
    api<Category[]>("/api/categories").then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    const query = new URLSearchParams(params);
    const [sort, direction] = (params.get("sort") ?? "name:asc").split(":");
    query.set("sort", sort);
    query.set("direction", direction);
    query.set("size", "12");
    setLoading(true);
    setError(null);
    api<Page<Product>>(`/api/products?${query}`)
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [params]);

  const update = (changes: Record<string, string>) => {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([key, value]) => (value ? next.set(key, value) : next.delete(key)));
    if (!("page" in changes)) next.delete("page");
    setParams(next);
  };

  const onSearch = (e: FormEvent) => {
    e.preventDefault();
    update({ q, minPrice, maxPrice });
  };

  return (
    <>
      <h1>Produtos</h1>

      <form className="filters" onSubmit={onSearch}>
        <input placeholder="Buscar por nome" value={q} onChange={(e) => setQ(e.target.value)} />
        <select value={params.get("categoryId") ?? ""} onChange={(e) => update({ categoryId: e.target.value })}>
          <option value="">Todas as categorias</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <input type="number" min="0" step="0.01" placeholder="Preço mín." value={minPrice}
               onChange={(e) => setMinPrice(e.target.value)} />
        <input type="number" min="0" step="0.01" placeholder="Preço máx." value={maxPrice}
               onChange={(e) => setMaxPrice(e.target.value)} />
        <select value={params.get("sort") ?? "name:asc"} onChange={(e) => update({ sort: e.target.value })}>
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
        <label className="checkbox">
          <input type="checkbox" checked={params.get("inStock") === "true"}
                 onChange={(e) => update({ inStock: e.target.checked ? "true" : "" })} />
          Só com estoque
        </label>
        <button type="submit">Filtrar</button>
      </form>

      <ErrorMessage message={error} />
      {loading && <p className="muted">Carregando...</p>}
      {!loading && data?.content.length === 0 && <p className="muted">Nenhum produto encontrado.</p>}

      <div className="grid">
        {data?.content.map((p) => (
          <Link key={p.id} to={`/produtos/${p.id}`} className="card">
            {p.imageUrl ? <img src={p.imageUrl} alt={p.name} /> : <div className="no-image">Sem imagem</div>}
            <div className="card-body">
              <span className="muted small">{p.categoryName}</span>
              <strong>{p.name}</strong>
              <span>{formatPrice(p.price)}</span>
              {p.stock === 0 && <span className="badge">Esgotado</span>}
            </div>
          </Link>
        ))}
      </div>

      {data && (
        <Pagination page={data.page} totalPages={data.totalPages}
                    onChange={(page) => update({ page: String(page) })} />
      )}
    </>
  );
}
