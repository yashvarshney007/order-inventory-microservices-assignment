package com.yash.order.dto;

import java.time.LocalDate;

/** 

 * DTO for Inventory Service batch response 

 */



public class InventoryBatchResponse {

  private Long id;

  private String batchNumber;

  private Integer quantity;

  private LocalDate expiryDate;

  private LocalDate receivedDate;

  private String productCode;

  private String productName;

  public Long getId() {
	return id;
  }

  public void setId(Long id) {
	this.id = id;
  }

  public String getBatchNumber() {
	return batchNumber;
  }

  public void setBatchNumber(String batchNumber) {
	this.batchNumber = batchNumber;
  }

  public Integer getQuantity() {
	return quantity;
  }

  public void setQuantity(Integer quantity) {
	this.quantity = quantity;
  }

  public LocalDate getExpiryDate() {
	return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
	this.expiryDate = expiryDate;
  }

  public LocalDate getReceivedDate() {
	return receivedDate;
  }

  public void setReceivedDate(LocalDate receivedDate) {
	this.receivedDate = receivedDate;
  }

  public String getProductCode() {
	return productCode;
  }

  public void setProductCode(String productCode) {
	this.productCode = productCode;
  }

  public String getProductName() {
	return productName;
  }

  public void setProductName(String productName) {
	this.productName = productName;
  }

  public InventoryBatchResponse(Long id, String batchNumber, Integer quantity, LocalDate expiryDate,
		LocalDate receivedDate, String productCode, String productName) {
	super();
	this.id = id;
	this.batchNumber = batchNumber;
	this.quantity = quantity;
	this.expiryDate = expiryDate;
	this.receivedDate = receivedDate;
	this.productCode = productCode;
	this.productName = productName;
  }

  public InventoryBatchResponse() {
	super();
	// TODO Auto-generated constructor stub
  }
  
  

}