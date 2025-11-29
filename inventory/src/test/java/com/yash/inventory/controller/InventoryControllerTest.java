 package com.yash.inventory.controller;

 import com.yash.inventory.dto.BatchResponse;

 import com.yash.inventory.dto.InventoryUpdateRequest;

 import com.yash.inventory.dto.InventoryUpdateResponse;

 import com.yash.inventory.exception.InsufficientInventoryException;

 import com.yash.inventory.exception.ProductNotFoundException;

 import com.yash.inventory.service.InventoryService;

 import com.fasterxml.jackson.databind.ObjectMapper;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

 import org.springframework.boot.test.mock.mockito.MockBean;

 import org.springframework.http.MediaType;

 import org.springframework.test.web.servlet.MockMvc;

 import java.time.LocalDate;

 import java.util.Arrays;

 import java.util.List;

 import static org.mockito.ArgumentMatchers.any;

 import static org.mockito.ArgumentMatchers.anyString;

 import static org.mockito.Mockito.*;

 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
 
 @WebMvcTest(InventoryController.class)
 class InventoryControllerTest {

   @Autowired

   private MockMvc mockMvc;

   @Autowired

   private ObjectMapper objectMapper;

   @MockBean

   private InventoryService inventoryService;

   private List < BatchResponse > batchResponses;

   private InventoryUpdateResponse updateResponse;

   @BeforeEach

   void setUp() {

     BatchResponse batch1 = new BatchResponse();

     batch1.setId(1L);

     batch1.setBatchNumber("BATCH-001");

     batch1.setQuantity(100);

     batch1.setExpiryDate(LocalDate.now().plusMonths(6));

     batch1.setReceivedDate(LocalDate.now().minusDays(10));

     batch1.setProductCode("PROD-001");

     batch1.setProductName("Widget A");

     batchResponses = Arrays.asList(batch1);

     // Setup update response 

     InventoryUpdateResponse.BatchAllocation allocation =

       new InventoryUpdateResponse.BatchAllocation("BATCH-001", 30);

     updateResponse = new InventoryUpdateResponse();

     updateResponse.setSuccess(true);

     updateResponse.setMessage("Inventory updated successfully using FEFO strategy");

     updateResponse.setProductCode("PROD-001");

     updateResponse.setQuantityDeducted(30);

     updateResponse.setAllocations(Arrays.asList(allocation));

   }

   @Test

   void testGetInventory_Success() throws Exception {

     // Arrange 

     when(inventoryService.getBatchesByProduct("PROD-001")).thenReturn(batchResponses);

     // Act & Assert 

     mockMvc.perform(get("/inventory/PROD-001")

         .contentType(MediaType.APPLICATION_JSON))

       .andExpect(status().isOk())

       .andExpect(jsonPath("$[0].batchNumber").value("BATCH-001"))

       .andExpect(jsonPath("$[0].quantity").value(100))

       .andExpect(jsonPath("$[0].productCode").value("PROD-001"));

     verify(inventoryService, times(1)).getBatchesByProduct("PROD-001");

   }

   @Test

   void testGetInventory_ProductNotFound() throws Exception {

     // Arrange 

     when(inventoryService.getBatchesByProduct(anyString()))

       .thenThrow(new ProductNotFoundException("Product not found"));

     // Act & Assert 

     mockMvc.perform(get("/inventory/INVALID")

         .contentType(MediaType.APPLICATION_JSON))

       .andExpect(status().is5xxServerError())

       .andExpect(jsonPath("$.error").value("Internal server error: Product not found"));

     verify(inventoryService, times(1)).getBatchesByProduct("INVALID");

   }

   @Test

   void testUpdateInventory_Success() throws Exception {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(30);

     request.setStrategy("FEFO");

     when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))

       .thenReturn(updateResponse);

     // Act & Assert 

     mockMvc.perform(post("/inventory/update")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(request)))

       .andExpect(status().isOk())

       .andExpect(jsonPath("$.success").value(true))

       .andExpect(jsonPath("$.productCode").value("PROD-001"))

       .andExpect(jsonPath("$.quantityDeducted").value(30));

     verify(inventoryService, times(1)).updateInventory(any(InventoryUpdateRequest.class));

   }

   @Test

   void testUpdateInventory_InsufficientInventory() throws Exception {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(1000);

     when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))

       .thenThrow(new InsufficientInventoryException("Insufficient inventory"));

     // Act & Assert 

     mockMvc.perform(post("/inventory/update")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(request)))

       .andExpect(status().is5xxServerError())

       .andExpect(jsonPath("$.error").value("Internal server error: Insufficient inventory"));

     verify(inventoryService, times(1)).updateInventory(any(InventoryUpdateRequest.class));

   }

   @Test

   void testUpdateInventory_ProductNotFound() throws Exception {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("INVALID");

     request.setQuantity(10);

     when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))

       .thenThrow(new ProductNotFoundException("Product not found"));

     // Act & Assert 

     mockMvc.perform(post("/inventory/update")

         .contentType(MediaType.APPLICATION_JSON)

         .content(objectMapper.writeValueAsString(request)))

       .andExpect(status().is5xxServerError())

       .andExpect(jsonPath("$.error").value("Internal server error: Product not found"));

     verify(inventoryService, times(1)).updateInventory(any(InventoryUpdateRequest.class));

   }

   @Test

   void testGetAllProducts_Success() throws Exception {

     // Arrange 

     when(inventoryService.getAllProducts()).thenReturn(Arrays.asList());

     // Act & Assert 

     mockMvc.perform(get("/inventory/products")

         .contentType(MediaType.APPLICATION_JSON))

       .andExpect(status().isOk());

     verify(inventoryService, times(1)).getAllProducts();

   }

   @Test

   void testGetAllProducts_EmptyList() throws Exception {

     // Arrange 

     when(inventoryService.getAllProducts()).thenReturn(Arrays.asList());

     // Act & Assert 

     mockMvc.perform(get("/inventory/products")

         .contentType(MediaType.APPLICATION_JSON))

       .andExpect(status().isOk())

       .andExpect(jsonPath("$").isArray())

       .andExpect(jsonPath("$").isEmpty());

     verify(inventoryService, times(1)).getAllProducts();

   }

 }