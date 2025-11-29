 package com.yash.inventory.service;

 import java.util.ArrayList;
import java.util.List;
 import java.util.stream.Collectors;

 import org.springframework.stereotype.Service;

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

 import jakarta.transaction.Transactional;

 @Service

 public class InventoryService {

   private final ProductRepository productRepository;

   private final BatchRepository batchRepository;

   private final InventoryStrategyFactory strategyFactory;

   public InventoryService(ProductRepository productRepository,

     BatchRepository batchRepository,

     InventoryStrategyFactory strategyFactory) {

     this.productRepository = productRepository;

     this.batchRepository = batchRepository;

     this.strategyFactory = strategyFactory;

   }

   /** 

    * Get all batches for a product sorted by expiry date 

    *  

    * @param productCode Product code 

    * @return List of batches sorted by expiry date 

    */

   public List < BatchResponse > getBatchesByProduct(String productCode) {

     Product product = productRepository.findByProductCode(productCode)

       .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productCode));

     List < Batch > batches = batchRepository.findByProductProductCodeOrderByExpiryDateAsc(productCode);

     return batches.stream()

       .map(this::convertToBatchResponse)

       .collect(Collectors.toList());

   }

   /** 

    * Update inventory by deducting quantity using the specified strategy 

    *  

    * @param request Inventory update request 

    * @return Update response with allocation details 

    */

   @Transactional

   public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {

     // Validate request 

     if (request.getProductCode() == null || request.getQuantity() == null || request.getQuantity() <= 0) {

       throw new IllegalArgumentException("Invalid request: product code and positive quantity are required");

     }

     // Find product 

     Product product = productRepository.findByProductCode(request.getProductCode())

       .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductCode()));

     // Get all available batches 

     List < Batch > batches = batchRepository.findByProductProductCodeOrderByExpiryDateAsc(request.getProductCode());

     if (batches.isEmpty()) {

       throw new InsufficientInventoryException("No inventory available for product: " + request.getProductCode());

     }

     // Get strategy (use requested strategy or default) 

     String strategyName = request.getStrategy() != null ? request.getStrategy() : null;

     InventoryStrategy strategy = strategyName != null ?

       strategyFactory.getStrategy(strategyName) :

       strategyFactory.getStrategy();

     // Allocate inventory using strategy 

     List < Batch > allocatedBatches = strategy.allocate(batches, request.getQuantity());

     // Update database 

     List < InventoryUpdateResponse.BatchAllocation > allocations = new ArrayList<>();

     for (Batch allocatedBatch: allocatedBatches) {

       // Find the actual batch in database 

       Batch dbBatch = batchRepository.findById(allocatedBatch.getId())

         .orElseThrow(() -> new IllegalStateException("Batch not found: " + allocatedBatch.getId()));

       // Deduct quantity 

       int newQuantity = dbBatch.getQuantity() - allocatedBatch.getQuantity();

       dbBatch.setQuantity(newQuantity);

       batchRepository.save(dbBatch);

       // Track allocation 

       allocations.add(new InventoryUpdateResponse.BatchAllocation(

         dbBatch.getBatchNumber(),

         allocatedBatch.getQuantity()

       ));

     }

     // Build response 

     InventoryUpdateResponse response = new InventoryUpdateResponse();

     response.setSuccess(true);

     response.setMessage("Inventory updated successfully using " + strategy.getStrategyName() + " strategy");

     response.setProductCode(request.getProductCode());

     response.setQuantityDeducted(request.getQuantity());

     response.setAllocations(allocations);

     return response;

   }

   /** 

    * Convert Batch entity to BatchResponse DTO 

    */

   private BatchResponse convertToBatchResponse(Batch batch) {

     BatchResponse response = new BatchResponse();

     response.setId(batch.getId());

     response.setBatchNumber(batch.getBatchNumber());

     response.setQuantity(batch.getQuantity());

     response.setExpiryDate(batch.getExpiryDate());

     response.setReceivedDate(batch.getReceivedDate());

     response.setProductCode(batch.getProduct().getProductCode());

     response.setProductName(batch.getProduct().getName());

     return response;

   }

   /** 

    * Get all products with their batches (for testing/viewing data) 

    */

   public List < Object > getAllProducts() {

     return productRepository.findAll().stream()

       .map(product -> {

         List < BatchResponse > productBatches = batchRepository

         .findByProductProductCodeOrderByExpiryDateAsc(product.getProductCode())

         .stream()

         .map(this::convertToBatchResponse)

         .collect(Collectors.toList());

         return new Object() {

           public final String productCode = product.getProductCode();

           public final String productName = product.getName();

           public final String description = product.getDescription();

           public final List < BatchResponse > batches = productBatches;

         };

       })

       .collect(Collectors.toList());

   }

 }