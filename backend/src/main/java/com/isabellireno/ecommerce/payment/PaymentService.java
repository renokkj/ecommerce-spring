package com.isabellireno.ecommerce.payment;

import com.isabellireno.ecommerce.common.BusinessException;
import com.isabellireno.ecommerce.order.Order;
import com.isabellireno.ecommerce.order.OrderService;
import com.isabellireno.ecommerce.payment.PaymentGateway.CheckoutSession;
import com.isabellireno.ecommerce.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentGateway gateway;
    private final OrderService orderService;

    public PaymentService(PaymentGateway gateway, OrderService orderService) {
        this.gateway = gateway;
        this.orderService = orderService;
    }

    public CheckoutSession startCheckout(User user, Long orderId) {
        Order order = orderService.findForPayment(user, orderId);
        return gateway.createCheckout(order);
    }

    /** Recebe a notificação do provedor e confirma o pagamento consultando a API dele. */
    public void handleNotification(String type, String paymentId) {
        if (!"payment".equals(type) || paymentId == null || paymentId.isBlank()) {
            return;
        }
        gateway.findApprovedPayment(paymentId).ifPresent(approved -> {
            log.info("Pagamento {} aprovado para o pedido {}", approved.paymentId(), approved.orderId());
            try {
                orderService.markAsPaid(approved.orderId(), approved.paymentId());
            } catch (RuntimeException e) {
                // Ex.: pedido cancelado antes da confirmação. Registra para análise manual.
                log.warn("Pagamento {} aprovado, mas o pedido {} não pôde ser atualizado: {}",
                        approved.paymentId(), approved.orderId(), e.getMessage());
            }
        });
    }

    /** Aprovação manual, disponível apenas com o gateway simulado. */
    public void approveSimulated(User user, Long orderId) {
        if (!gateway.isSimulated()) {
            throw new BusinessException("Pagamento simulado desativado: o Mercado Pago está configurado.");
        }
        orderService.findForPayment(user, orderId);
        orderService.markAsPaid(orderId, "SIMULATED-" + orderId);
    }

    public boolean isSimulated() {
        return gateway.isSimulated();
    }
}
