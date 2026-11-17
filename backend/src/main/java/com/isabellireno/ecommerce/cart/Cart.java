package com.isabellireno.ecommerce.cart;

import com.isabellireno.ecommerce.product.Product;
import com.isabellireno.ecommerce.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(User user) {
        this.user = user;
    }

    public Optional<CartItem> findItem(Long productId) {
        return items.stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst();
    }

    /** Adiciona o produto ou soma a quantidade se ele já estiver no carrinho. */
    public CartItem addItem(Product product, int quantity) {
        CartItem item = findItem(product.getId()).orElse(null);
        if (item == null) {
            item = new CartItem(this, product, quantity);
            items.add(item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }
        return item;
    }

    public void removeItem(Long productId) {
        items.removeIf(i -> i.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotal() {
        return items.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
