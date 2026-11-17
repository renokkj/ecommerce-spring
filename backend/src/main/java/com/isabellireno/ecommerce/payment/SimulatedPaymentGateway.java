package com.isabellireno.ecommerce.payment;

import com.isabellireno.ecommerce.config.AppProperties;
import com.isabellireno.ecommerce.order.Order;

import java.util.Optional;

/**
 * Gateway usado quando MERCADOPAGO_ACCESS_TOKEN não está definido.
 * Leva o cliente para uma tela do front-end onde o pagamento é aprovado manualmente,
 * o que permite testar o fluxo completo sem conta no provedor.
 */
public class SimulatedPaymentGateway implements PaymentGateway {

    private final AppProperties properties;

    public SimulatedPaymentGateway(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    public CheckoutSession createCheckout(Order order) {
        return new CheckoutSession(properties.payment().frontendUrl() + "/pagamento-simulado/" + order.getId(),
                "simulated");
    }

    @Override
    public Optional<ApprovedPayment> findApprovedPayment(String paymentId) {
        return Optional.empty();
    }

    @Override
    public boolean isSimulated() {
        return true;
    }
}
