import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, formatPrice } from "../api";
import { useAuth } from "../auth";
import type { Cart, Product } from "../types";
import ErrorMessage from "../components/ErrorMessage";

export default function ProductPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    api<Product>(`/api/products/${id}`).then(setProduct).catch((e) => setError(e.message));
  }, [id]);

  const addToCart = async () => {
    if (!user) {
      navigate("/login", { state: { from: `/produtos/${id}` } });
      return;
    }
    setError(null);
    setMessage(null);
    try {
      await api<Cart>("/api/cart/items", {
        method: "POST",
        body: JSON.stringify({ productId: Number(id), quantity }),
      });
      setMessage("Produto adicionado ao carrinho.");
    } catch (e) {
      setError((e as Error).message);
    }
  };

  if (!product) return error ? <ErrorMessage message={error} /> : <p className="muted">Carregando...</p>;

  return (
    <div className="product-detail">
      {product.imageUrl ? <img src={product.imageUrl} alt={product.name} /> : <div className="no-image large">Sem imagem</div>}
      <div>
        <span className="muted">{product.categoryName}</span>
        <h1>{product.name}</h1>
        <p className="price">{formatPrice(product.price)}</p>
        <p>{product.description}</p>
        <p className="muted">{product.stock > 0 ? `${product.stock} em estoque` : "Esgotado"}</p>
        {product.stock > 0 && (
          <div className="row">
            <input type="number" min={1} max={product.stock} value={quantity}
                   onChange={(e) => setQuantity(Math.max(1, Math.min(product.stock, Number(e.target.value))))} />
            <button onClick={addToCart}>Adicionar ao carrinho</button>
          </div>
        )}
        <ErrorMessage message={error} />
        {message && <p className="success">{message}</p>}
      </div>
    </div>
  );
}
