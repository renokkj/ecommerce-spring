package com.isabellireno.ecommerce.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.isabellireno.ecommerce.payment.PaymentGateway.CheckoutSession;
import com.isabellireno.ecommerce.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Pagamentos")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/orders/{orderId}/payment")
    @Operation(summary = "Inicia o pagamento de um pedido PENDING e devolve a URL de checkout")
    public CheckoutSession start(@AuthenticationPrincipal User user, @PathVariable Long orderId) {
        return paymentService.startCheckout(user, orderId);
    }

    @PostMapping("/api/orders/{orderId}/payment/simulate-approval")
    @Operation(summary = "Aprova o pagamento no modo simulado (sem Mercado Pago configurado)")
    public ResponseEntity<Void> simulateApproval(@AuthenticationPrincipal User user, @PathVariable Long orderId) {
        paymentService.approveSimulated(user, orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Webhook do Mercado Pago. Aceita o formato atual (JSON com data.id) e o antigo (query string).
     * Sempre responde 200 para o provedor não reenviar notificações já processadas.
     */
    @PostMapping("/api/payments/webhook")
    @Operation(summary = "Webhook de notificação de pagamento (Mercado Pago)")
    public ResponseEntity<Void> webhook(@RequestParam(required = false) String type,
                                        @RequestParam(name = "data.id", required = false) String dataId,
                                        @RequestBody(required = false) JsonNode body) {
        String resolvedType = type;
        String paymentId = dataId;
        if (body != null) {
            if (resolvedType == null) {
                resolvedType = body.path("type").asText(null);
            }
            if (paymentId == null) {
                paymentId = body.path("data").path("id").asText(null);
            }
        }
        paymentService.handleNotification(resolvedType, paymentId);
        return ResponseEntity.ok().build();
    }
}
