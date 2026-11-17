import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, formatPrice } from "../api";
import type { Cart, Order } from "../types";
import ErrorMessage from "../components/ErrorMessage";

export default function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api<Cart>("/api/cart").then(setCart).catch((e) => setError(e.message));
  }, []);

  const run = async (action: () => Promise<Cart>) => {
    setError(null);
    setBusy(true);
    try {
      setCart(await action());
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };

  const changeQuantity = (productId: number, quantity: number) =>
    run(() => api<Cart>(`/api/cart/items/${productId}`, { method: "PATCH", body: JSON.stringify({ quantity }) }));

  const remove = (productId: number) => run(() => api<Cart>(`/api/cart/items/${productId}`, { method: "DELETE" }));

  const checkout = async () => {
    setError(null);
    setBusy(true);
    try {
      const order = await api<Order>("/api/orders", { method: "POST" });
      navigate(`/pedidos/${order.id}`);
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  };

  if (!cart) return error ? <ErrorMessage message={error} /> : <p className="muted">Carregando...</p>;

  if (cart.items.length === 0) {
    return (
      <>
        <h1>Carrinho</h1>
        <p className="muted">Seu carrinho está vazio. <Link to="/">Ver produtos</Link></p>
      </>
    );
  }

  return (
    <>
      <h1>Carrinho</h1>
      <table className="table">
        <thead>
          <tr><th>Produto</th><th>Preço</th><th>Quantidade</th><th>Subtotal</th><th /></tr>
        </thead>
        <tbody>
          {cart.items.map((item) => (
            <tr key={item.productId}>
              <td><Link to={`/produtos/${item.productId}`}>{item.name}</Link></td>
              <td>{formatPrice(item.unitPrice)}</td>
              <td>
                <input type="number" min={1} max={item.availableStock} value={item.quantity} disabled={busy}
                       onChange={(e) => changeQuantity(item.productId, Number(e.target.value))} />
              </td>
              <td>{formatPrice(item.subtotal)}</td>
              <td><button className="link-button" disabled={busy} onClick={() => remove(item.productId)}>Remover</button></td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="total">Total: {formatPrice(cart.total)}</p>
      <ErrorMessage message={error} />
      <button disabled={busy} onClick={checkout}>Finalizar pedido</button>
    </>
  );
}
