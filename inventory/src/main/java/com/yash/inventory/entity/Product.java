 package com.yash.inventory.entity;

 import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

 @Entity

 @Table(name = "products")

 public class Product {

   @Id

   @GeneratedValue(strategy = GenerationType.IDENTITY)

   private Long id;

   @Column(nullable = false, unique = true)

   private String productCode;

   @Column(nullable = false)

   private String name;

   private String description;

   @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

   private List < Batch > batches;

   public Long getId() {
	return id;
   }

   public void setId(Long id) {
	this.id = id;
   }

   public String getProductCode() {
	return productCode;
   }

   public void setProductCode(String productCode) {
	this.productCode = productCode;
   }

   public String getName() {
	return name;
   }

   public void setName(String name) {
	this.name = name;
   }

   public String getDescription() {
	return description;
   }

   public void setDescription(String description) {
	this.description = description;
   }

   public List<Batch> getBatches() {
	return batches;
   }

   public void setBatches(List<Batch> batches) {
	this.batches = batches;
   }

   public Product(Long id, String productCode, String name, String description, List<Batch> batches) {
	super();
	this.id = id;
	this.productCode = productCode;
	this.name = name;
	this.description = description;
	this.batches = batches;
   }

   public Product() {
	super();
	// TODO Auto-generated constructor stub
   }
   
   

 }