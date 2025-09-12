package com.eurest.supplier.dto;

public class NoticeDetailDTO {

	private String addresNumber;
	
	private String razonSocial;
	
	private String status;
	
	private String doc;
	
	private Boolean attachment;

	public String getAddresNumber() {
		return addresNumber;
	}

	public void setAddresNumber(String addresNumber) {
		this.addresNumber = addresNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public Boolean getAttachment() {
		return attachment;
	}

	public void setAttachment(Boolean attachment) {
		this.attachment = attachment;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}
	
	
}
