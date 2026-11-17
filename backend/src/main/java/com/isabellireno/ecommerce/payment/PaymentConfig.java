package com.isabellireno.ecommerce.payment;

import com.isabellireno.ecommerce.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfig.class);

    @Bean
    public PaymentGateway paymentGateway(AppProperties properties, RestClient.Builder builder) {
        if (properties.payment().mercadoPagoEnabled()) {
            log.info("Pagamentos: Mercado Pago");
            return new MercadoPagoGateway(builder, properties);
        }
        log.info("Pagamentos: gateway simulado (MERCADOPAGO_ACCESS_TOKEN não definido)");
        return new SimulatedPaymentGateway(properties);
    }
}
