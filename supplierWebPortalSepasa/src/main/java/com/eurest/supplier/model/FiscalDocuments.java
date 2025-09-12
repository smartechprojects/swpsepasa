package com.eurest.supplier.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;


@JsonAutoDetect
@Entity
@Table(name="fiscaldocuments")
public class FiscalDocuments {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;

	private String folio; 
	private String serie;
	private String uuidFactura;
	private String folioPago; 
	private String seriePago;
	private String uuidPago;
	private String folioNC; 
	private String serieNC;
	private String uuidNotaCredito;
	private String type;
	private String addressNumber;
	private String supplierName;
	private double amount; 
	private double subtotal;
	private double descuento;
	private double impuestos;
	private double tipoCambio;
	private String rfcEmisor; 
	private String rfcReceptor; 
	private String moneda;	
	private String centroCostos;
	private String conceptoArticulo;
	private String companyFD;	
	private String invoiceDate; 	
	private String status;
	private String noteRejected;
	private String orderCompany; 
	private int orderNumber; 
	private String orderType;
	private String currencyMode;
	private String currencyCode;	
	private String paymentTerms;	
	private String accountingAccount;
	private String accountNumber;
	private String glOffset;
	private String taxCode;
	private String currentApprover;
	private String nextApprover;
	private String approvalStatus;
	private String approvalStep;
	private double advancePayment;
	private double conceptTotalAmount;
	private String paymentStatus;
	private Date paymentDate;
	private String paymentReference;
	private double paymentAmount;	
	private String complPagoUuid;
	private String emailApprover;
	private String businessLine;
	private String zonaTpimp;
	private String formaPago;
	private String metodoPago;
	private String invoiceType;
	private String responsibleUser1;
	private String notes;

