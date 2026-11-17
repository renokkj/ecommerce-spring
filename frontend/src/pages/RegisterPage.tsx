import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiError } from "../api";
import { useAuth } from "../auth";
import ErrorMessage from "../components/ErrorMessage";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [fields, setFields] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setFields({});
    try {
      await register(name, email, password);
      navigate("/", { replace: true });
    } catch (err) {
      setError((err as Error).message);
      if (err instanceof ApiError) setFields(err.fields);
      setBusy(false);
    }
  };

  return (
    <form className="form" onSubmit={submit}>
      <h1>Criar conta</h1>
      <label>Nome<input required value={name} onChange={(e) => setName(e.target.value)} /></label>
      {fields.name && <span className="field-error">{fields.name}</span>}
      <label>E-mail<input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} /></label>
      {fields.email && <span className="field-error">{fields.email}</span>}
      <label>
        Senha (mínimo 8 caracteres)
        <input type="password" required minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} />
      </label>
      {fields.password && <span className="field-error">{fields.password}</span>}
      <ErrorMessage message={error} />
      <button type="submit" disabled={busy}>Criar conta</button>
      <p className="muted">Já tem conta? <Link to="/login">Entrar</Link></p>
    </form>
  );
}
