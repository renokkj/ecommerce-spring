package com.isabellireno.ecommerce.cart;

import com.isabellireno.ecommerce.cart.CartDtos.CartResponse;
import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.common.NotFoundException;
import com.isabellireno.ecommerce.product.Product;
import com.isabellireno.ecommerce.product.ProductRepository;
import com.isabellireno.ecommerce.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse get(User user) {
        return CartResponse.from(getOrCreate(user));
    }

    @Transactional
    public CartResponse addItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreate(user);
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado."));
        int current = cart.findItem(productId).map(CartItem::getQuantity).orElse(0);
        ensureStock(product, current + quantity);
        cart.addItem(product, quantity);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse updateQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreate(user);
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new NotFoundException("Produto não está no carrinho."));
        ensureStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(User user, Long productId) {
        Cart cart = getOrCreate(user);
        cart.removeItem(productId);
        return CartResponse.from(cart);
    }

    @Transactional
    public void clear(User user) {
        getOrCreate(user).clear();
    }

    /** Usado pelo checkout dentro da mesma transação. */
    public Cart getOrCreate(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private void ensureStock(Product product, int wanted) {
        if (wanted > product.getStock()) {
            throw new BusinessException("Estoque insuficiente para \"" + product.getName()
                    + "\". Disponível: " + product.getStock() + ".");
        }
    }
}
