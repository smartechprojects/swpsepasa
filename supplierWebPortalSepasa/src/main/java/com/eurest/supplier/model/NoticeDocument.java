package com.eurest.supplier.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Entity
@Table(name="noticedocument")
public class NoticeDocument {

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String name;
	private String idNotice;
	private String createdBy;
	private String documentType;
	private long size;
	private String type;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date uploadDate;
	
	@Lob @Basic(fetch = FetchType.LAZY)
	private byte[] content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getIdNotice() {
		return idNotice;
	}

	public void setIdNotice(String idNotice) {
		this.idNotice = idNotice;
	}
	
	
}
