package com.eurest.supplier.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@JsonAutoDetect
@Entity
@Table(name = "receiptinvoice")
public class ReceiptInvoice {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String addressNumber;
	private int documentNumber;
	private String documentType;
	private int receiptNumber;
	
	private double amountReceived;
	private double foreignAmountReceived;

	private String currencyCode;
	
	private Date estPmtDate;
	
	private String uuid;
	private String folio;
	private String serie;
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date invDate;

	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date uploadDate;
	
	private String paymentStatus;
	private Date paymentDate;
	private String paymentReference;
	private double paymentAmount;
	private int lineNumber;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}	
	public String getAddressNumber() {
		return addressNumber;
	}
	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}
	public int getDocumentNumber() {
		return documentNumber;
	}
	public void setDocumentNumber(int documentNumber) {
		this.documentNumber = documentNumber;
	}
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}	
	public int getReceiptNumber() {
		return receiptNumber;
	}
	public void setReceiptNumber(int receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
	public double getAmountReceived() {
		return amountReceived;
	}
	public void setAmountReceived(double amountReceived) {
		this.amountReceived = amountReceived;
	}
	public double getForeignAmountReceived() {
		return foreignAmountReceived;
	}
	public void setForeignAmountReceived(double foreignAmountReceived) {
		this.foreignAmountReceived = foreignAmountReceived;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public Date getEstPmtDate() {
		return estPmtDate;
	}
	public void setEstPmtDate(Date estPmtDate) {
		this.estPmtDate = estPmtDate;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public Date getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}
	public String getPaymentReference() {
		return paymentReference;
	}
	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}
	public double getPaymentAmount() {
		return paymentAmount;
	}
	public void setPaymentAmount(double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getFolio() {
		return folio;
	}
	public void setFolio(String folio) {
		this.folio = folio;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public Date getInvDate() {
		return invDate;
	}
	public void setInvDate(Date invDate) {
		this.invDate = invDate;
	}	
	public Date getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}	
	
}
