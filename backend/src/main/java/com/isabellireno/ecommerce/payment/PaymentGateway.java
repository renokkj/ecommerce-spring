package com.isabellireno.ecommerce.payment;

import com.isabellireno.ecommerce.order.Order;

import java.util.Optional;

/** Ponto de integração com o provedor de pagamento. */
public interface PaymentGateway {

    /** Cria a cobrança e devolve a URL para onde o cliente deve ser enviado. */
    CheckoutSession createCheckout(Order order);

    /**
     * Consulta o pagamento no provedor e devolve o id do pedido quando ele estiver aprovado.
     * A notificação recebida no webhook nunca é tratada como verdade sem essa consulta.
     */
    Optional<ApprovedPayment> findApprovedPayment(String paymentId);

    boolean isSimulated();

    record CheckoutSession(String checkoutUrl, String provider) {}

    record ApprovedPayment(Long orderId, String paymentId) {}
}
