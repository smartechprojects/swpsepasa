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

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@JsonAutoDetect
@Entity
@Table(name="supplierDocument")
public class SupplierDocument {

	private static final long serialVersionUID = 1L;
	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	private String addressBook;
	
	@Lob @Basic(fetch = FetchType.LAZY)
	private byte[] content;
	
	private String description;
	
	private String documentType;
	
	private String name;
	
	private long size;
	
	private String type;
	
	@Temporal(TemporalType.DATE)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private Date uploadDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAddressBook() {
		return addressBook;
	}

	public void setAddressBook(String addressBook) {
		this.addressBook = addressBook;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
