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

 class FIFOStrategyTest {

   private FIFOStrategy fifoStrategy;

   private Product product;

   private List < Batch > batches;

   @BeforeEach

   void setUp() {

     fifoStrategy = new FIFOStrategy();

     product = new Product();

     product.setId(1L);

     product.setProductCode("PROD-001");

     product.setName("Test Product");

     // Create batches with different received dates 

     Batch batch1 = new Batch();

     batch1.setId(1L);

     batch1.setBatchNumber("BATCH-001");

     batch1.setQuantity(100);

     batch1.setExpiryDate(LocalDate.now().plusMonths(6));

     batch1.setReceivedDate(LocalDate.now().minusDays(20)); // Oldest 

     batch1.setProduct(product);

     Batch batch2 = new Batch();

     batch2.setId(2L);

     batch2.setBatchNumber("BATCH-002");

     batch2.setQuantity(50);

     batch2.setExpiryDate(LocalDate.now().plusMonths(3));

     batch2.setReceivedDate(LocalDate.now().minusDays(10)); // Middle 

     batch2.setProduct(product);

     Batch batch3 = new Batch();

     batch3.setId(3L);

     batch3.setBatchNumber("BATCH-003");

     batch3.setQuantity(30);

     batch3.setExpiryDate(LocalDate.now().plusMonths(9));

     batch3.setReceivedDate(LocalDate.now().minusDays(5)); // Newest 

     batch3.setProduct(product);

     batches = new ArrayList < > (Arrays.asList(batch1, batch2, batch3));

   }

   @Test

   void testAllocate_SingleBatchSufficient() {

     // Arrange 

     int quantityNeeded = 50;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber()); // Should pick oldest received 

     assertEquals(50, allocatedBatches.get(0).getQuantity());

   }

   @Test

   void testAllocate_MultipleBatchesNeeded() {

     // Arrange 

     int quantityNeeded = 120;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(2, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber()); // Oldest first 

     assertEquals(100, allocatedBatches.get(0).getQuantity());

     assertEquals("BATCH-002", allocatedBatches.get(1).getBatchNumber()); // Second oldest 

     assertEquals(20, allocatedBatches.get(1).getQuantity());

   }

   @Test

   void testAllocate_AllBatchesNeeded() {

     // Arrange 

     int quantityNeeded = 180;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(3, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber());

     assertEquals("BATCH-002", allocatedBatches.get(1).getBatchNumber());

     assertEquals("BATCH-003", allocatedBatches.get(2).getBatchNumber());

   }

   @Test

   void testAllocate_ExactBatchQuantity() {

     // Arrange 

     int quantityNeeded = 100;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertNotNull(allocatedBatches);

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber());

     assertEquals(100, allocatedBatches.get(0).getQuantity());

   }

   @Test

   void testAllocate_InsufficientStock() {

     // Arrange 

     int quantityNeeded = 200; // More than available (180) 

     // Act & Assert 

     InsufficientInventoryException exception = assertThrows(

       InsufficientInventoryException.class,

       () -> fifoStrategy.allocate(batches, quantityNeeded)

     );

     assertTrue(exception.getMessage().contains("Insufficient stock"));

     assertTrue(exception.getMessage().contains("Available: 180"));

     assertTrue(exception.getMessage().contains("Needed: 200"));

   }

   @Test

   void testAllocate_CorrectSortingByReceivedDate() {

     // Arrange 

     int quantityNeeded = 10;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert - Should allocate from BATCH-001 (oldest received) 

     assertEquals(1, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber());

     assertEquals(LocalDate.now().minusDays(20), allocatedBatches.get(0).getReceivedDate());

   }

   @Test

   void testAllocate_EmptyBatchList() {

     // Arrange 

     List < Batch > emptyBatches = new ArrayList < > ();

     // Act & Assert 

     assertThrows(InsufficientInventoryException.class,

       () -> fifoStrategy.allocate(emptyBatches, 10));

   }

   @Test

   void testGetStrategyName() {

     // Act 

     String strategyName = fifoStrategy.getStrategyName();

     // Assert 

     assertEquals("FIFO", strategyName);

   }

   @Test

   void testAllocate_OriginalBatchesUnmodified() {

     // Arrange 

     int quantityNeeded = 50;

     int originalBatch1Quantity = batches.get(0).getQuantity();

     // Act 

     fifoStrategy.allocate(batches, quantityNeeded);

     // Assert - Original batches should not be modified 

     assertEquals(originalBatch1Quantity, batches.get(0).getQuantity());

   }

   @Test

   void testAllocate_PartialAllocationFromMultipleBatches() {

     // Arrange 

     int quantityNeeded = 110;

     // Act 

     List < Batch > allocatedBatches = fifoStrategy.allocate(batches, quantityNeeded);

     // Assert 

     assertEquals(2, allocatedBatches.size());

     assertEquals("BATCH-001", allocatedBatches.get(0).getBatchNumber());

     assertEquals(100, allocatedBatches.get(0).getQuantity());

     assertEquals("BATCH-002", allocatedBatches.get(1).getBatchNumber());

     assertEquals(10, allocatedBatches.get(1).getQuantity());

   }

 }
 
 