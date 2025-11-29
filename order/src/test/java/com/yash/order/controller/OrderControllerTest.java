 package com.yash.order.controller;

 import com.fasterxml.jackson.databind.ObjectMapper;

 import com.yash.order.dto.OrderRequest;

 import com.yash.order.dto.OrderResponse;

 import com.yash.order.exception.InsufficientInventoryException;

 import com.yash.order.exception.OrderProcessingException;

 import com.yash.order.service.OrderService;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

 import org.springframework.boot.test.mock.mockito.MockBean;

 import org.springframework.http.MediaType;

 import org.springframework.test.web.servlet.MockMvc;

 import java.time.LocalDateTime;

 import java.util.Collections;

 import static org.mockito.ArgumentMatchers.any;

 import static org.mockito.Mockito.when;

 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

 @WebMvcTest(OrderController.class)

 class OrderControllerTest {

   @Autowired

   private MockMvc mockMvc;

   @Autowired

   private ObjectMapper objectMapper;

   @MockBean

   private OrderService orderService;

   private OrderRequest validOrderRequest;

   private OrderResponse successResponse;

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

     // Setup success response 

     OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse(

       "PROD001",

       "Product 1",

       5,

       100.0,

       500.0,

       "[{\"batchNumber\":\"BATCH001\",\"quantity\":5}]"

     );

     successResponse = new OrderResponse(

       1L,

       "ORD-12345678",

       "John Doe",

       "john@example.com",

       "CONFIRMED",

       LocalDateTime.now(),

       500.0,

       Collections.singletonList(itemResponse),

       "Order placed successfully"

     );

   }

   @Test

   void testPlaceOrder_Success() throws Exception {

     // Arrange 

     when(orderService.processOrder(any(OrderRequest.class))).thenReturn(successResponse);

     // Act & Assert 

     mockMvc.perform(post("/order")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(validOrderRequest)))

       .andExpect(status().isCreated())

       .andExpect(jsonPath("$.orderNumber").value("ORD-12345678"))

       .andExpect(jsonPath("$.customerName").value("John Doe"))

       .andExpect(jsonPath("$.status").value("CONFIRMED"))

       .andExpect(jsonPath("$.totalAmount").value(500.0))

       .andExpect(jsonPath("$.items[0].productCode").value("PROD001"))

       .andExpect(jsonPath("$.items[0].productName").value("Product 1"))

       .andExpect(jsonPath("$.items[0].quantity").value(5))

       .andExpect(jsonPath("$.items[0].batchAllocations").exists());

   }

   @Test

   void testPlaceOrder_InvalidRequest() throws Exception {

     // Arrange 

     when(orderService.processOrder(any(OrderRequest.class)))

       .thenThrow(new IllegalArgumentException("Customer name is required"));

     // Act & Assert 

     mockMvc.perform(post("/order")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(validOrderRequest)))

       .andExpect(status().isBadRequest())

       .andExpect(jsonPath("$.error").value("Customer name is required"));

   }

   @Test

   void testPlaceOrder_InsufficientInventory() throws Exception {

     // Arrange 

     when(orderService.processOrder(any(OrderRequest.class)))

       .thenThrow(new InsufficientInventoryException("Insufficient inventory for product PROD001"));

     // Act & Assert 

     mockMvc.perform(post("/order")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(validOrderRequest)))

       .andExpect(status().isInternalServerError())

       .andExpect(jsonPath("$.error").exists());

   }

   @Test

   void testPlaceOrder_OrderProcessingException() throws Exception {

     // Arrange 

     when(orderService.processOrder(any(OrderRequest.class)))

       .thenThrow(new OrderProcessingException("Order processing failed"));

     // Act & Assert 

     mockMvc.perform(post("/order")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(validOrderRequest)))

       .andExpect(status().isInternalServerError())

       .andExpect(jsonPath("$.error").exists());

   }

   @Test

   void testPlaceOrder_RuntimeException() throws Exception {

     // Arrange 

     when(orderService.processOrder(any(OrderRequest.class)))

       .thenThrow(new RuntimeException("Unexpected error"));

     // Act & Assert 

     mockMvc.perform(post("/order")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(validOrderRequest)))

       .andExpect(status().isInternalServerError())

       .andExpect(jsonPath("$.error").value("Order processing failed: Unexpected error"));

   }

 }