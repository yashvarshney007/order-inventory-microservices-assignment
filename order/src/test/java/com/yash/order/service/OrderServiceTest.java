 package com.yash.order.service;

 import com.yash.order.client.InventoryClient;

 import com.yash.order.dto.*;

 import com.yash.order.entity.Order;

 import com.yash.order.entity.OrderItem;

 import com.yash.order.exception.InsufficientInventoryException;

 import com.yash.order.exception.OrderProcessingException;

 import com.yash.order.repository.OrderRepository;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import org.junit.jupiter.api.extension.ExtendWith;

 import org.mockito.ArgumentCaptor;

 import org.mockito.InjectMocks;

 import org.mockito.Mock;

 import org.mockito.junit.jupiter.MockitoExtension;

 import java.time.LocalDate;

 import java.util.ArrayList;

 import java.util.Arrays;

 import java.util.Collections;

 import java.util.List;

 import static org.junit.jupiter.api.Assertions.*;

 import static org.mockito.ArgumentMatchers.*;

 import static org.mockito.Mockito.*;

 @ExtendWith(MockitoExtension.class)

 class OrderServiceTest {

   @Mock

   private OrderRepository orderRepository;

   @Mock

   private InventoryClient inventoryClient;

   @InjectMocks

   private OrderService orderService;

   private OrderRequest validOrderRequest;

   private List < InventoryBatchResponse > availableBatches;

   private InventoryUpdateResponse updateResponse;

   @BeforeEach

   void setUp() {

     // Setup valid order request 

     OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(

       "PROD001",

       5,

       100.0

     );

     validOrderRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       Collections.singletonList(item)

     );

     // Setup available batches 

     availableBatches = Arrays.asList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1"),

       new InventoryBatchResponse(2L, "BATCH002", 5,

         LocalDate.now().plusMonths(3), LocalDate.now(), "PROD001", "Product 1")

     );

     // Setup update response 

     updateResponse = new InventoryUpdateResponse(

       true,

       "Inventory updated successfully",

       "PROD001",

       5,

       Arrays.asList(

         new InventoryUpdateResponse.BatchAllocation("BATCH001", 5)

       )

     );

   }

   @Test

   void testProcessOrder_Success() {

     // Arrange 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(availableBatches);

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

     Order savedOrder = new Order();

     savedOrder.setId(1L);

     savedOrder.setOrderNumber("ORD-12345678");

     savedOrder.setStatus(Order.OrderStatus.CONFIRMED);

     savedOrder.setOrderItems(new ArrayList < > ()); // Initialize orderItems 

     when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {

       Order order = invocation.getArgument(0);

       if (order.getOrderItems() == null) {

         order.setOrderItems(new ArrayList < > ());

       }

       return order;

     });

     // Act 

     OrderResponse response = orderService.processOrder(validOrderRequest);

     // Assert 

     assertNotNull(response);

     verify(inventoryClient, times(2)).checkInventory("PROD001"); // Once for availability check, once for product name 

     verify(inventoryClient, times(1)).updateInventory(any(InventoryUpdateRequest.class));

     verify(orderRepository, times(2)).save(any(Order.class)); // Once for pending, once for confirmed 

   }

   @Test

   void testProcessOrder_InvalidRequest_NullCustomerName() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       null,

       "john@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 5, 100.0))

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

     verify(orderRepository, never()).save(any(Order.class));

   }

   @Test

   void testProcessOrder_InvalidRequest_EmptyCustomerName() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       "",

       "john@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 5, 100.0))

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

   }

   @Test

   void testProcessOrder_InvalidRequest_NullItems() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       null

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

   }

   @Test

   void testProcessOrder_InvalidRequest_EmptyItems() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       Collections.emptyList()

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

   }

   @Test

   void testProcessOrder_InvalidRequest_NullProductCode() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest(null, 5, 100.0))

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

   }

   @Test

   void testProcessOrder_InvalidRequest_ZeroQuantity() {

     // Arrange 

     OrderRequest invalidRequest = new OrderRequest(

       "John Doe",

       "john@example.com",

       Collections.singletonList(new OrderRequest.OrderItemRequest("PROD001", 0, 100.0))

     );

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(invalidRequest));

     verify(inventoryClient, never()).checkInventory(anyString());

   }

   @Test

   void testProcessOrder_ProductNotFound() {

     // Arrange 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(Collections.emptyList());

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(validOrderRequest));

     verify(inventoryClient, times(1)).checkInventory("PROD001");

     verify(orderRepository, never()).save(any(Order.class));

   }

   @Test

   void testProcessOrder_InsufficientInventory() {

     // Arrange 

     List < InventoryBatchResponse > insufficientBatches = Collections.singletonList(

       new InventoryBatchResponse(1L, "BATCH001", 3,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     );

     when(inventoryClient.checkInventory("PROD001")).thenReturn(insufficientBatches);

     // Act & Assert 

     assertThrows(InsufficientInventoryException.class, () -> orderService.processOrder(validOrderRequest));

     verify(inventoryClient, times(1)).checkInventory("PROD001");

     verify(orderRepository, never()).save(any(Order.class));

   }

   @Test

   void testProcessOrder_InventoryUpdateFails() {

     // Arrange 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(availableBatches);

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))

       .thenThrow(new RuntimeException("Inventory service unavailable"));

     Order savedOrder = new Order();

     savedOrder.setId(1L);

     savedOrder.setOrderNumber("ORD-12345678");

     savedOrder.setStatus(Order.OrderStatus.PENDING);

     when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {

       Order order = invocation.getArgument(0);

       if (order.getOrderItems() == null) {

         order.setOrderItems(new ArrayList < > ());

       }

       return order;

     });

     // Act & Assert 

     assertThrows(OrderProcessingException.class, () -> orderService.processOrder(validOrderRequest));

     verify(inventoryClient, times(2)).checkInventory("PROD001"); // Once for availability, once for product name 

     verify(inventoryClient, times(1)).updateInventory(any(InventoryUpdateRequest.class));

     verify(orderRepository, times(2)).save(any(Order.class)); // Saves as PENDING then FAILED 

   }

   @Test

   void testProcessOrder_ProductNameFetchedFromInventory() {

     // Arrange 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(availableBatches);

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

     ArgumentCaptor < Order > orderCaptor = ArgumentCaptor.forClass(Order.class);

     when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {

       Order order = invocation.getArgument(0);

       if (order.getOrderItems() == null) {

         order.setOrderItems(new ArrayList < > ());

       }

       return order;

     });

     // Act 

     orderService.processOrder(validOrderRequest);

     // Assert 

     verify(orderRepository, times(2)).save(orderCaptor.capture());

     Order capturedOrder = orderCaptor.getAllValues().get(0); // First save with PENDING status 

     assertNotNull(capturedOrder.getOrderItems());

     assertFalse(capturedOrder.getOrderItems().isEmpty());

     OrderItem orderItem = capturedOrder.getOrderItems().get(0);

     assertEquals("Product 1", orderItem.getProductName()); // Product name should be set 

     assertEquals("PROD001", orderItem.getProductCode());

   }

   @Test

   void testProcessOrder_MultipleItems_Success() {

     // Arrange 

     OrderRequest.OrderItemRequest item1 = new OrderRequest.OrderItemRequest("PROD001", 5, 100.0);

     OrderRequest.OrderItemRequest item2 = new OrderRequest.OrderItemRequest("PROD002", 3, 200.0);

     OrderRequest multiItemRequest = new OrderRequest(

       "Jane Doe",

       "jane@example.com",

       Arrays.asList(item1, item2)

     );

     List < InventoryBatchResponse > batches1 = Collections.singletonList(

       new InventoryBatchResponse(1L, "BATCH001", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD001", "Product 1")

     );

     List < InventoryBatchResponse > batches2 = Collections.singletonList(

       new InventoryBatchResponse(2L, "BATCH002", 10,

         LocalDate.now().plusMonths(6), LocalDate.now(), "PROD002", "Product 2")

     );

     when(inventoryClient.checkInventory("PROD001")).thenReturn(batches1);

     when(inventoryClient.checkInventory("PROD002")).thenReturn(batches2);

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(updateResponse);

     when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {

       Order order = invocation.getArgument(0);

       if (order.getOrderItems() == null) {

         order.setOrderItems(new ArrayList < > ());

       }

       return order;

     });

     // Act 

     OrderResponse response = orderService.processOrder(multiItemRequest);

     // Assert 

     assertNotNull(response);

     verify(inventoryClient, times(2)).checkInventory("PROD001"); // Availability + product name 

     verify(inventoryClient, times(2)).checkInventory("PROD002"); // Availability + product name 

     verify(inventoryClient, times(2)).updateInventory(any(InventoryUpdateRequest.class));

   }

   @Test

   void testProcessOrder_BatchAllocationsStored() {

     // Arrange 

     when(inventoryClient.checkInventory("PROD001")).thenReturn(availableBatches);

     InventoryUpdateResponse updateResponseWithAllocations = new InventoryUpdateResponse(

       true,

       "Inventory updated successfully",

       "PROD001",

       5,

       Arrays.asList(

         new InventoryUpdateResponse.BatchAllocation("BATCH001", 3),

         new InventoryUpdateResponse.BatchAllocation("BATCH002", 2)

       )

     );

     when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))

       .thenReturn(updateResponseWithAllocations);

     ArgumentCaptor < Order > orderCaptor = ArgumentCaptor.forClass(Order.class);

     when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {

       Order order = invocation.getArgument(0);

       if (order.getOrderItems() == null) {

         order.setOrderItems(new ArrayList < > ());

       }

       return order;

     });

     // Act 

     orderService.processOrder(validOrderRequest);

     // Assert 

     verify(orderRepository, times(2)).save(orderCaptor.capture());

     Order confirmedOrder = orderCaptor.getAllValues().get(1); // Second save with CONFIRMED status 

     assertNotNull(confirmedOrder.getOrderItems());

     assertFalse(confirmedOrder.getOrderItems().isEmpty());

     OrderItem orderItem = confirmedOrder.getOrderItems().get(0);

     assertNotNull(orderItem.getBatchAllocations());

     assertTrue(orderItem.getBatchAllocations().contains("BATCH001"));

     assertTrue(orderItem.getBatchAllocations().contains("BATCH002"));

     assertTrue(orderItem.getBatchAllocations().contains("\"quantity\":3"));

     assertTrue(orderItem.getBatchAllocations().contains("\"quantity\":2"));

   }

 }