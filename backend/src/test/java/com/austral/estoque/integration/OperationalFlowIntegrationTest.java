package com.austral.estoque.integration;

import com.austral.estoque.domain.organization.Warehouse;
import com.austral.estoque.repository.organization.CostCenterRepository;
import com.austral.estoque.repository.organization.OperationalUnitRepository;
import com.austral.estoque.repository.organization.SectorRepository;
import com.austral.estoque.repository.organization.WarehouseRepository;
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

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OperationalUnitRepository operationalUnitRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private CostCenterRepository costCenterRepository;

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

    @Test
    void shouldTransferStockBetweenWarehouses() throws Exception {
        String token = loginAndGetToken();
        String userId = loginAndGetUserId();
        String suffix = String.valueOf(System.currentTimeMillis()) + "TRF";

        String supplierId = createSupplier(token, suffix);
        String itemId = createItem(token, suffix);
        String requisitionId = createRequisition(token, suffix);
        approveRequisition(token, requisitionId);

        JsonNode order = createOrder(token, suffix, supplierId, userId, requisitionId, itemId);
        String sourceWarehouseId = firstWarehouseId(token);
        receiveFirstOrderItem(token, userId, order, sourceWarehouseId, "2");

        String targetWarehouseId = createAdditionalWarehouse(suffix).getId().toString();

        mockMvc.perform(post("/api/v1/stock-transfers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sourceWarehouseId":"%s",
                      "targetWarehouseId":"%s",
                      "items":[
                        {"itemId":"%s","quantity":1}
                      ]
                    }
                    """.formatted(sourceWarehouseId, targetWarehouseId, itemId)))
            .andExpect(status().isCreated());

        JsonNode stock = listStock(token);
        assertThat(quantityFor(stock, sourceWarehouseId, itemId)).isEqualByComparingTo("1.000");
        assertThat(quantityFor(stock, targetWarehouseId, itemId)).isEqualByComparingTo("1.000");
    }

    @Test
    void shouldIssueStockConsumptionFromWarehouse() throws Exception {
        String token = loginAndGetToken();
        String userId = loginAndGetUserId();
        String suffix = String.valueOf(System.currentTimeMillis()) + "ISS";

        String supplierId = createSupplier(token, suffix);
        String itemId = createItem(token, suffix);
        String requisitionId = createRequisition(token, suffix);
        approveRequisition(token, requisitionId);

        JsonNode order = createOrder(token, suffix, supplierId, userId, requisitionId, itemId);
        String warehouseId = firstWarehouseId(token);
        receiveFirstOrderItem(token, userId, order, warehouseId, "2");

        mockMvc.perform(post("/api/v1/issues")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "warehouseId":"%s",
                      "items":[
                        {"itemId":"%s","quantity":1}
                      ]
                    }
                    """.formatted(warehouseId, itemId)))
            .andExpect(status().isCreated());

        JsonNode stock = listStock(token);
        assertThat(quantityFor(stock, warehouseId, itemId)).isEqualByComparingTo("1.000");
    }

    @Test
    void shouldCreateAndFetchAsset() throws Exception {
        String token = loginAndGetToken();
        String suffix = String.valueOf(System.currentTimeMillis()) + "AST";
        String unitId = operationalUnitRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow()
            .getId()
            .toString();
        String sectorId = sectorRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow()
            .getId()
            .toString();
        String costCenterId = costCenterRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow()
            .getId()
            .toString();

        MvcResult createResult = mockMvc.perform(post("/api/v1/assets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tag":"RESP-%s",
                      "name":"Respirador %s",
                      "type":"EQUIPAMENTO_SAUDE",
                      "unitId":"%s",
                      "sectorId":"%s",
                      "mainCostCenterId":"%s"
                    }
                    """.formatted(suffix, suffix, unitId, sectorId, costCenterId)))
            .andExpect(status().isCreated())
            .andReturn();

        String assetId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tag").value("RESP-" + suffix))
            .andExpect(jsonPath("$.type").value("EQUIPAMENTO_SAUDE"))
            .andExpect(jsonPath("$.unitId").value(unitId));
    }

    @Test
    void shouldCreateAndFetchWaterWell() throws Exception {
        String token = loginAndGetToken();
        String suffix = String.valueOf(System.currentTimeMillis()) + "WTR";
        String unitId = operationalUnitRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow()
            .getId()
            .toString();

        MvcResult createResult = mockMvc.perform(post("/api/v1/water/wells")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name":"Poco %s",
                      "unitId":"%s",
                      "communityName":"Vila %s",
                      "latitude":-4.270,
                      "longitude":15.284,
                      "populationServed":800,
                      "capacityM3PerDay":25.5
                    }
                    """.formatted(suffix, unitId, suffix)))
            .andExpect(status().isCreated())
            .andReturn();

        String wellId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(get("/api/v1/water/wells/{id}", wellId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Poco " + suffix))
            .andExpect(jsonPath("$.unitId").value(unitId))
            .andExpect(jsonPath("$.communityName").value("Vila " + suffix))
            .andExpect(jsonPath("$.populationServed").value(800));
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

    private Warehouse createAdditionalWarehouse(String suffix) {
        var unit = operationalUnitRepository.findFirstByDeletedAtIsNullAndActiveTrueOrderByCreatedAtAsc()
            .orElseThrow();
        return warehouseRepository.save(Warehouse.builder()
            .unit(unit)
            .name("Warehouse " + suffix)
            .code("WH-" + suffix)
            .type(Warehouse.WarehouseType.DESCENTRALIZADO)
            .active(true)
            .build());
    }

    private void receiveFirstOrderItem(String token, String userId, JsonNode order, String warehouseId, String quantity) throws Exception {
        String orderId = order.path("id").asText();
        String orderItemId = order.path("items").get(0).path("id").asText();

        mockMvc.perform(post("/api/v1/orders/{id}/receive", orderId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "warehouseId":"%s",
                      "receiverId":"%s",
                      "items":[
                        {"orderItemId":"%s","quantity":%s}
                      ]
                    }
                    """.formatted(warehouseId, userId, orderItemId, quantity)))
            .andExpect(status().isOk());
    }

    private JsonNode listStock(String token) throws Exception {
        MvcResult stockResult = mockMvc.perform(get("/api/v1/stock")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(stockResult.getResponse().getContentAsString());
    }

    private java.math.BigDecimal quantityFor(JsonNode stock, String warehouseId, String itemId) {
        for (JsonNode node : stock) {
            if (warehouseId.equals(node.path("warehouseId").asText()) && itemId.equals(node.path("itemId").asText())) {
                return node.path("quantity").decimalValue();
            }
        }
        return java.math.BigDecimal.ZERO;
    }
}
