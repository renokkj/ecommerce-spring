package com.isabellireno.ecommerce.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddItemRequest(
            @NotNull(message = "Informe o produto.") Long productId,
            @NotNull @Min(value = 1, message = "Quantidade mínima é 1.") @Max(99) Integer quantity
    ) {}

    public record UpdateQuantityRequest(
            @NotNull @Min(value = 1, message = "Quantidade mínima é 1.") @Max(99) Integer quantity
    ) {}

    public record CartItemResponse(
            Long productId,
            String name,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            int availableStock,
            BigDecimal subtotal
    ) {
        static CartItemResponse from(CartItem item) {
            var p = item.getProduct();
            return new CartItemResponse(p.getId(), p.getName(), p.getImageUrl(), p.getPrice(),
                    item.getQuantity(), p.getStock(), item.getSubtotal());
        }
    }

    public record CartResponse(List<CartItemResponse> items, int totalItems, BigDecimal total) {
        static CartResponse from(Cart cart) {
            List<CartItemResponse> items = cart.getItems().stream().map(CartItemResponse::from).toList();
            int count = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
            return new CartResponse(items, count, cart.getTotal());
        }
    }
}
