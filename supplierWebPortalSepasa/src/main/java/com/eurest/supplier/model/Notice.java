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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Entity
@Table(name="notice")
public class Notice {

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String idNotice;
	
	private String createdBy;
	
	private String noticeTitle;
	
	private String frequency;
	
	private String[] filters;
	
	@Column(length = 5000)
	private String[] suppliersNotice;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date noticeFromDate;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date noticeToDate;
	
	private Boolean required;
	
	private Boolean emailNotif;
	
	private Boolean docSupplier;
	
	private Boolean enabled;
	
	private String noticeFile;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date fechaCreacion;
	
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date fechaActualizacion;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdNotice() {
		return idNotice;
	}

	public void setIdNotice(String idNotice) {
		this.idNotice = idNotice;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getNoticeTitle() {
		return noticeTitle;
	}

	public void setNoticeTitle(String noticeTitle) {
		this.noticeTitle = noticeTitle;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
	}

	public String[] getSuppliersNotice() {
		return suppliersNotice;
	}

	public void setSuppliersNotice(String[] suppliersNotice) {
		this.suppliersNotice = suppliersNotice;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(Date fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getEmailNotif() {
		return emailNotif;
	}

	public void setEmailNotif(Boolean emailNotif) {
		this.emailNotif = emailNotif;
	}

	public Boolean getDocSupplier() {
		return docSupplier;
	}

	public void setDocSupplier(Boolean docSupplier) {
		this.docSupplier = docSupplier;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getNoticeFromDate() {
		return noticeFromDate;
	}

	public void setNoticeFromDate(Date noticeFromDate) {
		this.noticeFromDate = noticeFromDate;
	}

	public Date getNoticeToDate() {
		return noticeToDate;
	}

	public void setNoticeToDate(Date noticeToDate) {
		this.noticeToDate = noticeToDate;
	}

	public String getNoticeFile() {
		return noticeFile;
	}

	public void setNoticeFile(String noticeFile) {
		this.noticeFile = noticeFile;
	}
	
	
}
