package com.isabellireno.ecommerce.cart;

import com.isabellireno.ecommerce.cart.CartDtos.AddItemRequest;
import com.isabellireno.ecommerce.cart.CartDtos.CartResponse;
import com.isabellireno.ecommerce.cart.CartDtos.UpdateQuantityRequest;
import com.isabellireno.ecommerce.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrinho")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Carrinho do usuário autenticado")
    public CartResponse get(@AuthenticationPrincipal User user) {
        return cartService.get(user);
    }

    @PostMapping("/items")
    @Operation(summary = "Adiciona um produto (soma a quantidade se já existir)")
    public CartResponse add(@AuthenticationPrincipal User user, @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(user, request.productId(), request.quantity());
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Altera a quantidade de um item")
    public CartResponse update(@AuthenticationPrincipal User user, @PathVariable Long productId,
                               @Valid @RequestBody UpdateQuantityRequest request) {
        return cartService.updateQuantity(user, productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove um item")
    public CartResponse remove(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        return cartService.removeItem(user, productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Esvazia o carrinho")
    public void clear(@AuthenticationPrincipal User user) {
        cartService.clear(user);
    }
}
