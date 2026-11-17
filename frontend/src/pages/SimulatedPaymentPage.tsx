import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import ErrorMessage from "../components/ErrorMessage";

/** Tela usada quando a API está sem Mercado Pago configurado (gateway simulado). */
export default function SimulatedPaymentPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const approve = async () => {
    setBusy(true);
    try {
      await api<void>(`/api/orders/${id}/payment/simulate-approval`, { method: "POST" });
      navigate(`/pedidos/${id}`);
    } catch (e) {
      setError((e as Error).message);
      setBusy(false);
    }
  };

  return (
    <>
      <h1>Pagamento simulado</h1>
      <p>
        A API está rodando sem credenciais do Mercado Pago, então o pagamento do pedido #{id} é aprovado
        manualmente aqui. Nenhuma cobrança real é feita.
      </p>
      <ErrorMessage message={error} />
      <div className="row">
        <button disabled={busy} onClick={approve}>Aprovar pagamento</button>
        <button className="secondary" onClick={() => navigate(`/pedidos/${id}`)}>Voltar</button>
      </div>
    </>
  );
}
