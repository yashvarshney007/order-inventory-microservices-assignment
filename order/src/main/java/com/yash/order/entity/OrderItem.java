package com.yash.order.entity;

import jakarta.persistence.*;


@Entity

@Table(name = "order_items")

public class OrderItem {

  @Id

  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)

  @JoinColumn(name = "order_id", nullable = false)

  private Order order;

  @Column(nullable = false)

  private String productCode;

  private String productName;

  @Column(nullable = false)

  private Integer quantity;

  private Double unitPrice;

  private Double totalPrice;

  @Column(length = 1000)

  private String batchAllocations; // JSON string: [{"batchNumber":"BATCH001","quantity":5}] 

  public Long getId() {
	return id;
  }

  public void setId(Long id) {
	this.id = id;
  }

  public Order getOrder() {
	return order;
  }

  public void setOrder(Order order) {
	this.order = order;
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

  public Integer getQuantity() {
	return quantity;
  }

  public void setQuantity(Integer quantity) {
	this.quantity = quantity;
  }

  public Double getUnitPrice() {
	return unitPrice;
  }

  public void setUnitPrice(Double unitPrice) {
	this.unitPrice = unitPrice;
  }

  public Double getTotalPrice() {
	return totalPrice;
  }

  public void setTotalPrice(Double totalPrice) {
	this.totalPrice = totalPrice;
  }

  public String getBatchAllocations() {
	return batchAllocations;
  }

  public void setBatchAllocations(String batchAllocations) {
	this.batchAllocations = batchAllocations;
  }

  public OrderItem(Long id, Order order, String productCode, String productName, Integer quantity, Double unitPrice,
		Double totalPrice, String batchAllocations) {
	super();
	this.id = id;
	this.order = order;
	this.productCode = productCode;
	this.productName = productName;
	this.quantity = quantity;
	this.unitPrice = unitPrice;
	this.totalPrice = totalPrice;
	this.batchAllocations = batchAllocations;
  }

  public OrderItem() {
	super();
	// TODO Auto-generated constructor stub
  }
  
  

}