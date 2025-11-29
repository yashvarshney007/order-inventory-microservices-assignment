 package com.yash.inventory.strategy;

 import com.yash.inventory.entity.Batch;

 import com.yash.inventory.entity.Product;

 import com.yash.inventory.exception.InsufficientInventoryException;

 import org.junit.jupiter.api.BeforeEach;

 import org.junit.jupiter.api.Test;

 import java.time.LocalDate;

 import java.util.ArrayList;

 import java.util.Arrays;

 import java.util.List;

 import static org.junit.jupiter.api.Assertions.*;

 class FEFOStrategyTest {

   private FEFOStrategy fefoStrategy;

   private Product product;

   private List < Batch > batches;

   @BeforeEach

   void setUp() {

     fefoStrategy = new FEFOStrategy();

     product = new Product();

     product.setId(1L);

     product.setProductCode("PROD-001");

     product.setName("Test Product");

     // Create batches with different expiry dates 

     Batch batch1 = new Batch();

     batch1.setId(1L);

     batch1.setBatchNumber("BATCH-001");

     batch1.setQuantity(50);

     batch1.setExpiryDate(LocalDate.now().plusMonths(6)); // Expires later 

     batch1.setReceivedDate(LocalDate.now().minusDays(10));

     batch1.setProduct(product);

     Batch batch2 = new Batch();

     batch2.setId(2L);

     batch2.setBatchNumber("BATCH-002");

     batch2.setQuantity(30);

     batch2.setExpiryDate(LocalDate.now().plusMonths(3)); // Expires sooner 

     batch2.setReceivedDate(LocalDate.now().minusDays(5));

     batch2.setProduct(product);

     Batch batch3 = new Batch();

     batch3.setId(3L);

     batch3.setBatchNumber("BATCH-003");

     batch3.setQuantity(20);

     batch3.setExpiryDate(LocalDate.now().plusMonths(9)); // Expires last 

     batch3.setReceivedDate(LocalDate.now().minusDays(2));

     batch3.setProduct(product);

     batches = new ArrayList < > (Arrays.asList(batch1, batch2, batch3));

   }

   @Test

   void testAllocate_SingleBatchSufficient() {

     // Arrange 

     int quantityNeeded = 20;

     // Act 

     List < Batch > allocatedBatches = fefoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-002", allocatedBatches.get(0).getBatchNumber()); // Should pick earliest expiry 

     assertEquals(20, allocatedBatches.get(0).getQuantity());

   }

   @Test

   void testAllocate_MultipleBatchesNeeded() {

     // Arrange 

     int quantityNeeded = 50;

     // Act 

     List < Batch > allocatedBatches = fefoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(2, allocatedBatches.size());

     assertEquals("BATCH-002", allocatedBatches.get(0).getBatchNumber()); // Earliest expiry first 

     assertEquals(30, allocatedBatches.get(0).getQuantity());

     assertEquals("BATCH-001", allocatedBatches.get(1).getBatchNumber()); // Second earliest 

     assertEquals(20, allocatedBatches.get(1).getQuantity());

   }

   @Test

   void testAllocate_AllBatchesNeeded() {

     // Arrange 

     int quantityNeeded = 100;

     // Act 

     List < Batch > allocatedBatches = fefoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(3, allocatedBatches.size());

     assertEquals("BATCH-002", allocatedBatches.get(0).getBatchNumber());

     assertEquals("BATCH-001", allocatedBatches.get(1).getBatchNumber());

     assertEquals("BATCH-003", allocatedBatches.get(2).getBatchNumber());

   }

   @Test

   void testAllocate_ExactBatchQuantity() {

     // Arrange 

     int quantityNeeded = 30;

     // Act 

     List < Batch > allocatedBatches = fefoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-002", allocatedBatches.get(0).getBatchNumber());

     assertEquals(30, allocatedBatches.get(0).getQuantity());

   }

   @Test

   void testAllocate_InsufficientStock() {

     // Arrange 

     int quantityNeeded = 150; // More than available (100) 

     // Act & Assert 

     InsufficientInventoryException exception = assertThrows(

       InsufficientInventoryException.class,

       () -> fefoStrategy.allocate(batches, quantityNeeded)

     );

     assertTrue(exception.getMessage().contains("Insufficient stock"));

     assertTrue(exception.getMessage().contains("Available: 100"));

     assertTrue(exception.getMessage().contains("Needed: 150"));

   }

   @Test

   void testAllocate_CorrectSortingByExpiryDate() {

     // Arrange 

     int quantityNeeded = 10;

     // Act 

     List < Batch > allocatedBatches = fefoStrategy.allocate(batches, quantityNeeded);

     // Assert - Should allocate from BATCH-002 (earliest expiry) 

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-002", allocatedBatches.get(0).getBatchNumber());

     assertEquals(LocalDate.now().plusMonths(3), allocatedBatches.get(0).getExpiryDate());

   }

   @Test

   void testAllocate_EmptyBatchList() {

     // Arrange 

     List < Batch > emptyBatches = new ArrayList < > ();

     // Act & Assert 

     assertThrows(InsufficientInventoryException.class,

       () -> fefoStrategy.allocate(emptyBatches, 10));

   }

   @Test

   void testGetStrategyName() {

     // Act 

     String strategyName = fefoStrategy.getStrategyName();

     // Assert 

     assertEquals("FEFO", strategyName);

   }

   @Test

   void testAllocate_OriginalBatchesUnmodified() {

     // Arrange 

     int quantityNeeded = 20;

     int originalBatch2Quantity = batches.get(1).getQuantity();

     // Act 

     fefoStrategy.allocate(batches, quantityNeeded);

     // Assert - Original batches should not be modified 

     assertEquals(originalBatch2Quantity, batches.get(1).getQuantity());

   }

 }