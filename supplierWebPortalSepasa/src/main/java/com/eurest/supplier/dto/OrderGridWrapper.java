package com.eurest.supplier.dto;

import java.util.List;

public class OrderGridWrapper {
	
	List<PurchaseOrderGridDTO> orders;

	public List<PurchaseOrderGridDTO> getOrders() {
		return orders;
	}

	public void setOrders(List<PurchaseOrderGridDTO> orders) {
		this.orders = orders;
	}
	
	

}
