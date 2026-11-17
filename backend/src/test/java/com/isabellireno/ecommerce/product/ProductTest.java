package com.isabellireno.ecommerce.product;

import com.isabellireno.ecommerce.TestData;
import com.isabellireno.ecommerce.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void decreaseStockSubtractsQuantity() {
        Product product = TestData.product(1, "10.00", 5);

        product.decreaseStock(3);

        assertThat(product.getStock()).isEqualTo(2);
    }

    @Test
    void decreaseStockFailsWhenNotEnough() {
        Product product = TestData.product(1, "10.00", 2);

        assertThatThrownBy(() -> product.decreaseStock(3))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(product.getStock()).isEqualTo(2);
    }

    @Test
    void decreaseStockRejectsNonPositiveQuantity() {
        Product product = TestData.product(1, "10.00", 2);

        assertThatThrownBy(() -> product.decreaseStock(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
