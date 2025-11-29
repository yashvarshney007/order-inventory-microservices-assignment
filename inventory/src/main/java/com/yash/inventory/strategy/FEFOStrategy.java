 package com.yash.inventory.strategy;

 import com.yash.inventory.entity.Batch;

 import com.yash.inventory.exception.InsufficientInventoryException;

 import org.springframework.stereotype.Component;

 import java.util.ArrayList;

 import java.util.Comparator;

 import java.util.List;

 /** 

  * FEFO (First Expired First Out) Strategy 

  * Allocates inventory from batches that expire soonest 

  */

 @Component

 public class FEFOStrategy implements InventoryStrategy {

   @Override

   public List < Batch > allocate(List < Batch > batches, int quantityNeeded) {

     // Sort batches by expiry date (ascending - earliest expiry first) 

     List < Batch > sortedBatches = new ArrayList < > (batches);

     sortedBatches.sort(Comparator.comparing(Batch::getExpiryDate));

     return allocateFromSortedBatches(sortedBatches, quantityNeeded);

   }

   private List < Batch > allocateFromSortedBatches(List < Batch > sortedBatches, int quantityNeeded) {

     List < Batch > allocatedBatches = new ArrayList < > ();

     int remainingQuantity = quantityNeeded;

     // Calculate total available quantity 

     int totalAvailable = sortedBatches.stream()

       .mapToInt(Batch::getQuantity)

       .sum();

     if (totalAvailable < quantityNeeded) {

       throw new InsufficientInventoryException(

         "Insufficient stock. Available: " + totalAvailable + ", Needed: " + quantityNeeded);

     }

     // Allocate from batches in order 

     for (Batch batch: sortedBatches) {

       if (remainingQuantity <= 0) {

         break;

       }

       if (batch.getQuantity() > 0) {

         int allocatedFromBatch = Math.min(batch.getQuantity(), remainingQuantity);

         // Create a copy with allocated quantity (for tracking) 

         Batch allocatedBatch = new Batch();

         allocatedBatch.setId(batch.getId());

         allocatedBatch.setBatchNumber(batch.getBatchNumber());

         allocatedBatch.setProduct(batch.getProduct());

         allocatedBatch.setQuantity(allocatedFromBatch);

         allocatedBatch.setExpiryDate(batch.getExpiryDate());

         allocatedBatch.setReceivedDate(batch.getReceivedDate());

         allocatedBatches.add(allocatedBatch);

         remainingQuantity -= allocatedFromBatch;

       }

     }

     return allocatedBatches;

   }

   @Override

   public String getStrategyName() {

     return "FEFO";

   }

 }