package com.yash.order.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity

@Table(name = "orders")



public class Order {

  @Id

  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Long id;

  @Column(nullable = false, unique = true)

  private String orderNumber;

  @Column(nullable = false)

  private String customerName;

  private String customerEmail;

  @Column(nullable = false)

  @Enumerated(EnumType.STRING)

  private OrderStatus status;

  @Column(nullable = false)

  private LocalDateTime orderDate;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)

  private List < OrderItem > orderItems;

  private Double totalAmount;

  public enum OrderStatus {

    PENDING,

    CONFIRMED,

    FAILED,

    CANCELLED

  }

  public Long getId() {
	return id;
  }

  public void setId(Long id) {
	this.id = id;
  }

  public String getOrderNumber() {
	return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
	this.orderNumber = orderNumber;
  }

  public String getCustomerName() {
	return customerName;
  }

  public void setCustomerName(String customerName) {
	this.customerName = customerName;
  }

  public String getCustomerEmail() {
	return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
	this.customerEmail = customerEmail;
  }

  public OrderStatus getStatus() {
	return status;
  }

  public void setStatus(OrderStatus status) {
	this.status = status;
  }

  public LocalDateTime getOrderDate() {
	return orderDate;
  }

  public void setOrderDate(LocalDateTime orderDate) {
	this.orderDate = orderDate;
  }

  public List<OrderItem> getOrderItems() {
	return orderItems;
  }

  public void setOrderItems(List<OrderItem> orderItems) {
	this.orderItems = orderItems;
  }

  public Double getTotalAmount() {
	return totalAmount;
  }

  public void setTotalAmount(Double totalAmount) {
	this.totalAmount = totalAmount;
  }

  public Order(Long id, String orderNumber, String customerName, String customerEmail, OrderStatus status,
		LocalDateTime orderDate, List<OrderItem> orderItems, Double totalAmount) {
	super();
	this.id = id;
	this.orderNumber = orderNumber;
	this.customerName = customerName;
	this.customerEmail = customerEmail;
	this.status = status;
	this.orderDate = orderDate;
	this.orderItems = orderItems;
	this.totalAmount = totalAmount;
  }

  public Order() {
	super();
	// TODO Auto-generated constructor stub
  }
  
  

}