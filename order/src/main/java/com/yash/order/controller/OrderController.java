package com.yash.order.controller;

import com.yash.order.dto.OrderRequest;

import com.yash.order.dto.OrderResponse;

import com.yash.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.responses.ApiResponses;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController

@RequestMapping("/order")

@Tag(name = "Order Management", description = "APIs for processing customer orders")

public class OrderController {

	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	private final OrderService orderService;

	public OrderController(OrderService orderService) {

		this.orderService = orderService;

	}

	/**
	 * 
	 *  * POST /order - Place a new order
	 * 
	 *  * 
	 * 
	 *  * @param request Order request with customer details and items
	 * 
	 *  * @return Order response with order details
	 * 
	 *  
	 */

	@Operation(summary = "Place a new order",

			description = "Creates a new order, validates inventory availability, and updates stock. " +

					"Communicates with Inventory Service to ensure sufficient stock before order confirmation.")

	@ApiResponses(value = {

			@ApiResponse(responseCode = "201", description = "Order created successfully"),

			@ApiResponse(responseCode = "400", description = "Invalid request or insufficient inventory"),

			@ApiResponse(responseCode = "500", description = "Order processing failed")

	})

	@PostMapping

	public ResponseEntity<?> placeOrder(

			@io.swagger.v3.oas.annotations.parameters.RequestBody(

					description = "Order request containing customer information and list of items to order",

					required = true)

			@RequestBody OrderRequest request) {

		try {

			logger.info("csk");
			OrderResponse response = orderService.processOrder(request);

			logger.info("csk1");

			return ResponseEntity.status(HttpStatus.CREATED).body(response);

		} catch (IllegalArgumentException e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)

					.body(new ErrorResponse(e.getMessage()));

		} catch (RuntimeException e) {

			System.out.println("++yashasha");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body(new ErrorResponse("Order processing failed: " + e.getMessage()));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

					.body(new ErrorResponse("Internal server error: " + e.getMessage()));

		}

	}

	/**
	 * 
	 *  * Error response DTO
	 * 
	 *  
	 */

	private record ErrorResponse(String error) {
	}

}