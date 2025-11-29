package com.yash.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yash.order.client.InventoryClient;
import com.yash.order.dto.InventoryBatchResponse;
import com.yash.order.dto.InventoryUpdateRequest;
import com.yash.order.dto.InventoryUpdateResponse;
import com.yash.order.dto.OrderRequest;
import com.yash.order.dto.OrderResponse;
import com.yash.order.entity.Order;
import com.yash.order.entity.OrderItem;
import com.yash.order.exception.InsufficientInventoryException;
import com.yash.order.exception.OrderProcessingException;
import com.yash.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service

public class OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orderRepository;

	private final InventoryClient inventoryClient;

	public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient) {

		this.orderRepository = orderRepository;

		this.inventoryClient = inventoryClient;

	}

	/**
	 * 
	 *  * Process order - validate, create order, and update inventory
	 * 
	 *  *
	 * 
	 *  * @param request Order request
	 * 
	 *  * @return Order response
	 * 
	 *  
	 */

	@Transactional

	public OrderResponse processOrder(OrderRequest request) {

		// Validate request

		logger.info("firts");
		validateOrderRequest(request);

		logger.info("second");

		// Check inventory availability for all items

		for (OrderRequest.OrderItemRequest item : request.getItems()) {

			checkInventoryAvailability(item.getProductCode(), item.getQuantity());

		}

		logger.info("third");

		// Create order

		Order order = createOrder(request);

		// Update inventory for each item and capture batch allocations

		List<String> inventoryUpdates = new ArrayList<>();

		try {

			for (OrderItem item : order.getOrderItems()) {

				InventoryUpdateRequest updateRequest = new InventoryUpdateRequest(

						item.getProductCode(),

						item.getQuantity(),

						null // Use default strategy

				);

				InventoryUpdateResponse updateResponse = inventoryClient.updateInventory(updateRequest);

				inventoryUpdates.add(updateResponse.getMessage());

				// Store batch allocations in OrderItem

				if (updateResponse.getAllocations() != null && !updateResponse.getAllocations().isEmpty()) {

					String batchAllocations = buildBatchAllocationsString(updateResponse.getAllocations());

					item.setBatchAllocations(batchAllocations);

				}

			}

			// Mark order as confirmed

			order.setStatus(Order.OrderStatus.CONFIRMED);

			orderRepository.save(order);

		} catch (Exception e) {

			// If inventory update fails, mark order as failed

			order.setStatus(Order.OrderStatus.FAILED);

			orderRepository.save(order);

			throw new OrderProcessingException("Order created but inventory update failed: " + e.getMessage(), e);

		}

		// Build response

		return buildOrderResponse(order, "Order placed successfully");

	}

	/**
	 * 
	 *  * Validate order request
	 * 
	 *  
	 */

	private void validateOrderRequest(OrderRequest request) {

		if (request.getCustomerName() == null || request.getCustomerName().isEmpty()) {

			throw new OrderProcessingException("Customer name is required");

		}

		if (request.getItems() == null || request.getItems().isEmpty()) {

			throw new OrderProcessingException("Order must contain at least one item");

		}

		for (OrderRequest.OrderItemRequest item : request.getItems()) {

			if (item.getProductCode() == null || item.getProductCode().isEmpty()) {

				throw new OrderProcessingException("Product code is required for all items");

			}

			if (item.getQuantity() == null || item.getQuantity() <= 0) {

				throw new OrderProcessingException("Quantity must be greater than 0");

			}

		}

	}

	/**
	 * 
	 *  * Check inventory availability
	 * 
	 *  
	 */

	private void checkInventoryAvailability(String productCode, int quantityNeeded) {

		logger.info("checkInventoryAvailability Method start");
		try {

			List<InventoryBatchResponse> batches = inventoryClient.checkInventory(productCode);

			if (batches == null || batches.isEmpty()) {

				throw new OrderProcessingException("Product not found or no inventory available: " + productCode);

			}

			int totalAvailable = batches.stream()

					.mapToInt(InventoryBatchResponse::getQuantity)

					.sum();

			if (totalAvailable < quantityNeeded) {

				throw new InsufficientInventoryException(

						"Insufficient inventory for product " + productCode +

								". Available: " + totalAvailable + ", Needed: " + quantityNeeded);

			}

		} catch (InsufficientInventoryException e) {

			throw e;

		} catch (RuntimeException e) {

			throw new OrderProcessingException("Failed to check inventory: " + e.getMessage(), e);

		}

		logger.info("checkInventoryAvailability Method end");

	}

	/**
	 * 
	 *  * Create order entity
	 * 
	 *  
	 */

	private Order createOrder(OrderRequest request) {

		Order order = new Order();

		order.setOrderNumber(generateOrderNumber());

		order.setCustomerName(request.getCustomerName());

		order.setCustomerEmail(request.getCustomerEmail());

		order.setStatus(Order.OrderStatus.PENDING);

		order.setOrderDate(LocalDateTime.now());

		// Create order items

		List<OrderItem> orderItems = new ArrayList<>();

		double totalAmount = 0.0;

		for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {

			OrderItem orderItem = new OrderItem();

			orderItem.setOrder(order);

			orderItem.setProductCode(itemRequest.getProductCode());

			orderItem.setQuantity(itemRequest.getQuantity());

			// Fetch product name from inventory

			String productName = getProductName(itemRequest.getProductCode());

			orderItem.setProductName(productName);

			// Set prices

			double unitPrice = itemRequest.getUnitPrice() != null ? itemRequest.getUnitPrice() : 0.0;

			double itemTotal = unitPrice * itemRequest.getQuantity();

			orderItem.setUnitPrice(unitPrice);

			orderItem.setTotalPrice(itemTotal);

			totalAmount += itemTotal;

			orderItems.add(orderItem);

		}

		order.setOrderItems(orderItems);

		order.setTotalAmount(totalAmount);

		// Save order

		return orderRepository.save(order);

	}

	/**
	 * 
	 *  * Get product name from inventory service
	 * 
	 *  
	 */

	private String getProductName(String productCode) {

		try {

			List<InventoryBatchResponse> batches = inventoryClient.checkInventory(productCode);

			if (batches != null && !batches.isEmpty() && batches.get(0).getProductName() != null) {

				return batches.get(0).getProductName();

			}

			return productCode; // Fallback to product code if name not found

		} catch (Exception e) {

			return productCode; // Fallback to product code on error

		}

	}

	/**
	 * 
	 *  * Build order response
	 * 
	 *  
	 */

	private OrderResponse buildOrderResponse(Order order, String message) {

		List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()

				.map(item -> new OrderResponse.OrderItemResponse(

						item.getProductCode(),

						item.getProductName(),

						item.getQuantity(),

						item.getUnitPrice(),

						item.getTotalPrice(),

						item.getBatchAllocations()

				))

				.collect(Collectors.toList());

		return new OrderResponse(

				order.getId(),

				order.getOrderNumber(),

				order.getCustomerName(),

				order.getCustomerEmail(),

				order.getStatus().name(),

				order.getOrderDate(),

				order.getTotalAmount(),

				itemResponses,

				message

		);

	}

	/**
	 * 
	 *  * Generate unique order number
	 * 
	 *  
	 */

	private String generateOrderNumber() {

		return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

	}

	/**
	 * 
	 *  * Build batch allocations string from list
	 * 
	 *  
	 */

	private String buildBatchAllocationsString(List<InventoryUpdateResponse.BatchAllocation> allocations) {

		if (allocations == null || allocations.isEmpty()) {

			return null;

		}

		return allocations.stream()

				.map(a -> String.format("{\"batchNumber\":\"%s\",\"quantity\":%d}",

						a.getBatchNumber(), a.getQuantityAllocated()))

				.collect(Collectors.joining(",", "[", "]"));

	}

}