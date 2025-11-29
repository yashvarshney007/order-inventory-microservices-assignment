package com.yash.order.dto;

import java.util.List;



public class OrderRequest {

  private String customerName;

  private String customerEmail;

  private List < OrderItemRequest > items;

  
  
 

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

  
  
  public List<OrderItemRequest> getItems() {
	return items;
  }





  public void setItems(List<OrderItemRequest> items) {
	this.items = items;
  }




  public OrderRequest(String customerName, String customerEmail, List<OrderItemRequest> items) {
	super();
	this.customerName = customerName;
	this.customerEmail = customerEmail;
	this.items = items;
}
  
  





  public OrderRequest() {
	super();
	// TODO Auto-generated constructor stub
}











  public static class OrderItemRequest {

    private String productCode;

    private Integer quantity;

    private Double unitPrice;

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

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public OrderItemRequest(String productCode, Integer quantity, Double unitPrice) {
		super();
		this.productCode = productCode;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}

	public OrderItemRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

  }

}