package com.isabellireno.ecommerce.order;

import com.isabellireno.ecommerce.TestData;
import com.isabellireno.ecommerce.cart.Cart;
import com.isabellireno.ecommerce.cart.CartService;
import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.order.OrderDtos.OrderResponse;
import com.isabellireno.ecommerce.product.Product;
import com.isabellireno.ecommerce.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = TestData.user(1);
        cart = new Cart(user);
    }

    @Test
    void checkoutCreatesOrderDecreasesStockAndClearsCart() {
        Product book = TestData.product(10, "50.00", 5);
        Product pen = TestData.product(11, "2.50", 100);
        cart.addItem(book, 2);
        cart.addItem(pen, 4);
        when(cartService.getOrCreate(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.checkout(user);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("110.00"));
        assertThat(response.items()).hasSize(2);
        assertThat(book.getStock()).isEqualTo(3);
        assertThat(pen.getStock()).isEqualTo(96);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void checkoutKeepsPriceFromTheMomentOfPurchase() {
        Product book = TestData.product(10, "50.00", 5);
        cart.addItem(book, 1);
        when(cartService.getOrCreate(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.checkout(user);
        book.setPrice(new BigDecimal("99.00"));

        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void checkoutFailsWithEmptyCart() {
        when(cartService.getOrCreate(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.checkout(user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkoutFailsWhenStockIsNotEnough() {
        Product book = TestData.product(10, "50.00", 1);
        cart.addItem(book, 3);
        when(cartService.getOrCreate(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.checkout(user)).isInstanceOf(BusinessException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelingPendingOrderReturnsStock() {
        Product book = TestData.product(10, "50.00", 3);
        Order order = new Order(user);
        order.addItem(new OrderItem(book, 2));
        when(orderRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelMine(user, 5L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    void markAsPaidIsIdempotent() {
        Order order = new Order(user);
        order.changeStatus(OrderStatus.PAID);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        orderService.markAsPaid(5L, "123");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void cannotShipUnpaidOrder() {
        Order order = new Order(user);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(5L, OrderStatus.SHIPPED))
                .isInstanceOf(BusinessException.class);
    }
}
