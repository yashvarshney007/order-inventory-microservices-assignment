package com.yash.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yash.inventory.dto.BatchResponse;
import com.yash.inventory.dto.InventoryUpdateRequest;
import com.yash.inventory.dto.InventoryUpdateResponse;
import com.yash.inventory.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController

@RequestMapping("/inventory")

@Tag(name = "Inventory Management", description = "APIs for managing product inventory with batch tracking")

public class InventoryController {

  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {

    this.inventoryService = inventoryService;

  }

  /** 

   * GET /inventory/{productCode} - Get all batches for a product sorted by expiry date 

   *  

   * @param productCode Product code 

   * @return List of batches sorted by expiry date 

   */

  @Operation(summary = "Get inventory by product",

    description = "Retrieves all inventory batches for a specific product, sorted by expiry date (FEFO - First Expired First Out)")

  @ApiResponses(value = {

    @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory batches"),

    @ApiResponse(responseCode = "404", description = "Product not found"),

    @ApiResponse(responseCode = "500", description = "Internal server error")

  })

  @GetMapping("/{productCode}")

  public ResponseEntity <? > getInventoryByProduct(

    @Parameter(description = "Unique product code", required = true, example = "PROD-001")

    @PathVariable String productCode) {

    try {

      List< BatchResponse > batches = inventoryService.getBatchesByProduct(productCode);

      return ResponseEntity.ok(batches);

    } catch (IllegalArgumentException e) {

      return ResponseEntity.status(HttpStatus.NOT_FOUND)

        .body(new ErrorResponse(e.getMessage()));

    } catch (Exception e) {

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

        .body(new ErrorResponse("Internal server error: " + e.getMessage()));

    }

  }

  /** 

   * POST /inventory/update - Update inventory after order placement 

   *  

   * @param request Inventory update request 

   * @return Update response with allocation details 

   */

  @Operation(summary = "Update inventory",

    description = "Deducts inventory quantity using the configured allocation strategy (FIFO/FEFO). Returns batch allocation details.")

  @ApiResponses(value = {

    @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),

    @ApiResponse(responseCode = "400", description = "Invalid request or insufficient inventory"),

    @ApiResponse(responseCode = "500", description = "Internal server error")

  })

  @PostMapping("/update")

  public ResponseEntity <? > updateInventory(

    @io.swagger.v3.oas.annotations.parameters.RequestBody(

      description = "Inventory update request with product code, quantity, and optional strategy",

      required = true)

    @RequestBody InventoryUpdateRequest request) {

    try {

      InventoryUpdateResponse response = inventoryService.updateInventory(request);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)

        .body(new ErrorResponse(e.getMessage()));

    } catch (Exception e) {

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

        .body(new ErrorResponse("Internal server error: " + e.getMessage()));

    }

  }

  /** 

   * Error response DTO 

   */

  private record ErrorResponse(String error) {}

  /** 

   * GET /inventory/products - Get all products (for testing) 

   */

  @GetMapping("/products")

  public ResponseEntity <? > getAllProducts() {

    try {

      return ResponseEntity.ok(inventoryService.getAllProducts());

    } catch (Exception e) {

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

        .body(new ErrorResponse("Error: " + e.getMessage()));

    }

  }

}