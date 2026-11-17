import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";
import ErrorMessage from "../components/ErrorMessage";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? "/";
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError((err as Error).message);
      setBusy(false);
    }
  };

  return (
    <form className="form" onSubmit={submit}>
      <h1>Entrar</h1>
      <label>E-mail<input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} /></label>
      <label>Senha<input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} /></label>
      <ErrorMessage message={error} />
      <button type="submit" disabled={busy}>Entrar</button>
      <p className="muted">Não tem conta? <Link to="/cadastro">Cadastre-se</Link></p>
    </form>
  );
}
