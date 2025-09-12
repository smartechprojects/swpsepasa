package com.eurest.supplier.dto;

public class NoticeDTO {
	
	private String id;
	
	private String idNotice;
	
	private String docNotice;
	
	private String noticeTitle;
	
	private Boolean required;
	
	private Boolean docSupplier;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIdNotice() {
		return idNotice;
	}

	public void setIdNotice(String idNotice) {
		this.idNotice = idNotice;
	}

	public String getDocNotice() {
		return docNotice;
	}

	public void setDocNotice(String docNotice) {
		this.docNotice = docNotice;
	}

	public String getNoticeTitle() {
		return noticeTitle;
	}

	public void setNoticeTitle(String noticeTitle) {
		this.noticeTitle = noticeTitle;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getDocSupplier() {
		return docSupplier;
	}

	public void setDocSupplier(Boolean docSupplier) {
		this.docSupplier = docSupplier;
	}
	
	
}
