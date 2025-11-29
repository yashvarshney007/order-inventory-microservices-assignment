 package com.yash.inventory.strategy;

 import com.yash.inventory.entity.Batch;

 import java.util.List;

 /** 

  * Strategy interface for different inventory allocation methods 

  * Allows extension for different inventory management strategies (FIFO, FEFO, LIFO, etc.) 

  */

 public interface InventoryStrategy {

   /** 

    * Allocate quantity from available batches based on the strategy 

    *  

    * @param batches Available batches for the product 

    * @param quantityNeeded Quantity to allocate 

    * @return List of batches with allocated quantities 

    * @throws IllegalArgumentException if insufficient stock 

    */

   List < Batch > allocate(List < Batch > batches, int quantityNeeded);

   /** 

    * Get the name of the strategy 

    *  

    * @return Strategy name 

    */

   String getStrategyName();

 }