 package com.yash.inventory.strategy;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.beans.factory.annotation.Value;

 import org.springframework.stereotype.Component;

 /** 

  * Factory class to provide appropriate inventory allocation strategy 

  * Allows easy extension and switching between different strategies 

  */

 @Component

 public class InventoryStrategyFactory {

   private final FEFOStrategy fefoStrategy;

   private final FIFOStrategy fifoStrategy;

   @Value("${inventory.strategy:FEFO}")

   private String defaultStrategy;

   @Autowired

   public InventoryStrategyFactory(FEFOStrategy fefoStrategy, FIFOStrategy fifoStrategy) {

     this.fefoStrategy = fefoStrategy;

     this.fifoStrategy = fifoStrategy;

   }

   /** 

    * Get strategy based on configuration or default 

    *  

    * @return InventoryStrategy instance 

    */

   public InventoryStrategy getStrategy() {

     return getStrategy(defaultStrategy);

   }

   /** 

    * Get strategy by name 

    *  

    * @param strategyName Strategy name (FEFO, FIFO) 

    * @return InventoryStrategy instance 

    */

   public InventoryStrategy getStrategy(String strategyName) {

     if (strategyName == null || strategyName.isEmpty()) {

       strategyName = "FEFO";

     }

     return switch (strategyName.toUpperCase()) {

     case "FIFO" -> fifoStrategy;

     case "FEFO" -> fefoStrategy;

     default -> fefoStrategy; // Default to FEFO 

     };

   }

 }