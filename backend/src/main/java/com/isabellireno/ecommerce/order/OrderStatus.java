package com.isabellireno.ecommerce.order;

import java.util.Set;

/**
 * Ciclo de vida do pedido. Cada status define para quais outros ele pode ir,
 * o que impede, por exemplo, enviar um pedido que não foi pago.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELED;

    public boolean canChangeTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> Set.of(PAID, CANCELED).contains(next);
            case PAID -> Set.of(SHIPPED, CANCELED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELED -> false;
        };
    }
}
