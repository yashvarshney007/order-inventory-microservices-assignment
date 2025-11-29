 package com.yash.inventory.service;

 import com.yash.inventory.dto.BatchResponse;

 import com.yash.inventory.dto.InventoryUpdateRequest;

 import com.yash.inventory.dto.InventoryUpdateResponse;

 import com.yash.inventory.entity.Batch;

 import com.yash.inventory.entity.Product;

 import com.yash.inventory.exception.InsufficientInventoryException;

 import com.yash.inventory.exception.ProductNotFoundException;

 import com.yash.inventory.repository.BatchRepository;

 import com.yash.inventory.repository.ProductRepository;

 import com.yash.inventory.strategy.InventoryStrategy;

 import com.yash.inventory.strategy.InventoryStrategyFactory;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import org.junit.jupiter.api.extension.ExtendWith;

 import org.mockito.InjectMocks;

 import org.mockito.Mock;

 import org.mockito.junit.jupiter.MockitoExtension;

 import java.time.LocalDate;

 import java.util.Arrays;

 import java.util.List;

 import java.util.Optional;

 import static org.junit.jupiter.api.Assertions.*;

 import static org.mockito.ArgumentMatchers.any;

 import static org.mockito.ArgumentMatchers.anyString;

 import static org.mockito.Mockito.*;

 @ExtendWith(MockitoExtension.class)

 class InventoryServiceTest {

   @Mock

   private ProductRepository productRepository;

   @Mock

   private BatchRepository batchRepository;

   @Mock

   private InventoryStrategyFactory strategyFactory;

   @Mock

   private InventoryStrategy inventoryStrategy;

   @InjectMocks

   private InventoryService inventoryService;

   private Product product;

   private List < Batch > batches;

   @BeforeEach

   void setUp() {

     product = new Product();

     product.setId(1L);

     product.setProductCode("PROD-001");

     product.setName("Widget A");

     product.setDescription("Test Product");

     Batch batch1 = new Batch();

     batch1.setId(1L);

     batch1.setBatchNumber("BATCH-001");

     batch1.setQuantity(100);

     batch1.setExpiryDate(LocalDate.now().plusMonths(6));

     batch1.setReceivedDate(LocalDate.now().minusDays(10));

     batch1.setProduct(product);

     Batch batch2 = new Batch();

     batch2.setId(2L);

     batch2.setBatchNumber("BATCH-002");

     batch2.setQuantity(50);

     batch2.setExpiryDate(LocalDate.now().plusMonths(3));

     batch2.setReceivedDate(LocalDate.now().minusDays(5));

     batch2.setProduct(product);

     batches = Arrays.asList(batch1, batch2);

   }

   @Test

   void testGetBatchesByProduct_Success() {

     // Arrange 

     when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(product));

     when(batchRepository.findByProductProductCodeOrderByExpiryDateAsc("PROD-001")).thenReturn(batches);

     // Act 

     List < BatchResponse > result = inventoryService.getBatchesByProduct("PROD-001");

     // Assert 

     assertNotNull(result);

     assertEquals(2, result.size());

     assertEquals("BATCH-001", result.get(0).getBatchNumber());

     assertEquals(100, result.get(0).getQuantity());

     assertEquals("PROD-001", result.get(0).getProductCode());

     verify(productRepository, times(1)).findByProductCode("PROD-001");

     verify(batchRepository, times(1)).findByProductProductCodeOrderByExpiryDateAsc("PROD-001");

   }

   @Test

   void testGetBatchesByProduct_ProductNotFound() {

     // Arrange 

     when(productRepository.findByProductCode(anyString())).thenReturn(Optional.empty());

     // Act & Assert 

     assertThrows(ProductNotFoundException.class, () -> {

       inventoryService.getBatchesByProduct("INVALID-CODE");

     });

     verify(productRepository, times(1)).findByProductCode("INVALID-CODE");

   }

   @Test

   void testUpdateInventory_Success() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(30);

     request.setStrategy("FEFO");

     Batch allocatedBatch = new Batch();

     allocatedBatch.setId(1L);

     allocatedBatch.setBatchNumber("BATCH-001");

     allocatedBatch.setQuantity(30);

     allocatedBatch.setExpiryDate(LocalDate.now().plusMonths(6));

     allocatedBatch.setReceivedDate(LocalDate.now().minusDays(10));

     allocatedBatch.setProduct(product);

     when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(product));

     when(batchRepository.findByProductProductCodeOrderByExpiryDateAsc("PROD-001")).thenReturn(batches);

     when(strategyFactory.getStrategy("FEFO")).thenReturn(inventoryStrategy);

     when(inventoryStrategy.allocate(batches, 30)).thenReturn(Arrays.asList(allocatedBatch));

     when(inventoryStrategy.getStrategyName()).thenReturn("FEFO");

     when(batchRepository.findById(1L)).thenReturn(Optional.of(batches.get(0)));

     when(batchRepository.save(any(Batch.class))).thenReturn(batches.get(0));

     // Act 

     InventoryUpdateResponse response = inventoryService.updateInventory(request);

     // Assert 

     assertNotNull(response);

     assertTrue(response.isSuccess());

     assertEquals("PROD-001", response.getProductCode());

     assertEquals(30, response.getQuantityDeducted());

     assertEquals(1, response.getAllocations().size());

     assertEquals("BATCH-001", response.getAllocations().get(0).getBatchNumber());

     assertEquals(30, response.getAllocations().get(0).getQuantityAllocated());

     verify(batchRepository, times(1)).save(any(Batch.class));

   }

   @Test

   void testUpdateInventory_InvalidRequest_NullProductCode() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setQuantity(30);

     // Act & Assert 

     assertThrows(IllegalArgumentException.class, () -> {

       inventoryService.updateInventory(request);

     });

   }

   @Test

   void testUpdateInventory_InvalidRequest_ZeroQuantity() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(0);

     // Act & Assert 

     assertThrows(IllegalArgumentException.class, () -> {

       inventoryService.updateInventory(request);

     });

   }

   @Test

   void testUpdateInventory_ProductNotFound() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("INVALID");

     request.setQuantity(10);

     when(productRepository.findByProductCode("INVALID")).thenReturn(Optional.empty());

     // Act & Assert 

     assertThrows(ProductNotFoundException.class, () -> {

       inventoryService.updateInventory(request);

     });

   }

   @Test

   void testUpdateInventory_NoInventoryAvailable() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(10);

     when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(product));

     when(batchRepository.findByProductProductCodeOrderByExpiryDateAsc("PROD-001")).thenReturn(Arrays.asList());

     // Act & Assert 

     assertThrows(InsufficientInventoryException.class, () -> {

       inventoryService.updateInventory(request);

     });

   }

   @Test

   void testUpdateInventory_InsufficientInventory() {

     // Arrange 

     InventoryUpdateRequest request = new InventoryUpdateRequest();

     request.setProductCode("PROD-001");

     request.setQuantity(200);

     when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(product));

     when(batchRepository.findByProductProductCodeOrderByExpiryDateAsc("PROD-001")).thenReturn(batches);

     when(strategyFactory.getStrategy()).thenReturn(inventoryStrategy);

     when(inventoryStrategy.allocate(batches, 200))

       .thenThrow(new InsufficientInventoryException("Insufficient stock. Available: 150, Needed: 200"));

     // Act & Assert 

     assertThrows(InsufficientInventoryException.class, () -> {

       inventoryService.updateInventory(request);

     });

   }

   @Test

   void testGetAllProducts_Success() {

     // Arrange 

     List < Product > products = Arrays.asList(product);

     when(productRepository.findAll()).thenReturn(products);

     when(batchRepository.findByProductProductCodeOrderByExpiryDateAsc("PROD-001")).thenReturn(batches);

     // Act 

     List < Object > result = inventoryService.getAllProducts();

     // Assert 

     assertNotNull(result);

     assertEquals(1, result.size());

     verify(productRepository, times(1)).findAll();

   }

   @Test

   void testGetAllProducts_EmptyList() {

     // Arrange 

     when(productRepository.findAll()).thenReturn(Arrays.asList());

     // Act 

     List < Object > result = inventoryService.getAllProducts();

     // Assert 

     assertNotNull(result);

     assertTrue(result.isEmpty());

     verify(productRepository, times(1)).findAll();

   }

 }