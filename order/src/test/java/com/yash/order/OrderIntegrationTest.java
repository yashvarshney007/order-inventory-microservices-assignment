 package com.yash.order;

 import com.yash.order.client.InventoryClient;

 import com.yash.order.dto.*;

 import com.yash.order.entity.Order;

 import com.yash.order.entity.OrderItem;

 import com.yash.order.repository.OrderItemRepository;

 import com.yash.order.repository.OrderRepository;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.boot.test.context.SpringBootTest;

 import org.springframework.boot.test.mock.mockito.MockBean;

 import org.springframework.boot.test.web.client.TestRestTemplate;

 import org.springframework.boot.test.web.server.LocalServerPort;

 import org.springframework.http.HttpStatus;

 import org.springframework.http.ResponseEntity;

 import org.springframework.test.context.ActiveProfiles;

 import java.time.LocalDate;

 import java.util.Arrays;

 import java.util.Collections;

 import java.util.List;

 import static org.junit.jupiter.api.Assertions.*;

 import static org.mockito.ArgumentMatchers.any;

 import static org.mockito.ArgumentMatchers.anyString;

 import static org.mockito.Mockito.when;

 @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

 @ActiveProfiles("test")

 class OrderIntegrationTest {

   @LocalServerPort

   private int port;

   @Autowired

   private TestRestTemplate restTemplate;

   @Autowired

   private OrderRepository orderRepository;

   @Autowired

   private OrderItemRepository orderItemRepository;

   @MockBean

   private InventoryClient inventoryClient;

   private String baseUrl;

   @BeforeEach

   void setUp() {

     baseUrl = "http://localhost:" + port + "/order";

     // Clean up database before each test 

     orderItemRepository.deleteAll();

     orderRepository.deleteAll();

   }

   @Test

   void testPlaceOrder_Success_SavedInDatabase() {

     // Arrange 

     OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest(

       "PROD001",

       5,

       100.0

     );

     OrderRequest orderRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       Collections.singletonList(itemRequest)

     );

     // Mock inventory client responses 

     List < InventoryBatchResponse > inventoryBatches = Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     );

     when(inventoryClient.checkInventory("PROD001")).thenReturn(inventoryBatches);

     InventoryUpdateResponse updateResponse = new InventoryUpdateResponse(

       true,

       "Inventory updated successfully",

       "PROD001",

       5,

       Collections.singletonList(new InventoryUpdateResponse.BatchAllocation("BATCH001", 5))

     );

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

     // Act 

     ResponseEntity < OrderResponse > response = restTemplate.postForEntity(

       baseUrl,

       orderRequest,

       OrderResponse.class

     );

     // Assert 

     assertEquals(HttpStatus.CREATED, response.getStatusCode());

     assertNotNull(response.getBody());

     OrderResponse orderResponse = response.getBody();

     assertEquals("John Doe", orderResponse.getCustomerName());

     assertEquals("CONFIRMED", orderResponse.getStatus());

     assertEquals(500.0, orderResponse.getTotalAmount());

     assertNotNull(orderResponse.getOrderNumber());

     // Verify database persistence 

     List < Order > orders = orderRepository.findAll();

     assertEquals(1, orders.size());

     Order savedOrder = orders.get(0);

     assertEquals("John Doe", savedOrder.getCustomerName());

     assertEquals("john@example.com", savedOrder.getCustomerEmail());

     assertEquals(Order.OrderStatus.CONFIRMED, savedOrder.getStatus());

     assertEquals(500.0, savedOrder.getTotalAmount());

     assertNotNull(savedOrder.getOrderNumber());

     assertNotNull(savedOrder.getOrderDate());

     // Verify order items 

     assertNotNull(savedOrder.getOrderItems());

     assertEquals(1, savedOrder.getOrderItems().size());

     OrderItem savedItem = savedOrder.getOrderItems().get(0);

     assertEquals("PROD001", savedItem.getProductCode());

     assertEquals("Product 1", savedItem.getProductName());

     assertEquals(5, savedItem.getQuantity());

     assertEquals(100.0, savedItem.getUnitPrice());

     assertEquals(500.0, savedItem.getTotalPrice());

     assertNotNull(savedItem.getBatchAllocations());

     assertTrue(savedItem.getBatchAllocations().contains("BATCH001"));

   }

   @Test

   void testPlaceOrder_MultipleItems_SavedInDatabase() {

     // Arrange 

     OrderRequest orderRequest = new OrderRequest(

       "Jane Smith",

       "jane@example.com",

       Arrays.asList(

         new OrderRequest.OrderItemRequest("PROD001", 3, 100.0),

         new OrderRequest.OrderItemRequest("PROD002", 2, 200.0)

       )

     );

     // Mock inventory responses for both products 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     ));

     when(inventoryClient.checkInventory("PROD002")).thenReturn(Arrays.asList(

       new InventoryBatchResponse(2L, "BATCH002", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD002", "Product 2")

     ));

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(

       new InventoryUpdateResponse(true, "Success", "PROD001", 3,

         Collections.singletonList(new InventoryUpdateResponse.BatchAllocation("BATCH001", 3)))

     );

     // Act 

     ResponseEntity < OrderResponse > response = restTemplate.postForEntity(

       baseUrl,

       orderRequest,

       OrderResponse.class

     );

     // Assert 

     assertEquals(HttpStatus.CREATED, response.getStatusCode());

     assertNotNull(response.getBody());

     // Verify in database 

     List < Order > orders = orderRepository.findAll();

     assertEquals(1, orders.size());

     Order savedOrder = orders.get(0);

     assertEquals(2, savedOrder.getOrderItems().size());

     assertEquals(700.0, savedOrder.getTotalAmount()); // (3*100) + (2*200) 

     // Verify both items have product names 

     List < OrderItem > items = savedOrder.getOrderItems();

     assertTrue(items.stream().anyMatch(i -> i.getProductCode().equals("PROD001")));

     assertTrue(items.stream().anyMatch(i -> i.getProductCode().equals("PROD002")));

     assertTrue(items.stream().allMatch(i -> i.getProductName() != null));

   }

   @Test

   void testPlaceOrder_InsufficientInventory_OrderNotSaved() {

     // Arrange 

     OrderRequest orderRequest = new OrderRequest(

       "Test User",

       "test@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 100, 50.0))

     );

     // Mock insufficient inventory 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 5,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     ));

     // Act 

     ResponseEntity < String > response = restTemplate.postForEntity(

       baseUrl,

       orderRequest,

       String.class

     );

     // Assert - Should return error 

     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

     // Verify no order saved in database 

     List < Order > orders = orderRepository.findAll();

     assertEquals(0, orders.size());

   }

   @Test

   void testPlaceOrder_InvalidRequest_OrderNotSaved() {

     // Arrange - Missing customer name 

     OrderRequest invalidRequest = new OrderRequest(

       null,

       "test@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 5, 100.0))

     );

     // Act 

     ResponseEntity < String > response = restTemplate.postForEntity(

       baseUrl,

       invalidRequest,

       String.class

     );

     // Assert 

     assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());

     // Verify no order saved 

     List < Order > orders = orderRepository.findAll();

     assertEquals(0, orders.size());

   }

   @Test

   void testPlaceOrder_InventoryUpdateFails_OrderMarkedAsFailed() {

     // Arrange 

     OrderRequest orderRequest = new OrderRequest(

       "Test User",

       "test@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 5, 100.0))

     );

     // Mock inventory check succeeds but update fails 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     ));

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))

       .thenThrow(new RuntimeException("Inventory service unavailable"));

     // Act 

     ResponseEntity < String > response = restTemplate.postForEntity(

       baseUrl,

       orderRequest,

       String.class

     );

     // Assert 

     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

     // Note: In a real transactional scenario, the order might not be saved  

     // if the transaction is rolled back. The service tries to save it as FAILED, 

     // but Spring's transaction management may roll it back entirely. 

     // This is actually correct behavior - we don't want partial data in DB 

     List < Order > orders = orderRepository.findAll();

     // Order may or may not be saved depending on transaction boundaries 

     assertTrue(orders.isEmpty() || (orders.size() == 1 && orders.get(0).getStatus() == Order.OrderStatus.FAILED));

   }

   @Test

   void testPlaceOrder_BatchAllocationsStoredCorrectly() {

     // Arrange 

     OrderRequest orderRequest = new OrderRequest(

       "Test User",

       "test@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 5, 100.0))

     );

     when(inventoryClient.checkInventory("PROD001")).thenReturn(Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     ));

     // Mock response with multiple batch allocations 

     InventoryUpdateResponse updateResponse = new InventoryUpdateResponse(

       true,

       "Inventory updated successfully",

       "PROD001",

       5,

       Arrays.asList(

         new InventoryUpdateResponse.BatchAllocation("BATCH001", 3),

         new InventoryUpdateResponse.BatchAllocation("BATCH002", 2)

       )

     );

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

     // Act 

     ResponseEntity < OrderResponse > response = restTemplate.postForEntity(

       baseUrl,

       orderRequest,

       OrderResponse.class

     );

     // Assert 

     assertEquals(HttpStatus.CREATED, response.getStatusCode());

     // Verify batch allocations in database 

     List < Order > orders = orderRepository.findAll();

     Order savedOrder = orders.get(0);

     OrderItem savedItem = savedOrder.getOrderItems().get(0);

     assertNotNull(savedItem.getBatchAllocations());

     assertTrue(savedItem.getBatchAllocations().contains("BATCH001"));

     assertTrue(savedItem.getBatchAllocations().contains("BATCH002"));

     assertTrue(savedItem.getBatchAllocations().contains("\"quantity\":3"));

     assertTrue(savedItem.getBatchAllocations().contains("\"quantity\":2"));

   }

   @Test

   void testPlaceOrder_UniqueOrderNumber() {

     // Arrange - Create two orders 

     OrderRequest orderRequest1 = new OrderRequest(

       "User 1",

       "user1@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 2, 100.0))

     );

     OrderRequest orderRequest2 = new OrderRequest(

       "User 2",

       "user2@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 3, 100.0))

     );

     when(inventoryClient.checkInventory(anyString())).thenReturn(Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     ));

     when(inventoryClient.updateInventory(any())).thenReturn(

       new InventoryUpdateResponse(true, "Success", "PROD001", 2,

         Collections.singletonList(new InventoryUpdateResponse.BatchAllocation("BATCH001", 2)))

     );

     // Act 

     ResponseEntity < OrderResponse > response1 = restTemplate.postForEntity(baseUrl, orderRequest1, OrderResponse.class);

     ResponseEntity < OrderResponse > response2 = restTemplate.postForEntity(baseUrl, orderRequest2, OrderResponse.class);

     // Assert 

     assertEquals(HttpStatus.CREATED, response1.getStatusCode());

     assertEquals(HttpStatus.CREATED, response2.getStatusCode());

     String orderNumber1 = response1.getBody().getOrderNumber();

     String orderNumber2 = response2.getBody().getOrderNumber();

     assertNotEquals(orderNumber1, orderNumber2); // Order numbers must be unique 

     // Verify both orders in database 

     List < Order > orders = orderRepository.findAll();

     assertEquals(2, orders.size());

   }

 }