package com.yash.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



public class OrderResponse {

  private Long orderId;

  private String orderNumber;

  private String customerName;

  private String customerEmail;

  private String status;

  private LocalDateTime orderDate;

  private Double totalAmount;

  private List < OrderItemResponse > items;

  private String message;

 
  public Long getOrderId() {
	return orderId;
}


  public void setOrderId(Long orderId) {
	this.orderId = orderId;
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


  public String getStatus() {
	return status;
  }


  public void setStatus(String status) {
	this.status = status;
  }


  public LocalDateTime getOrderDate() {
	return orderDate;
  }


  public void setOrderDate(LocalDateTime orderDate) {
	this.orderDate = orderDate;
  }


  public Double getTotalAmount() {
	return totalAmount;
  }


  public void setTotalAmount(Double totalAmount) {
	this.totalAmount = totalAmount;
  }


  public List<OrderItemResponse> getItems() {
	return items;
  }


  public void setItems(List<OrderItemResponse> items) {
	this.items = items;
  }


  public String getMessage() {
	return message;
  }


  public void setMessage(String message) {
	this.message = message;
  }
  
  


  public OrderResponse(Long orderId, String orderNumber, String customerName, String customerEmail, String status,
		LocalDateTime orderDate, Double totalAmount, List<OrderItemResponse> items, String message) {
	super();
	this.orderId = orderId;
	this.orderNumber = orderNumber;
	this.customerName = customerName;
	this.customerEmail = customerEmail;
	this.status = status;
	this.orderDate = orderDate;
	this.totalAmount = totalAmount;
	this.items = items;
	this.message = message;
}
  
  




  public OrderResponse() {
	super();
	// TODO Auto-generated constructor stub
}






  public static class OrderItemResponse {

    private String productCode;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;

    private String batchAllocations;

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

	public OrderItemResponse(String productCode, String productName, Integer quantity, Double unitPrice,
			Double totalPrice, String batchAllocations) {
		super();
		this.productCode = productCode;
		this.productName = productName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalPrice = totalPrice;
		this.batchAllocations = batchAllocations;
	}

	public OrderItemResponse() {
		super();
		// TODO Auto-generated constructor stub
	} 
	
	

  }

}