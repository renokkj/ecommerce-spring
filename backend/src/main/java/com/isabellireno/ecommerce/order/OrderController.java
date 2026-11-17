package com.isabellireno.ecommerce.order;

import com.isabellireno.ecommerce.common.PageResponse;
import com.isabellireno.ecommerce.order.OrderDtos.OrderResponse;
import com.isabellireno.ecommerce.order.OrderDtos.UpdateStatusRequest;
import com.isabellireno.ecommerce.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Finaliza o carrinho e cria um pedido PENDING")
    public OrderResponse checkout(@AuthenticationPrincipal User user) {
        return orderService.checkout(user);
    }

    @GetMapping("/api/orders")
    @Operation(summary = "Pedidos do usuário autenticado (mais recentes primeiro)")
    public PageResponse<OrderResponse> listMine(@AuthenticationPrincipal User user,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return orderService.listMine(user, pageable(page, size));
    }

    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Detalhe de um pedido do usuário")
    public OrderResponse getMine(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orderService.getMine(user, id);
    }

    @PostMapping("/api/orders/{id}/cancel")
    @Operation(summary = "Cancela um pedido ainda não pago e devolve o estoque")
    public OrderResponse cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orderService.cancelMine(user, id);
    }

    @GetMapping("/api/admin/orders")
    @Operation(summary = "Todos os pedidos, com filtro opcional por status (admin)")
    public PageResponse<OrderResponse> listAll(@RequestParam(required = false) OrderStatus status,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return orderService.listAll(status, pageable(page, size));
    }

    @PatchMapping("/api/admin/orders/{id}/status")
    @Operation(summary = "Altera o status de um pedido seguindo o fluxo permitido (admin)")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException("Paginação inválida: page >= 0 e size entre 1 e 50.");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    }
}
