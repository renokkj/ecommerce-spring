package com.isabellireno.ecommerce.common;

/**
 * Violação de regra de negócio (ex.: estoque insuficiente, carrinho vazio).
 * Retorna HTTP 422.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
