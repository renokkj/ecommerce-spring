export type Role = "CUSTOMER" | "ADMIN";

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInMinutes: number;
  user: User;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Category {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stock: number;
  imageUrl: string | null;
  active: boolean;
  categoryId: number;
  categoryName: string;
}

export interface CartItem {
  productId: number;
  name: string;
  imageUrl: string | null;
  unitPrice: number;
  quantity: number;
  availableStock: number;
  subtotal: number;
}

export interface Cart {
  items: CartItem[];
  totalItems: number;
  total: number;
}

export type OrderStatus = "PENDING" | "PAID" | "SHIPPED" | "DELIVERED" | "CANCELED";

export interface OrderItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  status: OrderStatus;
  total: number;
  items: OrderItem[];
  customerEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface CheckoutSession {
  checkoutUrl: string;
  provider: string;
}
