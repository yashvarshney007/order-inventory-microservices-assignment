 package com.yash.inventory.dto;

 import java.util.List;

 public class InventoryUpdateResponse {

   public boolean isSuccess() {
		return success;
	}



	public void setSuccess(boolean success) {
		this.success = success;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public String getProductCode() {
		return productCode;
	}



	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}



	public Integer getQuantityDeducted() {
		return quantityDeducted;
	}



	public void setQuantityDeducted(Integer quantityDeducted) {
		this.quantityDeducted = quantityDeducted;
	}



	public List<BatchAllocation> getAllocations() {
		return allocations;
	}



	public void setAllocations(List<BatchAllocation> allocations) {
		this.allocations = allocations;
	}



   public InventoryUpdateResponse(boolean success, String message, String productCode, Integer quantityDeducted,
			List<BatchAllocation> allocations) {
		super();
		this.success = success;
		this.message = message;
		this.productCode = productCode;
		this.quantityDeducted = quantityDeducted;
		this.allocations = allocations;
	}



   public InventoryUpdateResponse() {
	super();
	// TODO Auto-generated constructor stub
}



   private boolean success;

   private String message;

   private String productCode;

   private Integer quantityDeducted;

   private List < BatchAllocation > allocations;



   public static class BatchAllocation {

     private String batchNumber;
     
     private Integer quantityAllocated;
     
     public BatchAllocation(String batchNumber, Integer quantityAllocated) {
		super();
		this.batchNumber = batchNumber;
		this.quantityAllocated = quantityAllocated;
	}

	 

     public BatchAllocation() {
		super();
		// TODO Auto-generated constructor stub
	}



	 public String getBatchNumber() {
		return batchNumber;
	}

	 public void setBatchNumber(String batchNumber) {
		 this.batchNumber = batchNumber;
	 }

	 public Integer getQuantityAllocated() {
		 return quantityAllocated;
	 }

	 public void setQuantityAllocated(Integer quantityAllocated) {
		 this.quantityAllocated = quantityAllocated;
	 }

	 

   }

 }