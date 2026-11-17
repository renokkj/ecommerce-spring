import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, formatPrice, statusLabel } from "../api";
import type { Order, Page } from "../types";
import Pagination from "../components/Pagination";
import ErrorMessage from "../components/ErrorMessage";

export default function OrdersPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<Order> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api<Page<Order>>(`/api/orders?page=${page}`).then(setData).catch((e) => setError(e.message));
  }, [page]);

  return (
    <>
      <h1>Meus pedidos</h1>
      <ErrorMessage message={error} />
      {data?.content.length === 0 && <p className="muted">Você ainda não fez pedidos.</p>}
      {data && data.content.length > 0 && (
        <table className="table">
          <thead>
            <tr><th>Pedido</th><th>Data</th><th>Status</th><th>Total</th></tr>
          </thead>
          <tbody>
            {data.content.map((o) => (
              <tr key={o.id}>
                <td><Link to={`/pedidos/${o.id}`}>#{o.id}</Link></td>
                <td>{new Date(o.createdAt).toLocaleString("pt-BR")}</td>
                <td><span className={`status status-${o.status}`}>{statusLabel[o.status]}</span></td>
                <td>{formatPrice(o.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  );
}
