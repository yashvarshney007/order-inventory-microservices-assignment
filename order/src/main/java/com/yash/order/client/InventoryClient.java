package com.yash.order.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.yash.order.dto.InventoryBatchResponse;
import com.yash.order.dto.InventoryUpdateRequest;
import com.yash.order.dto.InventoryUpdateResponse;

/**
 * 
 *  * Client to communicate with Inventory Service
 * 
 *  
 */

@Component

public class InventoryClient {

	private final WebClient webClient;

	public InventoryClient(WebClient.Builder webClientBuilder,

			@Value("${inventory.service.url}") String inventoryServiceUrl) {

		this.webClient = webClientBuilder.baseUrl(inventoryServiceUrl).build();

	}

	/**
	 * 
	 *  * Check inventory availability for a product
	 * 
	 *  *
	 * 
	 *  * @param productCode Product code
	 * 
	 *  * @return List of available batches
	 * 
	 *  
	 */

	public List<InventoryBatchResponse> checkInventory(String productCode) {

		try {

			return webClient.get()

					.uri("/inventory/{productCode}", productCode)

					.retrieve()

					.bodyToMono(new ParameterizedTypeReference<List<InventoryBatchResponse>>() {
					})

					.block();

		} catch (WebClientResponseException e) {

			throw new RuntimeException("Failed to check inventory for product: " + productCode +

					". Error: " + e.getResponseBodyAsString(), e);

		}

	}

	/**
	 * 
	 *  * Update inventory after order placement
	 * 
	 *  *
	 * 
	 *  * @param request Inventory update request
	 * 
	 *  * @return Update response
	 * 
	 *  
	 */

	public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {

		try {

			return webClient.post()

					.uri("/inventory/update")

					.bodyValue(request)

					.retrieve()

					.bodyToMono(InventoryUpdateResponse.class)

					.block();

		} catch (WebClientResponseException e) {

			throw new RuntimeException("Failed to update inventory for product: " + request.getProductCode() +

					". Error: " + e.getResponseBodyAsString(), e);

		}

	}

}