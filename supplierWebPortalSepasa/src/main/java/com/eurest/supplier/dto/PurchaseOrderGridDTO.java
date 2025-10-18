package com.eurest.supplier.dto;

import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class PurchaseOrderGridDTO {
	private String orderCompany;
	private String addressNumber;
	private String longCompanyName;
	private String orderNumber;
	private String orderType;
	private String currecyCode;	
	private String invoiceNumber;
	private String transferDate;
	private String promiseDelivery;
	private String invoiceUuid;
	private String orderAmount;
	private String supplierEmail;
	private String paymentUuid; 
	private String paymentType;
	private String paymentDateStr;
	private String orderStauts;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date estPmtDate;
	private String estPmtDateStr;
	private int estPmtDateInt2; 
	private String folio;
	private String invoiceAmount;
	private String issuerRFC;
	private String receiverRFC;
	private int cancelAttempts;
	private String cancelStatus;	
	private String cancelRevisionNotes;	
	private String cancelRevisionDateStr;
	private String cancelRequestDateStr;
	private String cancelFinalDateStr;
	private String description;
	private String fiscalType;
	private String documentType;
	
	public String getOrderCompany() {
		return orderCompany;
	}
	public void setOrderCompany(String orderCompany) {
		this.orderCompany = orderCompany;
	}
	public String getAddressNumber() {
		return addressNumber;
	}
	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}
	public String getLongCompanyName() {
		return longCompanyName;
	}
	public void setLongCompanyName(String longCompanyName) {
		this.longCompanyName = longCompanyName;
	}
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getCurrecyCode() {
		return currecyCode;
	}
	public void setCurrecyCode(String currecyCode) {
		this.currecyCode = currecyCode;
	}
	public String getInvoiceNumber() {
		return invoiceNumber;
	}
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	public String getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(String transferDate) {
		this.transferDate = transferDate;
	}
	public String getPromiseDelivery() {
		return promiseDelivery;
	}
	public void setPromiseDelivery(String promiseDelivery) {
		this.promiseDelivery = promiseDelivery;
	}
	public String getInvoiceUuid() {
		return invoiceUuid;
	}
	public void setInvoiceUuid(String invoiceUuid) {
		this.invoiceUuid = invoiceUuid;
	}
	public String getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(String orderAmount) {
		this.orderAmount = orderAmount;
	}
	public String getSupplierEmail() {
		return supplierEmail;
	}
	public void setSupplierEmail(String supplierEmail) {
		this.supplierEmail = supplierEmail;
	}
	public String getPaymentUuid() {
		return paymentUuid;
	}
	public void setPaymentUuid(String paymentUuid) {
		this.paymentUuid = paymentUuid;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getPaymentDateStr() {
		return paymentDateStr;
	}
	public void setPaymentDateStr(String paymentDateStr) {
		this.paymentDateStr = paymentDateStr;
	}
	public String getOrderStauts() {
		return orderStauts;
	}
	public void setOrderStauts(String orderStauts) {
		this.orderStauts = orderStauts;
	}
	public Date getEstPmtDate() {
		return estPmtDate;
	}
	public void setEstPmtDate(Date estPmtDate) {
		this.estPmtDate = estPmtDate;
	}
	public String getEstPmtDateStr() {
		return estPmtDateStr;
	}
	public void setEstPmtDateStr(String estPmtDateStr) {
		this.estPmtDateStr = estPmtDateStr;
	}
	public String getFolio() {
		return folio;
	}
	public void setFolio(String folio) {
		this.folio = folio;
	}
	public String getInvoiceAmount() {
		return invoiceAmount;
	}
	public void setInvoiceAmount(String invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	public String getIssuerRFC() {
		return issuerRFC;
	}
	public void setIssuerRFC(String issuerRFC) {
		this.issuerRFC = issuerRFC;
	}
	public String getReceiverRFC() {
		return receiverRFC;
	}
	public void setReceiverRFC(String receiverRFC) {
		this.receiverRFC = receiverRFC;
	}
	public int getCancelAttempts() {
		return cancelAttempts;
	}
	public void setCancelAttempts(int cancelAttempts) {
		this.cancelAttempts = cancelAttempts;
	}
	public String getCancelStatus() {
		return cancelStatus;
	}
	public void setCancelStatus(String cancelStatus) {
		this.cancelStatus = cancelStatus;
	}
	public String getCancelRevisionNotes() {
		return cancelRevisionNotes;
	}
	public void setCancelRevisionNotes(String cancelRevisionNotes) {
		this.cancelRevisionNotes = cancelRevisionNotes;
	}
	public String getCancelRevisionDateStr() {
		return cancelRevisionDateStr;
	}
	public void setCancelRevisionDateStr(String cancelRevisionDateStr) {
		this.cancelRevisionDateStr = cancelRevisionDateStr;
	}
	public String getCancelRequestDateStr() {
		return cancelRequestDateStr;
	}
	public void setCancelRequestDateStr(String cancelRequestDateStr) {
		this.cancelRequestDateStr = cancelRequestDateStr;
	}
	public String getCancelFinalDateStr() {
		return cancelFinalDateStr;
	}
	public void setCancelFinalDateStr(String cancelFinalDateStr) {
		this.cancelFinalDateStr = cancelFinalDateStr;
	}
	
	public int getEstPmtDateInt2() {
		return estPmtDateInt2;
	}
	public void setEstPmtDateInt2(int estPmtDateInt2) {
		this.estPmtDateInt2 = estPmtDateInt2;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFiscalType() {
		return fiscalType;
	}
	public void setFiscalType(String fiscalType) {
		this.fiscalType = fiscalType;
	}
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	
}
