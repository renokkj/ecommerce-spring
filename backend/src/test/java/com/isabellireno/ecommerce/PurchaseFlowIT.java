package com.isabellireno.ecommerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de ponta a ponta da API com PostgreSQL real (Testcontainers) e as migrations do Flyway:
 * cadastro, catálogo, carrinho, checkout, pagamento simulado e permissões.
 */
@SpringBootTest(properties = {
        "app.admin.email=admin@teste.com",
        "app.admin.password=senha-admin-123",
        "app.storage.upload-dir=target/test-uploads",
        "app.payment.mercadopago-access-token="
})
@AutoConfigureMockMvc
@Testcontainers
class PurchaseFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void customerBuysAProductAndPays() throws Exception {
        String token = register("cliente-" + UUID.randomUUID() + "@teste.com");

        JsonNode catalog = getJson("/api/products?q=teclado", null);
        JsonNode keyboard = catalog.get("content").get(0);
        long productId = keyboard.get("id").asLong();
        int stockBefore = keyboard.get("stock").asInt();

        mvc.perform(post("/api/cart/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2));

        String orderBody = mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long orderId = json.readTree(orderBody).get("id").asLong();

        assertThat(getJson("/api/products/" + productId, null).get("stock").asInt()).isEqualTo(stockBefore - 2);
        assertThat(getJson("/api/cart", token).get("items")).isEmpty();

        mvc.perform(post("/api/orders/" + orderId + "/payment").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("simulated"));

        mvc.perform(post("/api/orders/" + orderId + "/payment/simulate-approval")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(getJson("/api/orders/" + orderId, token).get("status").asText()).isEqualTo("PAID");
    }

    @Test
    void cannotBuyMoreThanTheStock() throws Exception {
        String token = register("cliente-" + UUID.randomUUID() + "@teste.com");
        JsonNode product = getJson("/api/products?q=fone", null).get("content").get(0);

        mvc.perform(post("/api/cart/items").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.get("id").asLong() + ",\"quantity\":"
                                + (product.get("stock").asInt() + 1) + "}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void catalogSupportsPaginationAndPriceFilter() throws Exception {
        JsonNode page = getJson("/api/products?size=2&page=0&sort=price&direction=asc", null);
        assertThat(page.get("content")).hasSize(2);
        assertThat(page.get("totalElements").asInt()).isGreaterThan(2);

        JsonNode cheap = getJson("/api/products?maxPrice=40", null);
        assertThat(cheap.get("content")).isNotEmpty();
        cheap.get("content").forEach(p ->
                assertThat(p.get("price").decimalValue()).isLessThanOrEqualTo(new java.math.BigDecimal("40")));
    }

    @Test
    void adminRoutesRequireAdminRole() throws Exception {
        String customer = register("cliente-" + UUID.randomUUID() + "@teste.com");
        String body = "{\"name\":\"Novo\",\"price\":10,\"stock\":1,\"categoryId\":1}";

        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/products").header("Authorization", "Bearer " + customer)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        String admin = login("admin@teste.com", "senha-admin-123");
        mvc.perform(post("/api/admin/products").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalido\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    private String register(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cliente\",\"email\":\"" + email + "\",\"password\":\"senha-segura-1\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private JsonNode getJson(String url, String token) throws Exception {
        var request = get(url);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return json.readTree(mvc.perform(request).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
