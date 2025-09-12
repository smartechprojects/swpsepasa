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

@Entity
@Table(name = "noticedetail")
public class NoticeDetail {

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String idNotice;
	
	private String addresNumber;
	
	private String razonSocial;
	
	private Boolean required;
	
	private Boolean attachment;
	
	private Boolean enabled;
	
	private String status;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date fecha;

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

	public String getAddresNumber() {
		return addresNumber;
	}

	public void setAddresNumber(String addresNumber) {
		this.addresNumber = addresNumber;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getAttachment() {
		return attachment;
	}

	public void setAttachment(Boolean attachment) {
		this.attachment = attachment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	
}
