package com.isabellireno.ecommerce.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void pendingCanBePaidOrCanceled() {
        assertThat(OrderStatus.PENDING.canChangeTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PENDING.canChangeTo(OrderStatus.CANCELED)).isTrue();
        assertThat(OrderStatus.PENDING.canChangeTo(OrderStatus.SHIPPED)).isFalse();
    }

    @Test
    void shippedOnlyGoesToDelivered() {
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canChangeTo(OrderStatus.CANCELED)).isFalse();
    }

    @Test
    void finalStatusesCannotChange() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(OrderStatus.DELIVERED.canChangeTo(next)).isFalse();
            assertThat(OrderStatus.CANCELED.canChangeTo(next)).isFalse();
        }
    }
}
