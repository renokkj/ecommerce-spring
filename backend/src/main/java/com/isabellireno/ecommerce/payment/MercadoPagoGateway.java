package com.isabellireno.ecommerce.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.isabellireno.ecommerce.config.AppProperties;
import com.isabellireno.ecommerce.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Integração com o Checkout Pro do Mercado Pago via API REST.
 * Com um access token de teste (TEST-...), as cobranças acontecem no ambiente sandbox.
 */
public class MercadoPagoGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoGateway.class);

    private final RestClient client;
    private final AppProperties properties;

    public MercadoPagoGateway(RestClient.Builder builder, AppProperties properties) {
        this.properties = properties;
        this.client = builder
                .baseUrl("https://api.mercadopago.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.payment().mercadopagoAccessToken())
                .build();
    }

    @Override
    public CheckoutSession createCheckout(Order order) {
        String frontend = properties.payment().frontendUrl();
        List<Map<String, Object>> items = order.getItems().stream()
                .map(item -> Map.<String, Object>of(
                        "title", item.getProductName(),
                        "quantity", item.getQuantity(),
                        "unit_price", item.getUnitPrice(),
                        "currency_id", "BRL"))
                .toList();
        Map<String, Object> body = Map.of(
                "items", items,
                "external_reference", order.getId().toString(),
                "notification_url", properties.publicBaseUrl() + "/api/payments/webhook",
                "back_urls", Map.of(
                        "success", frontend + "/pedidos/" + order.getId(),
                        "pending", frontend + "/pedidos/" + order.getId(),
                        "failure", frontend + "/pedidos/" + order.getId()));
        JsonNode response = client.post()
                .uri("/checkout/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("Resposta vazia do Mercado Pago.");
        }
        String url = response.hasNonNull("sandbox_init_point")
                ? response.get("sandbox_init_point").asText()
                : response.get("init_point").asText();
        return new CheckoutSession(url, "mercadopago");
    }

    @Override
    public Optional<ApprovedPayment> findApprovedPayment(String paymentId) {
        try {
            JsonNode payment = client.get()
                    .uri("/v1/payments/{id}", paymentId)
                    .retrieve()
                    .body(JsonNode.class);
            if (payment == null || !"approved".equals(payment.path("status").asText())) {
                return Optional.empty();
            }
            String reference = payment.path("external_reference").asText(null);
            if (reference == null || !reference.matches("\\d+")) {
                return Optional.empty();
            }
            return Optional.of(new ApprovedPayment(Long.valueOf(reference), paymentId));
        } catch (RestClientException e) {
            log.warn("Falha ao consultar o pagamento {} no Mercado Pago", paymentId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isSimulated() {
        return false;
    }
}
