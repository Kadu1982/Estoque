package com.austral.estoque.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteCriticalOperationalFlow() throws Exception {
        String rand = String.valueOf(System.currentTimeMillis());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login":"admin",
                      "password":"Admin@123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("accessToken").asText();
        String userId = loginJson.path("userId").asText();
        assertThat(token).isNotBlank();
        assertThat(userId).isNotBlank();

        MvcResult supplierResult = mockMvc.perform(post("/api/v1/suppliers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"SUP-%s",
                      "name":"Supplier %s",
                      "country":"AO",
                      "currency":"USD",
                      "active":true
                    }
                    """.formatted(rand, rand)))
            .andExpect(status().isCreated())
            .andReturn();

        String supplierId = objectMapper.readTree(supplierResult.getResponse().getContentAsString()).path("id").asText();
        assertThat(supplierId).isNotBlank();

        MvcResult itemResult = mockMvc.perform(post("/api/v1/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"ITM-%s",
                      "description":"Item %s",
                      "unitOfMeasure":"UN",
                      "criticality":"MEDIO",
                      "minStock":1,
                      "maxStock":10,
                      "active":true
                    }
                    """.formatted(rand, rand)))
            .andExpect(status().isCreated())
            .andReturn();

        String itemId = objectMapper.readTree(itemResult.getResponse().getContentAsString()).path("id").asText();
        assertThat(itemId).isNotBlank();

        MvcResult requisitionResult = mockMvc.perform(post("/api/v1/requisitions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "urgency":"NORMAL",
                      "justification":"Integration test requisition %s",
                      "notes":"auto"
                    }
                    """.formatted(rand)))
            .andExpect(status().isCreated())
            .andReturn();

        String requisitionId = objectMapper.readTree(requisitionResult.getResponse().getContentAsString()).path("id").asText();
        assertThat(requisitionId).isNotBlank();

        mockMvc.perform(patch("/api/v1/requisitions/{id}/approve", requisitionId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"PO-%s",
                      "supplierId":"%s",
                      "requesterId":"%s",
                      "requisitionId":"%s",
                      "notes":"auto",
                      "items":[
                        {"itemId":"%s","quantity":2,"unitPrice":5.5}
                      ]
                    }
                    """.formatted(rand, supplierId, userId, requisitionId, itemId)))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode orderJson = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        String orderId = orderJson.path("id").asText();
        String orderItemId = orderJson.path("items").get(0).path("id").asText();
        assertThat(orderId).isNotBlank();
        assertThat(orderItemId).isNotBlank();

        MvcResult warehouseLookup = mockMvc.perform(get("/api/v1/lookups/warehouses")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode warehouses = objectMapper.readTree(warehouseLookup.getResponse().getContentAsString());
        assertThat(warehouses.isArray()).isTrue();
        assertThat(warehouses.size()).isGreaterThan(0);
        String warehouseId = warehouses.get(0).path("id").asText();
        assertThat(warehouseId).isNotBlank();

        MvcResult receiveResult = mockMvc.perform(post("/api/v1/orders/{id}/receive", orderId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "warehouseId":"%s",
                      "receiverId":"%s",
                      "items":[
                        {"orderItemId":"%s","quantity":2}
                      ]
                    }
                    """.formatted(warehouseId, userId, orderItemId)))
            .andExpect(status().isOk())
            .andReturn();

        String orderStatus = objectMapper.readTree(receiveResult.getResponse().getContentAsString()).path("status").asText();
        assertThat(orderStatus).isIn("ENTREGUE", "PARCIALMENTE_RECEBIDO");

        MvcResult stockResult = mockMvc.perform(get("/api/v1/stock")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode stockArray = objectMapper.readTree(stockResult.getResponse().getContentAsString());
        boolean hasItemInStock = false;
        for (JsonNode node : stockArray) {
            if (itemId.equals(node.path("itemId").asText())) {
                hasItemInStock = true;
                break;
            }
        }
        assertThat(hasItemInStock).isTrue();

        mockMvc.perform(get("/api/v1/dashboard/summary")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login":"admin",
                      "password":"wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectReceiveWhenOrderItemDoesNotBelongToOrder() throws Exception {
        String token = loginAndGetToken();
        String userId = loginAndGetUserId();
        String rand = String.valueOf(System.currentTimeMillis());

        String supplierA = createSupplier(token, rand + "A");
        String supplierB = createSupplier(token, rand + "B");
        String itemA = createItem(token, rand + "A");
        String itemB = createItem(token, rand + "B");

        String reqA = createRequisition(token, rand + "A");
        String reqB = createRequisition(token, rand + "B");
        approveRequisition(token, reqA);
        approveRequisition(token, reqB);

        JsonNode orderA = createOrder(token, rand + "A", supplierA, userId, reqA, itemA);
        JsonNode orderB = createOrder(token, rand + "B", supplierB, userId, reqB, itemB);

        String orderAId = orderA.path("id").asText();
        String orderBItemId = orderB.path("items").get(0).path("id").asText();
        String warehouseId = firstWarehouseId(token);

        mockMvc.perform(post("/api/v1/orders/{id}/receive", orderAId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "warehouseId":"%s",
                      "receiverId":"%s",
                      "items":[
                        {"orderItemId":"%s","quantity":1}
                      ]
                    }
                    """.formatted(warehouseId, userId, orderBItemId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Order item does not belong to this order"));
    }

    @Test
    void shouldCreateRedStockAlertWhenReceivedQuantityIsAtOrBelowThirtyPercentOfPlannedQuantity() throws Exception {
        String token = loginAndGetToken();
        String userId = loginAndGetUserId();
        String suffix = String.valueOf(System.currentTimeMillis()) + "LOW";

        String supplierId = createSupplier(token, suffix);
        String itemId = createItem(token, suffix);
        String requisitionId = createRequisition(token, suffix);
        approveRequisition(token, requisitionId);

        JsonNode order = createOrder(token, suffix, supplierId, userId, requisitionId, itemId);
        String orderId = order.path("id").asText();
        String orderItemId = order.path("items").get(0).path("id").asText();
        String warehouseId = firstWarehouseId(token);

        mockMvc.perform(post("/api/v1/orders/{id}/receive", orderId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "warehouseId":"%s",
                      "receiverId":"%s",
                      "items":[
                        {"orderItemId":"%s","quantity":2}
                      ]
                    }
                    """.formatted(warehouseId, userId, orderItemId)))
            .andExpect(status().isOk());

        MvcResult alertsResult = mockMvc.perform(get("/api/v1/stock-alerts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode alerts = objectMapper.readTree(alertsResult.getResponse().getContentAsString());
        boolean hasRedAlertForItem = false;
        for (JsonNode alert : alerts) {
            if (itemId.equals(alert.path("itemId").asText())
                && warehouseId.equals(alert.path("warehouseId").asText())
                && "RED".equals(alert.path("statusColor").asText())) {
                hasRedAlertForItem = true;
                break;
            }
        }
        assertThat(hasRedAlertForItem).isTrue();
    }

    private String loginAndGetToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login":"admin",
                      "password":"Admin@123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("accessToken").asText();
    }

    private String loginAndGetUserId() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "login":"admin",
                      "password":"Admin@123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).path("userId").asText();
    }

    private String createSupplier(String token, String suffix) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/suppliers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"SUP-%s",
                      "name":"Supplier %s",
                      "country":"AO",
                      "currency":"USD",
                      "active":true
                    }
                    """.formatted(suffix, suffix)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    private String createItem(String token, String suffix) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"ITM-%s",
                      "description":"Item %s",
                      "unitOfMeasure":"UN",
                      "criticality":"MEDIO",
                      "minStock":1,
                      "maxStock":10,
                      "active":true
                    }
                    """.formatted(suffix, suffix)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    private String createRequisition(String token, String suffix) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/requisitions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "urgency":"NORMAL",
                      "justification":"Integration requisition %s",
                      "notes":"auto"
                    }
                    """.formatted(suffix)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
    }

    private void approveRequisition(String token, String requisitionId) throws Exception {
        mockMvc.perform(patch("/api/v1/requisitions/{id}/approve", requisitionId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    private JsonNode createOrder(String token, String suffix, String supplierId, String userId, String requisitionId, String itemId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code":"PO-%s",
                      "supplierId":"%s",
                      "requesterId":"%s",
                      "requisitionId":"%s",
                      "notes":"auto",
                      "items":[
                        {"itemId":"%s","quantity":2,"unitPrice":5.5}
                      ]
                    }
                    """.formatted(suffix, supplierId, userId, requisitionId, itemId)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String firstWarehouseId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/lookups/warehouses")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode warehouses = objectMapper.readTree(result.getResponse().getContentAsString());
        return warehouses.get(0).path("id").asText();
    }
}
