import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, formatPrice, statusLabel } from "../../api";
import type { Order, OrderStatus, Page } from "../../types";
import Pagination from "../../components/Pagination";
import ErrorMessage from "../../components/ErrorMessage";

const NEXT_STATUS: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ["CANCELED"],
  PAID: ["SHIPPED", "CANCELED"],
  SHIPPED: ["DELIVERED"],
  DELIVERED: [],
  CANCELED: [],
};

export default function AdminOrdersPage() {
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<Order> | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    const query = new URLSearchParams({ page: String(page) });
    if (status) query.set("status", status);
    api<Page<Order>>(`/api/admin/orders?${query}`).then(setData).catch((e) => setError(e.message));
  }, [page, status]);

  useEffect(load, [load]);

  const changeStatus = async (id: number, next: OrderStatus) => {
    setError(null);
    try {
      await api<Order>(`/api/admin/orders/${id}/status`, { method: "PATCH", body: JSON.stringify({ status: next }) });
      load();
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <>
      <div className="row space-between">
        <h1>Pedidos (admin)</h1>
        <Link to="/admin/produtos">Ver produtos</Link>
      </div>
      <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
        <option value="">Todos os status</option>
        {Object.entries(statusLabel).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
      </select>
      <ErrorMessage message={error} />
      <table className="table">
        <thead>
          <tr><th>#</th><th>Cliente</th><th>Data</th><th>Total</th><th>Status</th><th>Ações</th></tr>
        </thead>
        <tbody>
          {data?.content.map((o) => (
            <tr key={o.id}>
              <td>{o.id}</td>
              <td>{o.customerEmail}</td>
              <td>{new Date(o.createdAt).toLocaleString("pt-BR")}</td>
              <td>{formatPrice(o.total)}</td>
              <td><span className={`status status-${o.status}`}>{statusLabel[o.status]}</span></td>
              <td className="actions">
                {NEXT_STATUS[o.status].map((next) => (
                  <button key={next} className="link-button" onClick={() => changeStatus(o.id, next)}>
                    {statusLabel[next]}
                  </button>
                ))}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </>
  );
}
