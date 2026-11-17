import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, formatPrice, statusLabel } from "../api";
import type { CheckoutSession, Order } from "../types";
import ErrorMessage from "../components/ErrorMessage";

export default function OrderPage() {
  const { id } = useParams();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api<Order>(`/api/orders/${id}`).then(setOrder).catch((e) => setError(e.message));
  }, [id]);

  const pay = async () => {
    setBusy(true);
    setError(null);
    try {
      const session = await api<CheckoutSession>(`/api/orders/${id}/payment`, { method: "POST" });
      window.location.href = session.checkoutUrl;
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  };

  const cancel = async () => {
    setBusy(true);
    setError(null);
    try {
      setOrder(await api<Order>(`/api/orders/${id}/cancel`, { method: "POST" }));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };

  if (!order) return error ? <ErrorMessage message={error} /> : <p className="muted">Carregando...</p>;

  return (
    <>
      <h1>Pedido #{order.id}</h1>
      <p>
        <span className={`status status-${order.status}`}>{statusLabel[order.status]}</span>{" "}
        <span className="muted">criado em {new Date(order.createdAt).toLocaleString("pt-BR")}</span>
      </p>
      <table className="table">
        <thead>
          <tr><th>Produto</th><th>Preço</th><th>Qtd.</th><th>Subtotal</th></tr>
        </thead>
        <tbody>
          {order.items.map((item) => (
            <tr key={item.productId}>
              <td>{item.productName}</td>
              <td>{formatPrice(item.unitPrice)}</td>
              <td>{item.quantity}</td>
              <td>{formatPrice(item.subtotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <p className="total">Total: {formatPrice(order.total)}</p>
      <ErrorMessage message={error} />
      {order.status === "PENDING" && (
        <div className="row">
          <button disabled={busy} onClick={pay}>Pagar</button>
          <button className="secondary" disabled={busy} onClick={cancel}>Cancelar pedido</button>
        </div>
      )}
    </>
  );
}
