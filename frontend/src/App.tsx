import type { ReactNode } from "react";
import { Link, Navigate, NavLink, Route, Routes, useLocation } from "react-router-dom";
import { useAuth } from "./auth";
import CatalogPage from "./pages/CatalogPage";
import ProductPage from "./pages/ProductPage";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import OrderPage from "./pages/OrderPage";
import SimulatedPaymentPage from "./pages/SimulatedPaymentPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AdminProductsPage from "./pages/admin/AdminProductsPage";
import AdminOrdersPage from "./pages/admin/AdminOrdersPage";

function RequireAuth({ children, admin = false }: { children: ReactNode; admin?: boolean }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <p className="muted">Carregando...</p>;
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (admin && user.role !== "ADMIN") return <Navigate to="/" replace />;
  return <>{children}</>;
}

export default function App() {
  const { user, logout } = useAuth();

  return (
    <>
      <header className="header">
        <div className="container header-inner">
          <Link to="/" className="brand">Loja</Link>
          <nav className="nav">
            <NavLink to="/">Produtos</NavLink>
            {user && <NavLink to="/carrinho">Carrinho</NavLink>}
            {user && <NavLink to="/pedidos">Meus pedidos</NavLink>}
            {user?.role === "ADMIN" && <NavLink to="/admin/produtos">Admin</NavLink>}
            {user ? (
              <button className="link-button" onClick={logout}>Sair ({user.name})</button>
            ) : (
              <NavLink to="/login">Entrar</NavLink>
            )}
          </nav>
        </div>
      </header>

      <main className="container main">
        <Routes>
          <Route path="/" element={<CatalogPage />} />
          <Route path="/produtos/:id" element={<ProductPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/cadastro" element={<RegisterPage />} />
          <Route path="/carrinho" element={<RequireAuth><CartPage /></RequireAuth>} />
          <Route path="/pedidos" element={<RequireAuth><OrdersPage /></RequireAuth>} />
          <Route path="/pedidos/:id" element={<RequireAuth><OrderPage /></RequireAuth>} />
          <Route path="/pagamento-simulado/:id" element={<RequireAuth><SimulatedPaymentPage /></RequireAuth>} />
          <Route path="/admin/produtos" element={<RequireAuth admin><AdminProductsPage /></RequireAuth>} />
          <Route path="/admin/pedidos" element={<RequireAuth admin><AdminOrdersPage /></RequireAuth>} />
          <Route path="*" element={<p>Página não encontrada.</p>} />
        </Routes>
      </main>
    </>
  );
}
