 package com.yash.inventory.dto;

public class InventoryUpdateRequest {

   public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

  

   public InventoryUpdateRequest(String productCode, Integer quantity, String strategy) {
	super();
	this.productCode = productCode;
	this.quantity = quantity;
	this.strategy = strategy;
}

   public InventoryUpdateRequest() {
	super();
	// TODO Auto-generated constructor stub
}

   private String productCode;
   
   private Integer quantity;

   private String strategy; 

 }