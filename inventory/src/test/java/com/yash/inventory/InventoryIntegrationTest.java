package com.yash.inventory;

import com.yash.inventory.dto.BatchResponse;

import com.yash.inventory.dto.InventoryUpdateRequest;

import com.yash.inventory.dto.InventoryUpdateResponse;

import com.yash.inventory.entity.Batch;

import com.yash.inventory.entity.Product;

import com.yash.inventory.repository.BatchRepository;

import com.yash.inventory.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpMethod;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@ActiveProfiles("test")

class InventoryIntegrationTest {

  @LocalServerPort

  private int port;

  @Autowired

  private TestRestTemplate restTemplate;

  @Autowired

  private ProductRepository productRepository;

  @Autowired

  private BatchRepository batchRepository;

  private String baseUrl;

  @BeforeEach

  void setUp() {

    baseUrl = "http://localhost:" + port + "/inventory";

    batchRepository.deleteAll();

    productRepository.deleteAll();

  }

  @Test

  void testGetInventoryByProduct_Success_SavedInDatabase() {

    Product product = new Product();

    product.setProductCode("PROD-INT-001");

    product.setName("Test Product");

    product.setDescription("Integration Test Product");

    product = productRepository.save(product);

    Batch batch1 = new Batch();

    batch1.setBatchNumber("BATCH-INT-001");

    batch1.setProduct(product);

    batch1.setQuantity(100);

    batch1.setExpiryDate(LocalDate.now().plusDays(30));

    batch1.setReceivedDate(LocalDate.now().minusDays(5));

    batchRepository.save(batch1);

    Batch batch2 = new Batch();

    batch2.setBatchNumber("BATCH-INT-002");

    batch2.setProduct(product);

    batch2.setQuantity(50);

    batch2.setExpiryDate(LocalDate.now().plusDays(15));

    batch2.setReceivedDate(LocalDate.now().minusDays(3));

    batchRepository.save(batch2);

    ResponseEntity < List < BatchResponse >> response = restTemplate.exchange(

      baseUrl + "/PROD-INT-001",

      HttpMethod.GET,

      null,

      new ParameterizedTypeReference < List < BatchResponse >> () {}

    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());

    assertEquals(2, response.getBody().size());

    BatchResponse firstBatch = response.getBody().get(0);

    BatchResponse secondBatch = response.getBody().get(1);

    assertEquals("BATCH-INT-002", firstBatch.getBatchNumber());

    assertEquals(50, firstBatch.getQuantity());

    assertEquals("Test Product", firstBatch.getProductName());

    assertEquals("BATCH-INT-001", secondBatch.getBatchNumber());

    assertEquals(100, secondBatch.getQuantity());

  }

  @Test

  void testGetInventoryByProduct_ProductNotFound_Returns500() {

    ResponseEntity < String > response = restTemplate.exchange(

      baseUrl + "/NON-EXISTENT",

      HttpMethod.GET,

      null,

      String.class

    );

    // Controller's catch(Exception) block returns 500 for ProductNotFoundException 

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    assertTrue(response.getBody().contains("Product not found") ||

      response.getBody().contains("NON-EXISTENT") ||

      response.getBody().contains("Internal server error"));

  }

  @Test

  void testUpdateInventory_FIFOStrategy_Success() {

    Product product = new Product();

    product.setProductCode("PROD-FIFO-001");

    product.setName("FIFO Test Product");

    product.setDescription("Test FIFO Strategy");

    product = productRepository.save(product);

    Batch batch1 = new Batch();

    batch1.setBatchNumber("BATCH-FIFO-001");

    batch1.setProduct(product);

    batch1.setQuantity(50);

    batch1.setExpiryDate(LocalDate.now().plusDays(60));

    batch1.setReceivedDate(LocalDate.now().minusDays(10));

    batch1 = batchRepository.save(batch1);

    Batch batch2 = new Batch();

    batch2.setBatchNumber("BATCH-FIFO-002");

    batch2.setProduct(product);

    batch2.setQuantity(50);

    batch2.setExpiryDate(LocalDate.now().plusDays(90));

    batch2.setReceivedDate(LocalDate.now().minusDays(5));

    batch2 = batchRepository.save(batch2);

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-FIFO-001");

    request.setQuantity(30);

    request.setStrategy("FIFO");

    ResponseEntity < InventoryUpdateResponse > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      InventoryUpdateResponse.class

    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());

    assertTrue(response.getBody().isSuccess());

    assertEquals(30, response.getBody().getQuantityDeducted());

