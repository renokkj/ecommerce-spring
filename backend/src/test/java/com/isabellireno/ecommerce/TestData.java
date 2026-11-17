package com.isabellireno.ecommerce;

import com.isabellireno.ecommerce.category.Category;
import com.isabellireno.ecommerce.product.Product;
import com.isabellireno.ecommerce.user.Role;
import com.isabellireno.ecommerce.user.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public final class TestData {

    private TestData() {
    }

    public static User user(long id) {
        User user = new User("Cliente " + id, "cliente" + id + "@teste.com", "hash", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static Product product(long id, String price, int stock) {
        Category category = new Category("Categoria");
        ReflectionTestUtils.setField(category, "id", 1L);
        Product product = new Product("Produto " + id, "desc", new BigDecimal(price), stock, category);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
