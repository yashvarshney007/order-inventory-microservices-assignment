package com.yash.inventory.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity

@Table(name = "batches")
public class Batch {

	@Id

	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;

	@Column(nullable = false, unique = true)

	private String batchNumber;

	@ManyToOne(fetch = FetchType.LAZY)

	@JoinColumn(name = "product_id", nullable = false)

	private Product product;

	@Column(nullable = false)

	private Integer quantity;

	@Column(nullable = false)

	private LocalDate expiryDate;

	@Column(nullable = false)

	private LocalDate receivedDate;

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

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
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
	
	public Batch() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Batch(Long id, String batchNumber, Product product, Integer quantity, LocalDate expiryDate,
			LocalDate receivedDate) {
		super();
		this.id = id;
		this.batchNumber = batchNumber;
		this.product = product;
		this.quantity = quantity;
		this.expiryDate = expiryDate;
		this.receivedDate = receivedDate;
	}

	

}