	private Date creditNoteUploadDate; // Date requested
	private Date transferDate; // Original promise delivery
	private String transferStatus;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date invoiceUploadDate;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date estimatedPaymentDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date portalPaymentDate;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date paymentUploadDate; // Date requested	
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date approvalDate;
	
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	Set<FiscalDocumentsConcept> concepts;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getUuidFactura() {
		return uuidFactura;
	}
	public void setUuidFactura(String uuidFactura) {
		this.uuidFactura = uuidFactura;
	}
	public String getFolioPago() {
		return folioPago;
	}
	public void setFolioPago(String folioPago) {
		this.folioPago = folioPago;
	}
	public String getSeriePago() {
		return seriePago;
	}
	public void setSeriePago(String seriePago) {
		this.seriePago = seriePago;
	}
	public String getUuidPago() {
		return uuidPago;
	}
	public void setUuidPago(String uuidPago) {
		this.uuidPago = uuidPago;
	}
	public String getFolioNC() {
		return folioNC;
	}
	public void setFolioNC(String folioNC) {
		this.folioNC = folioNC;
	}
	public String getSerieNC() {
		return serieNC;
	}
	public void setSerieNC(String serieNC) {
		this.serieNC = serieNC;
	}
	public String getUuidNotaCredito() {
		return uuidNotaCredito;
	}
	public void setUuidNotaCredito(String uuidNotaCredito) {
		this.uuidNotaCredito = uuidNotaCredito;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAddressNumber() {
		return addressNumber;
	}
	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}
	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	public double getDescuento() {
		return descuento;
	}
	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	public double getImpuestos() {
		return impuestos;
	}
	public void setImpuestos(double impuestos) {
		this.impuestos = impuestos;
	}
	public double getTipoCambio() {
		return tipoCambio;
	}
	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}
	public String getRfcEmisor() {
		return rfcEmisor;
	}
	public void setRfcEmisor(String rfcEmisor) {
		this.rfcEmisor = rfcEmisor;
	}
	public String getRfcReceptor() {
		return rfcReceptor;
	}
	public void setRfcReceptor(String rfcReceptor) {
		this.rfcReceptor = rfcReceptor;
	}
	public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	public String getCentroCostos() {
		return centroCostos;
	}
	public void setCentroCostos(String centroCostos) {
		this.centroCostos = centroCostos;
	}
	public String getConceptoArticulo() {
		return conceptoArticulo;
	}
	public void setConceptoArticulo(String conceptoArticulo) {
		this.conceptoArticulo = conceptoArticulo;
	}
	public String getCompanyFD() {
		return companyFD;
	}
	public void setCompanyFD(String companyFD) {
		this.companyFD = companyFD;
	}
	public String getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNoteRejected() {
		return noteRejected;
	}
	public void setNoteRejected(String noteRejected) {
		this.noteRejected = noteRejected;
	}
	public String getOrderCompany() {
		return orderCompany;
	}
	public void setOrderCompany(String orderCompany) {
		this.orderCompany = orderCompany;
	}
	public int getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getCurrencyMode() {
		return currencyMode;
	}
	public void setCurrencyMode(String currencyMode) {
		this.currencyMode = currencyMode;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getPaymentTerms() {
		return paymentTerms;
	}
	public void setPaymentTerms(String paymentTerms) {
		this.paymentTerms = paymentTerms;
	}
	public String getAccountingAccount() {
		return accountingAccount;
	}
	public void setAccountingAccount(String accountingAccount) {
		this.accountingAccount = accountingAccount;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getGlOffset() {
		return glOffset;
	}
	public void setGlOffset(String glOffset) {
		this.glOffset = glOffset;
	}
	public String getTaxCode() {
		return taxCode;
	}
	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}
	public String getCurrentApprover() {
		return currentApprover;
	}
	public void setCurrentApprover(String currentApprover) {
		this.currentApprover = currentApprover;
	}
	public String getNextApprover() {
		return nextApprover;
	}
	public void setNextApprover(String nextApprover) {
		this.nextApprover = nextApprover;
	}
	public String getApprovalStatus() {
		return approvalStatus;
	}
	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
	public String getApprovalStep() {
		return approvalStep;
	}
	public void setApprovalStep(String approvalStep) {
		this.approvalStep = approvalStep;
	}
	public double getAdvancePayment() {
		return advancePayment;
	}
	public void setAdvancePayment(double advancePayment) {
		this.advancePayment = advancePayment;
	}
	public double getConceptTotalAmount() {
		return conceptTotalAmount;
	}
	public void setConceptTotalAmount(double conceptTotalAmount) {
		this.conceptTotalAmount = conceptTotalAmount;
	}
	public Date getInvoiceUploadDate() {
		return invoiceUploadDate;
	}
	public void setInvoiceUploadDate(Date invoiceUploadDate) {
		this.invoiceUploadDate = invoiceUploadDate;
	}
	public Date getEstimatedPaymentDate() {
		return estimatedPaymentDate;
	}
	public void setEstimatedPaymentDate(Date estimatedPaymentDate) {
		this.estimatedPaymentDate = estimatedPaymentDate;
	}
	public Set<FiscalDocumentsConcept> getConcepts() {
		return concepts;
	}
	public void setConcepts(Set<FiscalDocumentsConcept> concepts) {
		this.concepts = concepts;
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
	public String getComplPagoUuid() {
		return complPagoUuid;
	}
	public void setComplPagoUuid(String complPagoUuid) {
		this.complPagoUuid = complPagoUuid;
	}
	public Date getPortalPaymentDate() {
		return portalPaymentDate;
	}
	public void setPortalPaymentDate(Date portalPaymentDate) {
		this.portalPaymentDate = portalPaymentDate;
	}
	public String getEmailApprover() {
		return emailApprover;
	}
	public void setEmailApprover(String emailApprover) {
		this.emailApprover = emailApprover;
	}	
	public String getZonaTpimp() {
		return zonaTpimp;
	}
	public void setZonaTpimp(String zonaTpimp) {
		this.zonaTpimp = zonaTpimp;
	}	
	
	public Date getPaymentUploadDate() {
		return paymentUploadDate;
	}
	public void setPaymentUploadDate(Date paymentUploadDate) {
		this.paymentUploadDate = paymentUploadDate;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}
	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}
	
	public String getBusinessLine() {
		return businessLine;
	}
	public void setBusinessLine(String businessLine) {
		this.businessLine = businessLine;
	}

	public Date getCreditNoteUploadDate() {
		return creditNoteUploadDate;
	}
	public void setCreditNoteUploadDate(Date creditNoteUploadDate) {
		this.creditNoteUploadDate = creditNoteUploadDate;
	}

	public Date getTransferDate() {
		return transferDate;
	}
	public void setTransferDate(Date transferDate) {
		this.transferDate = transferDate;
	}
	public String getTransferStatus() {
		return transferStatus;
	}
	public void setTransferStatus(String transferStatus) {
		this.transferStatus = transferStatus;
	}
	public String getFormaPago() {
		return formaPago;
	}
	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}
	public String getMetodoPago() {
		return metodoPago;
	}
	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}
	public String getInvoiceType() {
		return invoiceType;
	}
	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}
	public String getResponsibleUser1() {
		return responsibleUser1;
	}
	public void setResponsibleUser1(String responsibleUser1) {
		this.responsibleUser1 = responsibleUser1;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
}