    assertTrue(response.getBody().getMessage().contains("FIFO"));

    List < InventoryUpdateResponse.BatchAllocation > allocations = response.getBody().getAllocations();

    assertNotNull(allocations);

    assertEquals(1, allocations.size());

    assertEquals("BATCH-FIFO-001", allocations.get(0).getBatchNumber());

    assertEquals(30, allocations.get(0).getQuantityAllocated());

    Batch updatedBatch1 = batchRepository.findById(batch1.getId()).orElseThrow();

    Batch updatedBatch2 = batchRepository.findById(batch2.getId()).orElseThrow();

    assertEquals(20, updatedBatch1.getQuantity());

    assertEquals(50, updatedBatch2.getQuantity());

  }

  @Test

  void testUpdateInventory_FEFOStrategy_Success() {

    Product product = new Product();

    product.setProductCode("PROD-FEFO-001");

    product.setName("FEFO Test Product");

    product.setDescription("Test FEFO Strategy");

    product = productRepository.save(product);

    Batch batch1 = new Batch();

    batch1.setBatchNumber("BATCH-FEFO-001");

    batch1.setProduct(product);

    batch1.setQuantity(40);

    batch1.setExpiryDate(LocalDate.now().plusDays(20));

    batch1.setReceivedDate(LocalDate.now().minusDays(5));

    batch1 = batchRepository.save(batch1);

    Batch batch2 = new Batch();

    batch2.setBatchNumber("BATCH-FEFO-002");

    batch2.setProduct(product);

    batch2.setQuantity(60);

    batch2.setExpiryDate(LocalDate.now().plusDays(60));

    batch2.setReceivedDate(LocalDate.now().minusDays(5));

    batch2 = batchRepository.save(batch2);

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-FEFO-001");

    request.setQuantity(35);

    request.setStrategy("FEFO");

    ResponseEntity < InventoryUpdateResponse > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      InventoryUpdateResponse.class

    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());

    assertTrue(response.getBody().isSuccess());

    assertEquals(35, response.getBody().getQuantityDeducted());

    assertTrue(response.getBody().getMessage().contains("FEFO"));

    List < InventoryUpdateResponse.BatchAllocation > allocations = response.getBody().getAllocations();

    assertNotNull(allocations);

    assertEquals(1, allocations.size());

    assertEquals("BATCH-FEFO-001", allocations.get(0).getBatchNumber());

    assertEquals(35, allocations.get(0).getQuantityAllocated());

    Batch updatedBatch1 = batchRepository.findById(batch1.getId()).orElseThrow();

    Batch updatedBatch2 = batchRepository.findById(batch2.getId()).orElseThrow();

    assertEquals(5, updatedBatch1.getQuantity());

    assertEquals(60, updatedBatch2.getQuantity());

  }

  @Test

  void testUpdateInventory_MultipleBatchesAllocated() {

    Product product = new Product();

    product.setProductCode("PROD-MULTI-001");

    product.setName("Multi Batch Test");

    product.setDescription("Test Multiple Batch Allocation");

    product = productRepository.save(product);

    Batch batch1 = new Batch();

    batch1.setBatchNumber("BATCH-MULTI-001");

    batch1.setProduct(product);

    batch1.setQuantity(30);

    batch1.setExpiryDate(LocalDate.now().plusDays(15));

    batch1.setReceivedDate(LocalDate.now().minusDays(5));

    batch1 = batchRepository.save(batch1);

    Batch batch2 = new Batch();

    batch2.setBatchNumber("BATCH-MULTI-002");

    batch2.setProduct(product);

    batch2.setQuantity(40);

    batch2.setExpiryDate(LocalDate.now().plusDays(30));

    batch2.setReceivedDate(LocalDate.now().minusDays(3));

    batch2 = batchRepository.save(batch2);

    Batch batch3 = new Batch();

    batch3.setBatchNumber("BATCH-MULTI-003");

    batch3.setProduct(product);

    batch3.setQuantity(50);

    batch3.setExpiryDate(LocalDate.now().plusDays(45));

    batch3.setReceivedDate(LocalDate.now().minusDays(1));

    batch3 = batchRepository.save(batch3);

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-MULTI-001");

    request.setQuantity(80);

    request.setStrategy("FEFO");

    ResponseEntity < InventoryUpdateResponse > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      InventoryUpdateResponse.class

    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());

    assertTrue(response.getBody().isSuccess());

    assertEquals(80, response.getBody().getQuantityDeducted());

    List < InventoryUpdateResponse.BatchAllocation > allocations = response.getBody().getAllocations();

    assertNotNull(allocations);

    assertEquals(3, allocations.size());

    assertEquals("BATCH-MULTI-001", allocations.get(0).getBatchNumber());

    assertEquals(30, allocations.get(0).getQuantityAllocated());

    assertEquals("BATCH-MULTI-002", allocations.get(1).getBatchNumber());

    assertEquals(40, allocations.get(1).getQuantityAllocated());

    assertEquals("BATCH-MULTI-003", allocations.get(2).getBatchNumber());

    assertEquals(10, allocations.get(2).getQuantityAllocated());

    Batch updatedBatch1 = batchRepository.findById(batch1.getId()).orElseThrow();

    Batch updatedBatch2 = batchRepository.findById(batch2.getId()).orElseThrow();

    Batch updatedBatch3 = batchRepository.findById(batch3.getId()).orElseThrow();

    assertEquals(0, updatedBatch1.getQuantity());

    assertEquals(0, updatedBatch2.getQuantity());

    assertEquals(40, updatedBatch3.getQuantity());

  }

  @Test

  void testUpdateInventory_InsufficientInventory_Returns500() {

    Product product = new Product();

    product.setProductCode("PROD-INSUF-001");

    product.setName("Insufficient Test");

    product.setDescription("Test Insufficient Inventory");

    product = productRepository.save(product);

    Batch batch = new Batch();

    batch.setBatchNumber("BATCH-INSUF-001");

    batch.setProduct(product);

    batch.setQuantity(10);

    batch.setExpiryDate(LocalDate.now().plusDays(30));

    batch.setReceivedDate(LocalDate.now());

    batchRepository.save(batch);

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-INSUF-001");

    request.setQuantity(50);

    ResponseEntity < String > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      String.class

    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    assertTrue(response.getBody().contains("Insufficient inventory") ||

      response.getBody().contains("Internal server error"));

    List < Batch > batches = batchRepository.findAll();

    assertEquals(1, batches.size());

    assertEquals(10, batches.get(0).getQuantity());

  }

  @Test

  void testUpdateInventory_ProductNotFound_Returns500() {

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("NON-EXISTENT");

    request.setQuantity(10);

    ResponseEntity < String > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      String.class

    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    assertTrue(response.getBody().contains("Product not found") ||

      response.getBody().contains("Internal server error"));

  }

  @Test

  void testUpdateInventory_InvalidRequest_Returns400() {

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-001");

    request.setQuantity(null);

    ResponseEntity < String > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      String.class

    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    assertTrue(response.getBody().contains("Invalid request") ||

      response.getBody().contains("required"));

  }

  @Test

  void testUpdateInventory_ZeroQuantity_Returns400() {

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-001");

    request.setQuantity(0);

    ResponseEntity < String > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      String.class

    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

  }

  @Test

  void testUpdateInventory_NegativeQuantity_Returns400() {

    InventoryUpdateRequest request = new InventoryUpdateRequest();

    request.setProductCode("PROD-001");

    request.setQuantity(-10);

    ResponseEntity < String > response = restTemplate.postForEntity(

      baseUrl + "/update",

      request,

      String.class

    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

  }

  @Test

  void testGetAllProducts_ReturnsProductsWithBatches() {

    Product product1 = new Product();

    product1.setProductCode("PROD-LIST-001");

    product1.setName("List Test Product 1");

    product1.setDescription("First Product");

    product1 = productRepository.save(product1);

    Batch batch1 = new Batch();

    batch1.setBatchNumber("BATCH-LIST-001");

    batch1.setProduct(product1);

    batch1.setQuantity(100);

    batch1.setExpiryDate(LocalDate.now().plusDays(30));

    batch1.setReceivedDate(LocalDate.now());

    batchRepository.save(batch1);

    Product product2 = new Product();

    product2.setProductCode("PROD-LIST-002");

    product2.setName("List Test Product 2");

    product2.setDescription("Second Product");

    product2 = productRepository.save(product2);

    ResponseEntity < String > response = restTemplate.getForEntity(

      baseUrl + "/products",

      String.class

    );

    assertEquals(HttpStatus.OK, response.getStatusCode());

    assertNotNull(response.getBody());

    assertTrue(response.getBody().contains("PROD-LIST-001"));

    assertTrue(response.getBody().contains("PROD-LIST-002"));

    assertTrue(response.getBody().contains("List Test Product 1"));

  }

}