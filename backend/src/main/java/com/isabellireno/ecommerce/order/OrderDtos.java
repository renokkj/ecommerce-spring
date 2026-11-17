package com.isabellireno.ecommerce.order;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity,
                                    BigDecimal subtotal) {
        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProduct().getId(), item.getProductName(), item.getUnitPrice(),
                    item.getQuantity(), item.getSubtotal());
        }
    }

    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, List<OrderItemResponse> items,
                                String customerEmail, Instant createdAt, Instant updatedAt) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(),
                    order.getItems().stream().map(OrderItemResponse::from).toList(),
                    order.getUser().getEmail(), order.getCreatedAt(), order.getUpdatedAt());
        }
    }

    public record UpdateStatusRequest(@NotNull(message = "Informe o status.") OrderStatus status) {}
}
