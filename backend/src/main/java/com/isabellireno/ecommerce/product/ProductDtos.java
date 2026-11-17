package com.isabellireno.ecommerce.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank(message = "Informe o nome.") @Size(max = 150) String name,
            @Size(max = 2000) String description,
            @NotNull(message = "Informe o preço.") @DecimalMin(value = "0.00", message = "Preço não pode ser negativo.")
            @Digits(integer = 8, fraction = 2) BigDecimal price,
            @NotNull(message = "Informe o estoque.") @Min(value = 0, message = "Estoque não pode ser negativo.") Integer stock,
            @NotNull(message = "Informe a categoria.") Long categoryId,
            Boolean active
    ) {}

    public record ProductResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            int stock,
            String imageUrl,
            boolean active,
            Long categoryId,
            String categoryName
    ) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                    p.getImageUrl(), p.isActive(), p.getCategory().getId(), p.getCategory().getName());
        }
    }

    /** Filtros aceitos em GET /api/products. */
    public record ProductFilter(
            String q,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock
    ) {}
}
