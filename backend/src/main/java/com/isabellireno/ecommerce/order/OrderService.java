package com.isabellireno.ecommerce.order;

import com.isabellireno.ecommerce.cart.Cart;
import com.isabellireno.ecommerce.cart.CartItem;
import com.isabellireno.ecommerce.cart.CartService;
import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.common.NotFoundException;
import com.isabellireno.ecommerce.common.PageResponse;
import com.isabellireno.ecommerce.order.OrderDtos.OrderResponse;
import com.isabellireno.ecommerce.product.Product;
import com.isabellireno.ecommerce.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    /**
     * Fecha o pedido a partir do carrinho: confere estoque, dá baixa, copia preços e esvazia o carrinho.
     * Tudo na mesma transação; se um item falhar, nada é alterado.
     */
    @Transactional
    public OrderResponse checkout(User user) {
        Cart cart = cartService.getOrCreate(user);
        if (cart.isEmpty()) {
            throw new BusinessException("O carrinho está vazio.");
        }
        Order order = new Order(user);
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.isActive()) {
                throw new BusinessException("O produto \"" + product.getName() + "\" não está mais disponível.");
            }
            product.decreaseStock(cartItem.getQuantity());
            order.addItem(new OrderItem(product, cartItem.getQuantity()));
        }
        Order saved = orderRepository.save(order);
        cart.clear();
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listMine(User user, Pageable pageable) {
        return PageResponse.of(orderRepository.findByUserId(user.getId(), pageable), OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getMine(User user, Long orderId) {
        return OrderResponse.from(findMine(user, orderId));
    }

    @Transactional
    public OrderResponse cancelMine(User user, Long orderId) {
        Order order = findMine(user, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Só é possível cancelar pedidos que ainda não foram pagos.");
        }
        cancel(order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listAll(OrderStatus status, Pageable pageable) {
        var page = status == null ? orderRepository.findAll(pageable) : orderRepository.findByStatus(status, pageable);
        return PageResponse.of(page, OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus next) {
        Order order = findById(orderId);
        if (next == OrderStatus.CANCELED) {
            cancel(order);
        } else {
            order.changeStatus(next);
        }
        return OrderResponse.from(order);
    }

    /** Chamado pelo módulo de pagamento quando o pagamento é aprovado. */
    @Transactional
    public void markAsPaid(Long orderId, String paymentReference) {
        Order order = findById(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            return; // webhooks podem chegar repetidos
        }
        order.changeStatus(OrderStatus.PAID);
        order.setPaymentReference(paymentReference);
    }

    @Transactional(readOnly = true)
    public Order findForPayment(User user, Long orderId) {
        Order order = findMine(user, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Este pedido não está aguardando pagamento.");
        }
        order.getItems().size(); // carrega os itens antes de sair da transação
        return order;
    }

    private void cancel(Order order) {
        order.changeStatus(OrderStatus.CANCELED);
        order.getItems().forEach(item -> item.getProduct().increaseStock(item.getQuantity()));
    }

    private Order findMine(User user, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }

    private Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado."));
    }
}
