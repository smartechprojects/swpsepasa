package com.eurest.supplier.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.eurest.supplier.async.ProcessBatchInvoice;
import com.eurest.supplier.dao.DocumentsDao;
import com.eurest.supplier.dao.FiscalDocumentDao;
import com.eurest.supplier.dao.PurchaseOrderDao;
import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.dto.ForeingInvoice;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.dto.InvoiceRequestDTO;
import com.eurest.supplier.dto.UserDocumentDTO;
import com.eurest.supplier.dto.ZipElementDTO;
import com.eurest.supplier.invoiceXml.Concepto;
import com.eurest.supplier.invoiceXml.DoctoRelacionado;
import com.eurest.supplier.invoiceXml.Impuestos;
import com.eurest.supplier.invoiceXml.Pago;
import com.eurest.supplier.invoiceXml.Retencion;
import com.eurest.supplier.invoiceXml.Retenciones;
import com.eurest.supplier.invoiceXml.Traslado;
import com.eurest.supplier.invoiceXml.Traslados;
import com.eurest.supplier.invoiceXml.TrasladosLocales;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.ForeignInvoiceTable;
import com.eurest.supplier.model.NonComplianceSupplier;
import com.eurest.supplier.model.NoticeDocument;
import com.eurest.supplier.model.PaymentCalendar;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.PurchaseOrderDetail;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.SupplierDocument;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AESEncrypt;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.FileConceptUploadBean;
import com.eurest.supplier.util.FileUploadBean;
import com.eurest.supplier.util.Logger;
import com.eurest.supplier.util.NullValidator;
import com.eurest.supplier.util.PayloadProducer;
import com.eurest.supplier.util.StringUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Service("documentsService")
public class DocumentsService {
	
	 @Autowired
	 private JavaMailSender mailSenderObj;
	
	@Autowired
	private DocumentsDao documentsDao;

	@Autowired
	private PurchaseOrderDao purchaseOrderDao;
	
	@Autowired
	XmlToPojoService xmlToPojoService;
	
	@Autowired
	PurchaseOrderService purchaseOrderService;
	
	@Autowired
	UdcService udcService;
	
	@Autowired
	SupplierService supplierService;
	
	@Autowired
	SupplierDao supplierDao;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	EDIService eDIService;
	
	@Autowired
	UsersService usersService;
	
	@Autowired
	FiscalDocumentService fiscalDocumentService;
	
	@Autowired
	NonComplianceSupplierService nonComplianceSupplierService;
	
	@Autowired
	HTTPRequestService HTTPRequestService;
	
	@Autowired
	StringUtils stringUtils;
	
	@Autowired
	UDCDao udcDao;
	
	@Autowired
	PaymentCalendarService paymentCalendarService;
	
	@Autowired
	Logger logger;
	
	@Autowired
	OutSourcingService outSourcingService;
	
	@Autowired
	DataAuditService dataAuditService;
	
	@Autowired
	FiscalDocumentDao fiscalDocumentDao;
	
	static String TIMESTAMP_DATE_PATTERN = "yyyy-MM-dd";
	
	private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(DocumentsService.class);
	
	public List<UserDocument> getDocumentsList(int start, int limit) {
		return documentsDao.getDocumentsList(start, limit);
	}
	
	public List<UserDocument> searchCriteria(String query){
		return documentsDao.searchCriteria(query);
	}
		
	public List<UserDocument> searchByAddressNumber(String query){
		return documentsDao.searchByAddressNumber(query);
	}
	
	public void update(UserDocument documents, Date date, String user){
		documentsDao.updateDocuments(documents);
	}
	
	public void delete(int id, String usr){
		
		UserDocument doc = documentsDao.getDocumentById(id);
		if(doc != null) {
			logger.log(AppConstants.LOG_DELETE_DOC, AppConstants.LOG_DELDOC_MSG.replace("ORDER_NUMBER", String.valueOf(doc.getDocumentNumber() + "-" + doc.getDocumentType())).replace("DOC_NAME", doc.getName()).replace("USER_NAME", usr));
			documentsDao.deleteDocuments(id);
		}
	}
	
	public int getTotalRecords(){
		return documentsDao.getTotalRecords();
	}

	public UserDocument getDocumentById(int id) {
		return documentsDao.getDocumentById(id);
	}
	
	public List<UserDocument> searchCriteriaByOrderNumber(int orderNumber, 
												          String orderType, 
												          String addressNumber){ 
			return documentsDao.searchCriteriaByOrderNumber(orderNumber, 
					                                        orderType, 
					                                        addressNumber); 

	}
	

	public List<UserDocument> searchCriteriaByType( String orderType){
		return documentsDao.searchCriteriaByType(orderType);
	}
	
	public List<UserDocument> searchCriteriaByRefFiscal(String addresNumber, String uuid){
		return documentsDao.searchCriteriaByRefFiscal(addresNumber, uuid);
	}

	public List<UserDocument> searchCriteriaByDescription(String addressNumber, String description){
		return documentsDao.searchCriteriaByDescription(addressNumber, description);
	}
	
	public void save(UserDocument obj, Date date, String usr) {
		documentsDao.saveDocuments(obj);		
	}

	/*public void save(FileUploadBean uploadItem, Date date, String usr,
			int docNumber, String docType, String addressNumber) {
		UserDocument doc = new UserDocument();
		doc.setName(uploadItem.getFile().getOriginalFilename());
		doc.setType(uploadItem.getFile().getContentType());
		doc.setAccept(true);
		doc.setDescription("");
		doc.setFolio("");
		doc.setSerie("");
		doc.setFiscalRef(0);
		doc.setUuid("");
		doc.setUploadDate(new Date());
		doc.setDocumentNumber(Integer.valueOf(docNumber));
		doc.setDocumentType(docType);
		doc.setAddressBook(addressNumber);
		doc.setSize(uploadItem.getFile().getSize());
		doc.setContent(uploadItem.getFile().getBytes());
		UserDocument d = documentsDao.saveDocuments(doc);// TODO Auto-generated method stub
		
		}*/
		
		public void save(FileUploadBean uploadItem, Date date, String usr,
			int docNumber, String docType, String addressNumber) {
			
			//UserDocument doc = new UserDocument();
			SupplierDocument doc = new SupplierDocument();
			doc.setName(uploadItem.getFile().getOriginalFilename());
			doc.setType(uploadItem.getFile().getContentType());
			doc.setDescription(addressNumber.replace("NEW_", ""));
			doc.setUploadDate(new Date());
			doc.setDocumentType(docType);
			doc.setAddressBook(addressNumber);
			doc.setSize(uploadItem.getFile().getSize());
			doc.setContent(uploadItem.getFile().getBytes());
			SupplierDocument d = documentsDao.saveSupplierDocuments(doc);// TODO Auto-generated method stub
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
			
			DataAudit dataAudit = new DataAudit();
			dataAudit.setAction("UploadSupplierDocument");
			dataAudit.setAddressNumber(addressNumber);
			dataAudit.setCreationDate(new Date());
			dataAudit.setDocumentNumber(null);
			dataAudit.setIp(request.getRemoteAddr());
			dataAudit.setMethod("upload.action");
			dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
			dataAudit.setOrderNumber(null);
			dataAudit.setUuid(null);
			dataAudit.setStep("");
			dataAudit.setMessage("Upload Supplier Document Successful - RFC: " + addressNumber + " - File Name: " + uploadItem.getFile().getOriginalFilename());
			dataAudit.setNotes("");
			dataAudit.setStatus("COMPLETE");
			dataAudit.setUser(usr);
			
			dataAuditService.save(dataAudit);
			
			if(!addressNumber.contains("NEW")) {
				Supplier s = supplierService.searchByAddressNumber(addressNumber);
				String fileList =  s.getFileList();
				fileList = fileList + "_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate();
				s.setFileList(fileList);
				supplierDao.updateSupplier(s);
			}
		}
		
		public List<SupplierDocument> searchSupplierDocument(String query){
			return documentsDao.searchSupplierDocument(query);
		}
		
		public void updateSupplierDocument(SupplierDocument documents){
			documentsDao.updateSupplierDocument(documents);
		}
		
		public SupplierDocument getDocumentSuppById(int id) {
			return documentsDao.getDocumentSuppById(id);
		}
	
	public void saveDocNotice(MultipartFile uploadItem,  String docType, String addressNumber,String idNoticeDoc) throws IOException {
		NoticeDocument doc = new NoticeDocument();
		doc.setName(uploadItem.getOriginalFilename());
		doc.setType(uploadItem.getContentType());
		doc.setUploadDate(new Date());
		doc.setDocumentType(docType);
		doc.setCreatedBy(addressNumber);
		doc.setIdNotice(idNoticeDoc);
		doc.setSize(uploadItem.getSize());
		doc.setContent(uploadItem.getBytes());
		NoticeDocument d = documentsDao.saveNotice(doc);// TODO Auto-generated method stub
		
		/*if(!addressNumber.contains("NEW")) {
			Supplier s = supplierService.searchByAddressNumber(addressNumber);
			String fileList =  s.getFileList();
			fileList = fileList + "_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate();
			s.setFileList(fileList);
			supplierDao.updateSupplier(s);
		}*/
	}
	
	
	public void saveNotice(FileUploadBean uploadItem,  String docType, String addressNumber,String idNoticeDoc) {
		NoticeDocument doc = new NoticeDocument();
		doc.setName(uploadItem.getFile().getOriginalFilename());
		doc.setType(uploadItem.getFile().getContentType());
		doc.setUploadDate(new Date());
		doc.setDocumentType(docType);
		doc.setCreatedBy(addressNumber);
		doc.setIdNotice(idNoticeDoc);
		doc.setSize(uploadItem.getFile().getSize());
		doc.setContent(uploadItem.getFile().getBytes());
		NoticeDocument d = documentsDao.saveNotice(doc);// TODO Auto-generated method stub
		
		/*if(!addressNumber.contains("NEW")) {
			Supplier s = supplierService.searchByAddressNumber(addressNumber);
			String fileList =  s.getFileList();
			fileList = fileList + "_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate();
			s.setFileList(fileList);
			supplierDao.updateSupplier(s);
		}*/
	}
	
	@SuppressWarnings("unused")
	public void save(FileConceptUploadBean uploadItems, String addressNumber, String uuid) {
		String docType = null;
		
		CommonsMultipartFile item = null;
		for(int i=1; i < 53; i++) {
			item = null;
			docType = "";

			switch (i) {
			case 1:
				item = uploadItems.getFileConcept1_1();
				docType = AppConstants.CONCEPT_001;
				break;
			case 2:
				item = uploadItems.getFileConcept1_2();
				docType = AppConstants.CONCEPT_001;
				break;
			case 3:
				item = uploadItems.getFileConcept2_1();
				docType = AppConstants.CONCEPT_002;
				break;
			case 4:
				item = uploadItems.getFileConcept2_2();
				docType = AppConstants.CONCEPT_002;
				break;
			case 5:
				item = uploadItems.getFileConcept3_1();
				docType = AppConstants.CONCEPT_003;
				break;
			case 6:
				item = uploadItems.getFileConcept3_2();
				docType = AppConstants.CONCEPT_003;
				break;
			case 7:
				item = uploadItems.getFileConcept4_1();
				docType = AppConstants.CONCEPT_004;
				break;
			case 8:
				item = uploadItems.getFileConcept4_2();
				docType = AppConstants.CONCEPT_004;
				break;
			case 9:				
				item = uploadItems.getFileConcept5_1();
				docType = AppConstants.CONCEPT_005;
				break;
			case 10:				
				item = uploadItems.getFileConcept5_2();
				docType = AppConstants.CONCEPT_005;
				break;
			case 11:
				item = uploadItems.getFileConcept6_1();
				docType = AppConstants.CONCEPT_006;
				break;
			case 12:
				item = uploadItems.getFileConcept6_2();
				docType = AppConstants.CONCEPT_006;
				break;
			case 13:
				item = uploadItems.getFileConcept7_1();
				docType = AppConstants.CONCEPT_007;
				break;
			case 14:
				item = uploadItems.getFileConcept7_2();
				docType = AppConstants.CONCEPT_007;
				break;
			case 15:
				item = uploadItems.getFileConcept8_1();
				docType = AppConstants.CONCEPT_008;
				break;
			case 16:
				item = uploadItems.getFileConcept8_2();
				docType = AppConstants.CONCEPT_008;
				break;
			case 17:
				item = uploadItems.getFileConcept9_1();
				docType = AppConstants.CONCEPT_009;
				break;
			case 18:
				item = uploadItems.getFileConcept9_2();
				docType = AppConstants.CONCEPT_009;
				break;
			case 19:
				item = uploadItems.getFileConcept10_1();
				docType = AppConstants.CONCEPT_010;
				break;
			case 20:
				item = uploadItems.getFileConcept10_2();
				docType = AppConstants.CONCEPT_010;
				break;
			case 21:
				item = uploadItems.getFileConcept11_1();
				docType = AppConstants.CONCEPT_011;
				break;
			case 22:				
				item = uploadItems.getFileConcept11_2();
				docType = AppConstants.CONCEPT_011;
				break;
			case 23:				
				item = uploadItems.getFileConcept12_1();
				docType = AppConstants.CONCEPT_012;
				break;
			case 24:
				item = uploadItems.getFileConcept12_2();
				docType = AppConstants.CONCEPT_012;
				break;
			case 25:
				item = uploadItems.getFileConcept13_1();
				docType = AppConstants.CONCEPT_013;
				break;
			case 26:
				item = uploadItems.getFileConcept13_2();
				docType = AppConstants.CONCEPT_013;
				break;
			case 27:
				item = uploadItems.getFileConcept14_1();
				docType = AppConstants.CONCEPT_014;
				break;
			case 28:
				item = uploadItems.getFileConcept14_2();
				docType = AppConstants.CONCEPT_014;
				break;
			case 29:				
				item = uploadItems.getFileConcept15_1();
				docType = AppConstants.CONCEPT_015;
				break;
			case 30:				
				item = uploadItems.getFileConcept15_2();
				docType = AppConstants.CONCEPT_015;
				break;
			case 31:
				item = uploadItems.getFileConcept16_1();
				docType = AppConstants.CONCEPT_016;
				break;
			case 32:
				item = uploadItems.getFileConcept16_2();
				docType = AppConstants.CONCEPT_016;
				break;
			case 33:
				item = uploadItems.getFileConcept17_1();
				docType = AppConstants.CONCEPT_017;
				break;
			case 34:
				item = uploadItems.getFileConcept17_2();
				docType = AppConstants.CONCEPT_017;
				break;
			case 35:
				item = uploadItems.getFileConcept18_1();
				docType = AppConstants.CONCEPT_018;
				break;
			case 36:
				item = uploadItems.getFileConcept18_2();
				docType = AppConstants.CONCEPT_018;
				break;
			case 37:
				item = uploadItems.getFileConcept19_1();
				docType = AppConstants.CONCEPT_019;
				break;
			case 38:
				item = uploadItems.getFileConcept19_2();
				docType = AppConstants.CONCEPT_019;
				break;
			case 39:
				item = uploadItems.getFileConcept20_1();
				docType = AppConstants.CONCEPT_020;
				break;
			case 40:
				item = uploadItems.getFileConcept20_2();
				docType = AppConstants.CONCEPT_020;
				break;
			case 41:
				item = uploadItems.getFileConcept21_1();
				docType = AppConstants.CONCEPT_021;
				break;
			case 42:				
				item = uploadItems.getFileConcept21_2();
				docType = AppConstants.CONCEPT_021;
				break;
			case 43:				
				item = uploadItems.getFileConcept22_1();
				docType = AppConstants.CONCEPT_022;
				break;
			case 44:
				item = uploadItems.getFileConcept22_2();
				docType = AppConstants.CONCEPT_022;
				break;
			case 45:
				item = uploadItems.getFileConcept23_1();
				docType = AppConstants.CONCEPT_023;
				break;
			case 46:
				item = uploadItems.getFileConcept23_2();
				docType = AppConstants.CONCEPT_023;
				break;
			case 47:
				item = uploadItems.getFileConcept24_1();
				docType = AppConstants.CONCEPT_024;
				break;
			case 48:
				item = uploadItems.getFileConcept24_2();
				docType = AppConstants.CONCEPT_024;
				break;
			case 49:				
				item = uploadItems.getFileConcept25_1();
				docType = AppConstants.CONCEPT_025;
				break;
			case 50:				
				item = uploadItems.getFileConcept25_2();
				docType = AppConstants.CONCEPT_025;
				break;
			case 51:
				item = uploadItems.getFileConcept26_1();
				docType = AppConstants.CONCEPT_026;
				break;
			case 52:
				item = uploadItems.getFileConcept26_2();
				docType = AppConstants.CONCEPT_026;
				break;
			default:
				break;
			}
						
			if(item != null && item.getSize() > 0) {
				
				String folio = "";
				String serie = "";
				String conceptUUID = "";
				
				if(item.getContentType() != null && "text/xml".equals(item.getContentType().trim())) {
					InvoiceDTO inv = null;
					inv = this.getInvoiceXmlFromBytes(item.getBytes());
					
					if(inv != null) {						
						if(inv.getFolio() != null && !"null".equals(inv.getFolio()) && !"NULL".equals(inv.getFolio()) ) {
							folio = inv.getFolio();
						}
						if(inv.getSerie() != null && !"null".equals(inv.getSerie()) && !"NULL".equals(inv.getSerie()) ) {
							serie = inv.getSerie();
						}
						if(inv.getUuid() != null && !"null".equals(inv.getUuid()) && !"NULL".equals(inv.getUuid()) ) {
							conceptUUID = inv.getUuid();
						}						
					}
				}
				
				UserDocument doc = new UserDocument();
	    		doc.setAddressBook(addressNumber);
	    		doc.setDocumentNumber(Integer.valueOf(0));
	        	doc.setDocumentType(docType);
	        	doc.setContent(item.getBytes());
	        	doc.setType(item.getContentType());
	        	doc.setName(item.getOriginalFilename());
	        	doc.setSize(item.getSize());
	        	doc.setStatus(true);
	        	doc.setAccept(true);
	        	doc.setFiscalType("Otros");
	        	doc.setFolio(folio);
	        	doc.setSerie(serie);
	        	doc.setUuid(conceptUUID);
	        	doc.setUploadDate(new Date());
	        	doc.setFiscalRef(0);
	        	doc.setDescription("MainUUID_".concat(uuid));
				UserDocument d = documentsDao.saveDocuments(doc);
			}
		}
	}
	
@SuppressWarnings("unused")
public JSONObject processExcelFile(FileUploadBean uploadItem) {
		JSONObject json = new JSONObject();
		Workbook workbook = null;
		Sheet sheet = null;
		List<Supplier> suppliers = new ArrayList<Supplier>();
		List<Users> users = new ArrayList<Users>();
		List<DataAudit> dataAuditList = new ArrayList<DataAudit>();
		int count = 0;
		UDC userRole = udcService.searchBySystemAndKey("ROLES", "SUPPLIER");
		UDC userType = udcService.searchBySystemAndKey("USERTYPE", "SUPPLIER");
		//String encodePass =  AppConstants.START_PASS_ENCODED;
		
		Date currentDate = new Date();
	  	
	  	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String userCurrent = auth.getName();

		try {
		    workbook = WorkbookFactory.create(uploadItem.getFile().getInputStream());
			sheet = workbook.getSheet("WebPortal_Suppliers");
			DataFormatter formatter = new DataFormatter();
			Iterator<Row> rowIterator = sheet.iterator();
			int rowNumber = 1;
			while(rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if(row.getRowNum() > 0) {
					Supplier sup = new Supplier();
					Users usr = new Users();
					DataAudit dataAudit = new DataAudit();
					sup.setId(0);
					
				try {
					rowNumber++;
					if(row.getCell(0)!= null) {
						int valueAN = row.getCell(0).getCellType();
						 
						if(valueAN == 0) {
							int addNum = (int) row.getCell(0).getNumericCellValue();
							addNum = (addNum*100)/100;
							sup.setAddresNumber(String.valueOf(addNum));
						}else {
							if("".equals(row.getCell(0).getStringCellValue())) {
								continue;
							}
							int addNum = Integer.valueOf(row.getCell(0).getStringCellValue());
							addNum = (addNum*100)/100;
							sup.setAddresNumber(String.valueOf(addNum));
						}
						
						Supplier o = supplierService.searchByAddressNumber(sup.getAddresNumber());
						
						if(o != null) {
							if(o.getAddresNumber().equals("0") || o.getAddresNumber().equals("")) {
								json.put("success", false);
								json.put("message_es", "Address Number 0 o campo vacio no es valido");
								json.put("message_en", "Address Number 0 or empty field is not valid");
								json.put("count", 0);
								return json;
							}else {
								json.put("success", false);
								json.put("message_es", "Address Number existente en el portal: "+ o.getAddresNumber());
								json.put("message_en", "Address Number existing in the portal: "+ o.getAddresNumber());
								json.put("count", 0);
								return json;
							}
						}
					}else {
						continue;
					}
					

					//sup.setAddresNumber(row.getCell(0).getStringCellValue());
					//sup.setAddresNumber(String.valueOf(row.getCell(0).getNumericCellValue()));
					if(row.getCell(1)!= null) {
						sup.setName(row.getCell(1).getStringCellValue());
						if(row.getCell(1).getStringCellValue().length() > 40) {
							sup.setRazonSocial(row.getCell(1).getStringCellValue().substring(0, 40));
						} else {
							sup.setRazonSocial(row.getCell(1).getStringCellValue());
						}
					}
					
					if(row.getCell(2)!= null) {
						int fisicaM = row.getCell(2).getCellType();
						if(fisicaM == 0) {
							sup.setRfc(String.valueOf(row.getCell(2).getNumericCellValue()));
						}else {
							sup.setRfc(row.getCell(2).getStringCellValue());
						}
						
						if(sup.getRfc().length()>20) {
							json.put("success", false);
							json.put("message_es", "RFC/TAX ID excede el limite de 20 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "RFC/TAX ID exceeds the limit of 20 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
						
					}
					
					if(row.getCell(3)!= null) {
						sup.setCountry(formatter.formatCellValue(row.getCell(3)));
						if(sup.getCountry().length()>2) {
							json.put("success", false);
							json.put("message_es", "PAIS excede el limite de 2 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "COUNTRY exceeds the limit of 2 positions.\n\n<br><br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					//sup.setRfc(row.getCell(2).getStringCellValue());
					if(row.getCell(4)!= null)
						sup.setRegimenFiscal(formatter.formatCellValue(row.getCell(4)));
					
					if(row.getCell(5)!= null)
						sup.setCodigoTriburario(formatter.formatCellValue(row.getCell(5)));
					
					if(row.getCell(6)!= null)
						sup.setSearchType(formatter.formatCellValue(row.getCell(6)));
					
					if(row.getCell(7)!= null) {
						
						String tipoCon = formatter.formatCellValue(row.getCell(7));
						
						if("F".equals(tipoCon)){
							sup.setFisicaMoral("F");
						}else if("M".equals(tipoCon)){
							sup.setFisicaMoral("M");
						}else {
							json.put("success", false);
							json.put("message_es", "TIPO_CONTRIBUYENTE Solo permite el valor F y M <br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "TAXPAYER_TYPE Only allows the value F and M <br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
						
						/*int fisicaM = row.getCell(3).getCellType();
						if(fisicaM == 0) {
							int cP  = (int) row.getCell(3).getNumericCellValue();
							cP = (cP*100)/100;
							sup.setFisicaMoral(String.valueOf(cP));
						}else {
							int addNum = Integer.valueOf(row.getCell(3).getStringCellValue());
							addNum = (addNum*100)/100;
							sup.setFisicaMoral(String.valueOf(addNum));
						}
						
						if(sup.getFisicaMoral().length()>1) {
							json.put("success", false);
							json.put("message_es", "TIPO_CONTRIBUYENTE excede el limite de 1 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "TAXPAYER_TYPE exceeds the limit of 1 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}*/
					}
					
					if(row.getCell(8)!= null)
						sup.setCurp(formatter.formatCellValue(row.getCell(8)));
					
					if(row.getCell(9)!= null) {
						sup.setEmailSupplier(formatter.formatCellValue(row.getCell(9)));
						
						if(sup.getEmailSupplier().length()>254) {
							json.put("success", false);
							json.put("message_es", "EMAIL_SUPPLIER excede el limite de 254 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "EMAIL_SUPPLIER exceeds the limit of 254 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(10)!= null) {
						sup.setCalleNumero(formatter.formatCellValue(row.getCell(10)));
						if(sup.getCalleNumero().length()>40) {
							json.put("success", false);
							json.put("message_es", "CALLE Y NUMERO excede el limite de 40 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "STREET AND NUMBER exceeds the limit of 40 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(11)!= null) {
						sup.setColonia(formatter.formatCellValue(row.getCell(11)));
						if(sup.getColonia().length()>40) {
							json.put("success", false);
							json.put("message_es", "COLONIA excede el limite de 40 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "SUBURB exceeds the limit of 40 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(12)!= null) {
						sup.setCodigoPostal(formatter.formatCellValue(row.getCell(12)));
						
						if(sup.getCodigoPostal().length()>12) {
							json.put("success", false);
							json.put("message_es", "CODIGO_POSTAL excede el limite de 12 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "POSTAL CODE exceeds the limit of 12 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(13)!= null)
					sup.setDelegacionMnicipio(formatter.formatCellValue(row.getCell(13)));
					
					if(row.getCell(14)!= null) {
						sup.setEstado(formatter.formatCellValue(row.getCell(14)));
						if(sup.getEstado().length()>3) {
							json.put("success", false);
							json.put("message_es", "ESTADO excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "STATE exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(15)!= null) {
						sup.setTelefonoDF(formatter.formatCellValue(row.getCell(15)));
						
						if(sup.getTelefonoDF().length()>20) {
							json.put("success", false);
							json.put("message_es", "TELEFONO_DIREC_FIS excede el limite de 20 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "PHONE_ADDRESS_FIS exceeds the limit of 20 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(16)!= null) {
						sup.setFaxDF(formatter.formatCellValue(row.getCell(16)));
						
						if(sup.getFaxDF().length()>20) {
							json.put("success", false);
							json.put("message_es", "FAX excede el limite de 20 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "FAX exceeds the limit of 20 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
						
					//if(row.getCell(13)!= null)
					//sup.setEmailComprador(formatter.formatCellValue(row.getCell(0)));
					
					if(row.getCell(17)!= null)
					sup.setNombreContactoCxC(formatter.formatCellValue(row.getCell(17)));
					
					/*if(row.getCell(15)!= null)
					sup.setApellidoPaternoCxC(row.getCell(15).getStringCellValue());
					if(row.getCell(16)!= null)
					sup.setApellidoMaternoCxC(row.getCell(16).getStringCellValue());*/
					/*if(row.getCell(15)!= null) {
						sup.setTelefonoContactoCxC(formatter.formatCellValue(row.getCell(0)));
					}
					
					if(row.getCell(18)!= null) {
						int faxCxC = row.getCell(18).getCellType();
						if(faxCxC == 0) {
							sup.setFaxCxC(String.valueOf(row.getCell(18).getNumericCellValue()));
						}else {
							sup.setFaxCxC(row.getCell(18).getStringCellValue());
						}
					}
					if(row.getCell(16)!= null)
					sup.setCargoCxC(row.getCell(16).getStringCellValue()); */
					
					if(row.getCell(18)!= null)
						sup.setNombreCxP01(formatter.formatCellValue(row.getCell(18)));
					
					if(row.getCell(19)!= null)
						sup.setEmailCxP01(formatter.formatCellValue(row.getCell(19)));
					
					
					if(row.getCell(20)!= null)
						sup.setNombreCxP02(formatter.formatCellValue(row.getCell(20)));
					
					if(row.getCell(21)!= null)
						sup.setEmailCxP02(formatter.formatCellValue(row.getCell(21)));
					
					
					if(row.getCell(22)!= null)
						sup.setNombreCxP03(formatter.formatCellValue(row.getCell(22)));
					
					if(row.getCell(23)!= null)
						sup.setEmailCxP03(formatter.formatCellValue(row.getCell(23)));
					
					
					if(row.getCell(24)!= null)
						sup.setNombreCxP04(formatter.formatCellValue(row.getCell(24)));
					
					if(row.getCell(25)!= null)
						sup.setEmailCxP04(formatter.formatCellValue(row.getCell(25)));
					
					
					if(row.getCell(26)!= null)
						sup.setNombreCxP05(formatter.formatCellValue(row.getCell(26)));
					
					if(row.getCell(27)!= null)
						sup.setEmailCxP05(formatter.formatCellValue(row.getCell(27)));
					
					
					if(row.getCell(28)!= null)
						sup.setNombreCxP06(formatter.formatCellValue(row.getCell(28)));
					
					if(row.getCell(29)!= null)
						sup.setEmailCxP06(formatter.formatCellValue(row.getCell(29)));
					
					
					if(row.getCell(30)!= null)
						sup.setNombreCxP07(formatter.formatCellValue(row.getCell(30)));
					
					if(row.getCell(31)!= null)
						sup.setEmailCxP07(formatter.formatCellValue(row.getCell(31)));
					
					/*if(row.getCell(19)!= null) {
						int texCxP1 = row.getCell(19).getCellType();
						if(texCxP1 == 0) {
							sup.setTelefonoCxP01(String.valueOf(row.getCell(19).getNumericCellValue()));
						}else {
							sup.setTelefonoCxP01(row.getCell(19).getStringCellValue());
						}
					}*/
					
					
					
					/*if(row.getCell(22)!= null) {
						int texCxP2 = row.getCell(22).getCellType();
						if(texCxP2 == 0) {
							sup.setTelefonoCxP02(String.valueOf(row.getCell(22).getNumericCellValue()));
						}else {
							sup.setTelefonoCxP02(row.getCell(22).getStringCellValue());
						}
					}
					if(row.getCell(23)!= null) {
						int cat15 = row.getCell(23).getCellType();
						if(cat15 == 0) {
							sup.setCatCode15(String.valueOf(row.getCell(23).getNumericCellValue()));
						}else {
							sup.setCatCode15(row.getCell(23).getStringCellValue());
						}
						
						if(sup.getCatCode15().length()>3) {
							json.put("success", false);
							json.put("message_es", "CAT_CODE_25 excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CAT_CODE_25 exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(24)!= null) {
						int cat15 = row.getCell(24).getCellType();
						if(cat15 == 0) {
							sup.setCatCode27(String.valueOf(row.getCell(24).getNumericCellValue()));
						}else {
							sup.setCatCode27(row.getCell(24).getStringCellValue());
						}
						
						if(sup.getCatCode27().length()>3) {
							json.put("success", false);
							json.put("message_es", "CAT_CODE_29 excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CAT_CODE_29 exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(25)!= null) {
						int cat15 = row.getCell(25).getCellType();
						if(cat15 == 0) {
							sup.setIndustryClass(String.valueOf(row.getCell(25).getNumericCellValue()));
						}else {
							sup.setIndustryClass(row.getCell(25).getStringCellValue());
						}
						
						if(sup.getIndustryClass().length()>10) {
							json.put("success", false);
							json.put("message_es", "CODIGO_IMPUESTO excede el limite de 10 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CODIGO_IMPUESTO exceeds the limit of 10 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(26)!= null) {
						int cat15 = row.getCell(26).getCellType();
						if(cat15 == 0) {
							sup.setGlClass(String.valueOf(row.getCell(26).getNumericCellValue()));
						}else {
							sup.setGlClass(row.getCell(26).getStringCellValue());
						}
						
						if(sup.getGlClass().length()>4) {
							json.put("success", false);
							json.put("message_es", "GL_CLASS excede el limite de 4 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "GL_CLASS exceeds the limit of 4 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(27)!= null) {
						int cat15 = row.getCell(27).getCellType();
						if(cat15 == 0) {
							sup.setPmtTrmCxC(String.valueOf(row.getCell(27).getNumericCellValue()));
						}else {
							sup.setPmtTrmCxC(row.getCell(27).getStringCellValue());
						}
						
						if(sup.getPmtTrmCxC().length()>3) {
							json.put("success", false);
							json.put("message_es", "TERMINO_PAGO excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "TERMINO_PAGO exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					/*if(row.getCell(27)!= null) {
						int indCl = row.getCell(27).getCellType();
						if(indCl == 0) {
							int addNum = (int) row.getCell(27).getNumericCellValue();
							addNum = (addNum*100)/100;
							sup.setIndustryClass(String.valueOf(addNum));
						}else {
							if(!"".equals(row.getCell(27).getStringCellValue())) {
								int addNum = Integer.valueOf(row.getCell(27).getStringCellValue());
								addNum = (addNum*100)/100;
								sup.setIndustryClass(String.valueOf(addNum));
							}else sup.setIndustryClass(String.valueOf(row.getCell(27).getStringCellValue()));
							
						}
						
						if(sup.getIndustryClass().length()>3) {
							json.put("success", false);
							json.put("message_es", "CAT_MATERIALES_SERVICIOS excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CAT_MATERIALS_SERVICES exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}*/
					
					if(row.getCell(32)!= null) {
						sup.setTipoIdentificacion(formatter.formatCellValue(row.getCell(32)));
						
					}
					if(row.getCell(33)!= null) {
						sup.setNumeroIdentificacion(formatter.formatCellValue(row.getCell(33)));
						
					}
					if(row.getCell(34)!= null)
					sup.setNombreRL(formatter.formatCellValue(row.getCell(34)));
					
					if(row.getCell(35)!= null)
					sup.setApellidoPaternoRL(formatter.formatCellValue(row.getCell(35)));
					
					if(row.getCell(36)!= null)
					sup.setApellidoMaternoRL(formatter.formatCellValue(row.getCell(36)));
					
					if(row.getCell(37)!= null) {
						sup.setSwiftCode(formatter.formatCellValue(row.getCell(37)));
						if(sup.getSwiftCode().length()>15) {
							json.put("success", false);
							json.put("message_es", "SWIFT_CODE excede el limite de 15 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "SWIFT_CODE exceeds the limit of 15 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(38)!= null) {
						sup.setIbanCode(formatter.formatCellValue(row.getCell(38)));
						
						if(sup.getIbanCode().length()>34) {
							json.put("success", false);
							json.put("message_es", "IBAN_CODE excede el limite de 34 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "IBAN_CODE exceeds the limit of 34 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					//sup.setIbanCode(row.getCell(35).getStringCellValue());
					if(row.getCell(39)!= null) {
						sup.setBankTransitNumber(formatter.formatCellValue(row.getCell(39)));
						
						if(sup.getBankTransitNumber().length()>20) {
							json.put("success", false);
							json.put("message_es", "BANK_TRANSIT_NUMBER excede el limite de 20 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "BANK_TRANSIT_NUMBER exceeds the limit of 20 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(40)!= null) {
						sup.setCustBankAcct(formatter.formatCellValue(row.getCell(40)));
						if(sup.getCustBankAcct().length()>20) {
							json.put("success", false);
							json.put("message_es", "CUST_BANK_ACCT excede el limite de 20 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CUST_BANK_ACCT exceeds the limit of 20 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(41)!= null) {
						sup.setControlDigit(formatter.formatCellValue(row.getCell(41)));
						if(sup.getControlDigit().length()>2) {
							json.put("success", false);
							json.put("message_es", "CODIGO_PAIS excede el limite de 2 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CODIGO_PAIS exceeds the limit of 2 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(42)!= null) {
						sup.setDescription(formatter.formatCellValue(row.getCell(42)));
						if(sup.getDescription().length()>30) {
							json.put("success", false);
							json.put("message_es", "NOMBRE_BANCO excede el limite de 30 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "NOMBRE_BANCO exceeds the limit of 30 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(43)!= null) {
						sup.setCheckingOrSavingAccount(formatter.formatCellValue(row.getCell(43)));
						
						if(sup.getCheckingOrSavingAccount().length()>1) {
							json.put("success", false);
							json.put("message_es", "CHECKING_OR_SAVING excede el limite de 1 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CHECKING_OR_SAVING exceeds the limit of 1 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(44)!= null) {
						sup.setRollNumber(formatter.formatCellValue(row.getCell(44)));
						if(sup.getRollNumber().length()>18) {
							json.put("success", false);
							json.put("message_es", "ROLL_NUMBER excede el limite de 18 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "ROLL_NUMBER exceeds the limit of 18 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(45)!= null) {
						sup.setBankAddressNumber(formatter.formatCellValue(row.getCell(45)));
						
						if(sup.getBankAddressNumber().length()>8) {
							json.put("success", false);
							json.put("message_es", "BANK_ADDRESS_NUMBER excede el limite de 8 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "BANK_ADDRESS_NUMBER exceeds the limit of 8 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					if(row.getCell(46)!= null) {
						sup.setBankCountryCode(formatter.formatCellValue(row.getCell(46)));
						if(sup.getBankCountryCode().length()>2) {
							json.put("success", false);
							json.put("message_es", "BANK_COUNTRY_CODE excede el limite de 2 posiciones.\nProveedor: "+ sup.getAddresNumber());
							json.put("message_en", "BANK_COUNTRY_CODE exceeds the limit of 2 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(47)!= null) {
						sup.setCurrencyCode(formatter.formatCellValue(row.getCell(47)));
						if(sup.getCurrencyCode().length()>3) {
							json.put("success", false);
							json.put("message_es", "MONEDA excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "CURRENCY exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(48)!= null) {
						sup.setGlClass(formatter.formatCellValue(row.getCell(48)));
						if(sup.getGlClass().length()>4) {
							json.put("success", false);
							json.put("message_es", "GL_Class excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "GL_Class exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(49)!= null) {
						sup.setPmtTrmCxC(formatter.formatCellValue(row.getCell(49)));
						if(sup.getPmtTrmCxC().length()>3) {
							json.put("success", false);
							json.put("message_es", "PMT_TRM excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "PMT_TRM exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(50)!= null) {
						sup.setTaxAreaCxC(formatter.formatCellValue(row.getCell(50)));
						if(sup.getTaxAreaCxC().length()>10) {
							json.put("success", false);
							json.put("message_es", "TAX_AREA excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "TAX_AREA exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					if(row.getCell(51)!= null) {
						sup.setPayInstCxC(formatter.formatCellValue(row.getCell(51)));
						if(sup.getPayInstCxC().length()>1) {
							json.put("success", false);
							json.put("message_es", "PAY_INST excede el limite de 3 posiciones.<br>Proveedor: "+ sup.getAddresNumber());
							json.put("message_en", "PAY_INST exceeds the limit of 3 positions.<br>Supplier:"+ sup.getAddresNumber());
							json.put("count", 0);
							return json;
						}
					}
					
					sup.setCurrentApprover("");
					sup.setNextApprover("");
					sup.setApprovalStatus(AppConstants.STATUS_ACCEPT);
					sup.setApprovalStep("");
					sup.setSteps(0);
					sup.setRejectNotes("");
					sup.setApprovalNotes("CARGA MASIVA");
					sup.setFechaSolicitud(null);
					sup.setFechaAprobacion(new Date());
					sup.setRegiones("");				
					sup.setTipoMovimiento("A");					
					sup.setRiesgoCategoria("");
					sup.setObservaciones("");
					sup.setDiasCreditoActual("0");
					sup.setDiasCreditoAnterior("0");
					sup.setAcceptOpenOrder(false);
					sup.setFileList("");
					//sup.setSearchType("V");
					sup.setCreditMessage("");
					sup.setTicketId(0l);
					/*sup.setInvException("N");
					sup.setTaxAreaCxC("");
					sup.setTaxExpl2CxC("");
					sup.setPayInstCxC("T");
					sup.setCurrCodeCxC("USD");					
					
					sup.setPmtTrmCxC("N60");
					if(sup.getCountry() != null && "MX".equals(sup.getCountry().toUpperCase().trim())) {
						sup.setGlClass("100");
					}else {
						sup.setGlClass("200");
					}
					if(sup.getFisicaMoral()!="1") {
						sup.setCatCode27("85");
					}else {
						sup.setCatCode27("03");
					}*/
					//if(sup.getCatCode15() == "0" || sup.getCatCode15() == null) sup.setCatCode15("");
					//if(sup.getIndustryClass() == "0" || sup.getIndustryClass() == null) sup.setIndustryClass("");
					
				    if(!"MX".equals(sup.getCountry())) {
				    	sup.setTaxId(sup.getRfc());	
				    	sup.setRfc(null);
				    }
				    
					suppliers.add(sup);
					
					usr.setId(0);
					usr.setUserName(sup.getAddresNumber());
					usr.setEnabled(true);
					usr.setEmail(sup.getEmailSupplier());
					usr.setName(sup.getRazonSocial());
					usr.setRole(userRole.getStrValue1());
					usr.setUserRole(userRole);
					usr.setUserType(userType);
					
					String yearTwoDigits = String.valueOf(LocalDate.now().getYear()).substring(2); // "25"
					String tempPass = "Sepasa." + yearTwoDigits;
					String encodePass = Base64.getEncoder().encodeToString(tempPass.trim().getBytes());
					encodePass = "==a20$" + encodePass; 
					
					usr.setPassword(encodePass); 
					users.add(usr);
					
					    dataAudit.setAction("UploadSupplierByLayout");
				    	dataAudit.setAddressNumber(sup.getAddresNumber());
				    	dataAudit.setCreationDate(currentDate);
				    	dataAudit.setDocumentNumber(null);
				    	dataAudit.setIp(request.getRemoteAddr());
				    	dataAudit.setMethod("processExcelFile");
				    	dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
				    	dataAudit.setOrderNumber(null);
				    	dataAudit.setUuid(null);
				    	dataAudit.setStep(null);
				    	dataAudit.setMessage("Upload Supplier Successful AddresNumber: " + sup.getAddresNumber());
				    	dataAudit.setNotes(null);
				    	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
				    	dataAudit.setUser(userCurrent);
				    	
				    	dataAuditList.add(dataAudit);	

					count = count + 1;
					
				}catch(Exception e) {
					json.put("success", false);
					json.put("message_es", "NUMERO DE FILA: " + rowNumber + " - RUNTIME ERROR: " + e.getMessage());
					json.put("message_en", "ROW NUM: " + rowNumber + " - RUNTIME ERROR: " + e.getMessage());
					json.put("count", 0);
					return json;
					
				}
					
				}
				
				
			}
			
			if(!suppliers.isEmpty()) {
				supplierService.saveSuppliers(suppliers);
				usersService.saveUsersList(users);
				dataAuditService.saveDataAudit(dataAuditList);
				for(Users s : users) {
					String emailRecipient = (s.getEmail());

					String pass = s.getPassword();
					pass = pass.replace("==a20$", "");
					byte[] decodedBytes = Base64.getDecoder().decode(pass);
					String decodedPass = new String(decodedBytes);
					
					
					String credentials = "Usuario: " + s.getUserName() + "<br />Contraseña: " + decodedPass + "<br />url: " + AppConstants.EMAIL_PORTAL_LINK ;
					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(AppConstants.EMAIL_INVOICE_SUBJECT, stringUtils.prepareEmailContent(AppConstants.EMAIL_MASS_SUPPLIER_NOTIFICATION + credentials), emailRecipient);
					emailAsyncSup.setMailSender(mailSenderObj);
					Thread emailThreadSup = new Thread(emailAsyncSup);
					//emailThreadSup.start();	
				}
				json.put("success", true);
				json.put("count", count);
				json.put("message", "");
				return json;
			}
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
		return null;
		
	}
		
	public InvoiceDTO getInvoiceXml(FileUploadBean uploadItem){
		try{
			ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
			String xmlContent = IOUtils.toString(stream, "UTF-8");			
			InvoiceDTO dto = null;
			
			if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				dto = xmlToPojoService.convertV4(xmlContent);
			} else {
				dto = xmlToPojoService.convert(xmlContent);
			}
			return dto;
		}catch(Exception e){
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
	}
	
	public InvoiceDTO getCreditNoteXml(FileUploadBean uploadItem){
		try{
			ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFileTwo().getBytes());
			String xmlContent = IOUtils.toString(stream, "UTF-8");			
			InvoiceDTO dto = null;
			
			if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				dto = xmlToPojoService.convertV4(xmlContent);
			} else {
				dto = xmlToPojoService.convert(xmlContent);
			}
			return dto;
		}catch(Exception e){
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
	}
	
	public InvoiceDTO getInvoiceXmlFromString(String xmlContent){
		try{
			InvoiceDTO dto = null;
			if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				dto = xmlToPojoService.convertV4(xmlContent);
			} else {
				dto = xmlToPojoService.convert(xmlContent);
			}
			return dto;
		}catch(Exception e){
			log4j.error("Exception" , e);
			return null;
		}
	}
	
	public InvoiceDTO getInvoiceXmlFromBytes(byte[] bytes){
		try{
			ByteArrayInputStream stream = new  ByteArrayInputStream(bytes);
			String xmlContent = IOUtils.toString(stream, "UTF-8");			
			InvoiceDTO dto = null;
			
			if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				dto = xmlToPojoService.convertV4(xmlContent);
			} else {
				dto = xmlToPojoService.convert(xmlContent);
			}
			return dto;
		}catch(Exception e){
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
	}
	
	public String validateInvZipFile(FileUploadBean uploadItem, 
									  BindingResult result, 
									  String addressBook, 
									  int documentNumber, 
									  String documentType,
									  String tipoComprobante,
									  String receiptIdList,
									  String usr) {

		DecimalFormat currencyFormat = new DecimalFormat("$#,###.###");
		Users user = usersService.getByUserName(usr);
		PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);		
		Supplier s = supplierService.searchByAddressNumber(addressBook);
		if(s == null) {
			return "El proveedor no existe en la base de datos.";
		}
		
		List<Receipt> requestedReceiptList = null;
		List<Receipt> receiptArray = null;
		
		if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)) {
			receiptArray= purchaseOrderService.getOrderReceipts(documentNumber, addressBook, documentType, "");
		} else {
			receiptArray= purchaseOrderService.getNegativeOrderReceipts(documentNumber, addressBook, documentType,"");
		}
		
		if(receiptArray != null) {
			String[] idList = receiptIdList.split(",");
			requestedReceiptList = new ArrayList<Receipt>();
			for(Receipt r : receiptArray) {
				if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
					requestedReceiptList.add(r);
				}
			}
			
			if(requestedReceiptList.isEmpty()) {
				return "No existen recibos por facturar.";
			}
		}else {
			return "No existen recibos por facturar.";
	    }

		//Valida que se corresponda el mismo recibo para todas las lÃ­neas seleccionadas.
		int originalReceipt = 0;
		for(Receipt r :requestedReceiptList) {
			if(originalReceipt == 0) {
				originalReceipt = r.getDocumentNumber();
			} else if (originalReceipt != r.getDocumentNumber()) {
				return "Las líneas seleccionadas deben corresponder al mismo número de recibo.";
			}
		}
		
		//Valida proveedores incumplidos
		if(s != null) {
			NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(s.getRfc(), 0, 0);
		    if (ncs != null && (
		      ncs.getRefDate1().contains("Definitivo") || 
		      ncs.getRefDate1().contains("Presunto") || 
		      ncs.getRefDate1().contains("Desvirtuado") || 
		      ncs.getRefDate2().contains("Definitivo") || 
		      ncs.getRefDate2().contains("Presunto") || 
		      ncs.getRefDate2().contains("Desvirtuado") || 
		      ncs.getStatus().contains("Definitivo") || 
		      ncs.getStatus().contains("Presunto") || 
		      ncs.getStatus().contains("Desvirtuado"))) {
		    	
		    	String altEmail = "";
					List<UDC> udcList =  udcService.searchBySystem("TAXALTEMAIL");
					if(udcList != null) {
						altEmail = udcList.get(0).getStrValue1();
					    EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						emailAsyncSup.setProperties(
								AppConstants.EMAIL_NO_COMPLIANCE_INVOICE,
								AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER + " Número: " + s.getAddresNumber() + "<br /> Nombre: " + s.getRazonSocial() + "<br />",
								altEmail);
						emailAsyncSup.setMailSender(mailSenderObj);
						Thread emailThreadSup = new Thread(emailAsyncSup);
						emailThreadSup.start();
					}
		    	
					if(user.getEmail() != null && !"".equals(user.getEmail())) {
						 EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						 emailAsyncSup.setProperties(
									AppConstants.EMAIL_INVOICE_REJECTED + " " + po.getOrderNumber() ,
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER_NOTIF  + " PO:" + po.getOrderNumber() + "<br /> <br />" + AppConstants.ETHIC_CONTENT,
									user.getEmail());
						emailAsyncSup.setMailSender(mailSenderObj);
						Thread emailThreadSup = new Thread(emailAsyncSup);
						emailThreadSup.start();	
					}
					
					return "Los registros indican que cuenta con problemas fiscales y no se podrán cargar facturas en este momento.";
		    } 
		}

		String domesticCurrency = AppConstants.DEFAULT_CURRENCY;
		
		List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
		if(comUDCList != null && !comUDCList.isEmpty()) {
			for(UDC company : comUDCList) {
				if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
					domesticCurrency = company.getStrValue2().trim();
					break;
				}
			}
		}
		
		List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
		if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
			for(UDC supplier : supDomUDCList) {
				if(supplier.getStrValue1().trim().equals(addressBook) && !"".equals(supplier.getStrValue2().trim())) {
					domesticCurrency = supplier.getStrValue2().trim();
					break;
				}
			}
		}
		
		List<Map<String, ZipElementDTO>> fileList = new ArrayList<Map<String,ZipElementDTO>>();
		List<Map<String, byte[]>> attachedList = new ArrayList<Map<String,byte[]>>();
		Map<String, ZipElementDTO> fileMap;
		Map<String, byte[]> attachedMap;
	    ZipElementDTO elementDTO;
	    InvoiceDTO invoiceDTO;
		
        try {        	
    		ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
    		ZipInputStream zis = new ZipInputStream(stream);
    		ZipEntry ze;
    		
			while ((ze = zis.getNextEntry()) != null) {	    		
				String fileName = FilenameUtils.getBaseName(ze.getName());
				int fileSize = (int)ze.getSize();
				log4j.info("Se obtiene el archivo " + fileName + " del ZIP.");
				
			    if(FilenameUtils.getExtension(ze.getName()).equals(AppConstants.FILE_EXT_XML)) {
			    	fileMap = new HashMap<String, ZipElementDTO>();
		    		elementDTO = new ZipElementDTO();
		    		invoiceDTO = new InvoiceDTO();
		    		
			    	byte[] byteArrayFile = this.getByteArrayFromZipInputStream(fileName, AppConstants.FILE_EXT_XML, fileSize, zis);
					ByteArrayInputStream streamXML = new  ByteArrayInputStream(byteArrayFile);
					String xmlContent = IOUtils.toString(streamXML, "UTF-8");
					
					if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
						invoiceDTO = xmlToPojoService.convertV4(xmlContent);
					} else {
						invoiceDTO = xmlToPojoService.convert(xmlContent);
					}
					
					if(invoiceDTO == null){
						return "El archivo " + fileName + ".xml contenido en el ZIP no es aceptado.<br />NO ha pasado la fase de verificación, la solicutud no será cargada.";
					}
					
			    	elementDTO.setInvoiceDTO(invoiceDTO);
			    	elementDTO.setXmlFileName(ze.getName());
			    	elementDTO.setXmlFile(byteArrayFile);
				    fileMap.put(fileName, elementDTO);
				    fileList.add(fileMap);
			    }
			    
			    if(FilenameUtils.getExtension(ze.getName()).equals(AppConstants.FILE_EXT_PDF)) {
			        byte[] byteArrayFile = this.getByteArrayFromZipInputStream(fileName, AppConstants.FILE_EXT_PDF, fileSize, zis);
			        attachedMap = new HashMap<String, byte[]>();
			        attachedMap.put(fileName, byteArrayFile);
			        attachedList.add(attachedMap);
			    }
			}			
			zis.close();
			
			boolean isAssigned = false;
			if(fileList != null && !fileList.isEmpty() && attachedList != null && !attachedList.isEmpty()) {
				for(Map<String, ZipElementDTO> dto : fileList) {
					for(Map.Entry<String, ZipElementDTO> entryDTO : dto.entrySet()) {						
						isAssigned = false;
						
						for(Map<String, byte[]> attached : attachedList) {						
							for(Map.Entry<String, byte[]> entryAtt : attached.entrySet()) {									
								if(entryDTO.getKey().equals(entryAtt.getKey())) {										
									entryDTO.getValue().setAttachedFile(entryAtt.getValue());
									entryDTO.getValue().setAttachedFileName(entryAtt.getKey().concat(".").concat(AppConstants.FILE_EXT_PDF));									
									isAssigned = true;
									break;
								}
							}							
							if(isAssigned) {
								break;
							}
						}
						
					}
				}
			}
		} catch (IOException e) {
			log4j.error("IOException" , e);
			e.printStackTrace();
		}

        //ValidaciÃ³n Montos
		UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
		UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");		
		UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");		
		UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
		UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
		
		double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
		double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;	
		double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
		double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
		double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
		double orderAmount = 0;
		double foreignOrderAmount = 0;
		double paymentAmount = 0;
		
		for(Receipt r :requestedReceiptList) {
			orderAmount = orderAmount + Math.abs(r.getAmountReceived());
			foreignOrderAmount = foreignOrderAmount + Math.abs(r.getForeignAmountReceived());
			paymentAmount = paymentAmount + Math.abs(r.getPaymentAmount());
		}
		
		double totalImporte = 0;
		double totalImporteMayor = 0;
		double totalImporteMenor = 0;
		double invoiceTotalAmount = 0;
		double invoiceAmount = 0;
		double discount = 0;
				
		//Obtiene moneda orden de compra
		String oCurr = "";
		if("PME".equals(po.getCurrecyCode())) {
			oCurr = "MXN";
		}else {
			oCurr = po.getCurrecyCode();
		}
		
		String invCurrency = "";
		//Obtiene Importes Totales de las facturas
		if(fileList != null && !fileList.isEmpty()) {
			for(Map<String, ZipElementDTO> o : fileList) {
				for (Map.Entry<String,ZipElementDTO> entry : o.entrySet())  {
					
					double exchangeRate = 0;
					ZipElementDTO zElement = entry.getValue();
					invCurrency = zElement.getInvoiceDTO().getMoneda().trim();					
					
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(zElement.getInvoiceDTO().getTipoComprobante())){
                		return "El documento cargado no es de tipo FACTURA.<br />(Tipo Comprobante = I) UUID: " + zElement.getInvoiceDTO().getUuid();
                	}
                	
                	if(AppConstants.NC_FIELD.equals(tipoComprobante) && !"E".equals(zElement.getInvoiceDTO().getTipoComprobante())){
                		return "El documento cargado no coresponde a una NOTA DE CREDITO.<br />(Tipo Comprobante = E) UUID: " + zElement.getInvoiceDTO().getUuid();
                	}                
					
					if(!invCurrency.equals(oCurr)) {
						return "La moneda de la factura es " + invCurrency + " sin embargo, el código de moneda de la orden de compra es " + oCurr + " UUID:" + zElement.getInvoiceDTO().getUuid();
					}
					
					if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {
						exchangeRate = zElement.getInvoiceDTO().getTipoCambio();						
						if(exchangeRate == 0) {
							return "La moneda de la factura es " + invCurrency + " sin embargo, no existe definido un tipo de cambio. UUID: " + zElement.getInvoiceDTO().getUuid();
						}
					}
					
					if(zElement.getInvoiceDTO().getDescuento() != 0) {
						discount = zElement.getInvoiceDTO().getDescuento();
						discount = discount * 100;
						discount = Math.round(discount);
						discount = discount /100;
					}
					invoiceTotalAmount = invoiceTotalAmount + zElement.getInvoiceDTO().getSubTotal();
					invoiceTotalAmount = invoiceTotalAmount * 100;
					invoiceTotalAmount = Math.round(invoiceTotalAmount);
					invoiceTotalAmount = invoiceTotalAmount/100;
					
					invoiceAmount = invoiceAmount + zElement.getInvoiceDTO().getSubTotal() - discount;
					invoiceAmount = invoiceAmount * 100;
					invoiceAmount = Math.round(invoiceAmount);
					invoiceAmount = invoiceAmount/100;
				}
			}	
		}
		
		if(domesticCurrency.equals(invCurrency)) {
			totalImporte = Math.round(orderAmount*100.00)/100;
			totalImporteMayor = orderAmount;
			totalImporteMenor = orderAmount;
		} else {
			totalImporte = Math.round(foreignOrderAmount*100.00)/100;
			totalImporteMayor = foreignOrderAmount;
			totalImporteMenor = foreignOrderAmount;
		}
		
		// ValidaciÃ³n con los importes del recibo:
		String tipoValidacion ="";
		if(montoLimite != 0) {
			if(invoiceTotalAmount >= montoLimite) {
				totalImporteMayor = totalImporteMayor + montoLimiteMax;
				totalImporteMenor = totalImporteMenor - montoLimiteMin;
				tipoValidacion = "Por Monto";
			}else {
				totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
				totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
				tipoValidacion = "Por porcentaje";
			}
		}
		
		totalImporteMayor = totalImporteMayor * 100;
		totalImporteMayor = Math.round(totalImporteMayor);
		totalImporteMayor = totalImporteMayor /100;	
		
		totalImporteMenor = totalImporteMenor * 100;
		totalImporteMenor = Math.round(totalImporteMenor);
		totalImporteMenor = totalImporteMenor /100;	

		if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
			return "El total de las facturas " + currencyFormat.format(invoiceAmount) + " no coincide con el total de los recibos seleccionados " + currencyFormat.format(totalImporte) + ". Tipo de validación: " + tipoValidacion + ".";
		}
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		
        // EnvÃ­o Proceso Batch        
		ProcessBatchInvoice bp = new ProcessBatchInvoice();
		bp.setDataAuditService(dataAuditService);
		bp.setRemoteAddress(request.getRemoteAddr());
		bp.setUdcService(udcService);
		bp.setPurchaseOrderService(purchaseOrderService);
		bp.seteDIService(eDIService);
		bp.sethTTPRequestService(HTTPRequestService);
		bp.setMailSenderObj(mailSenderObj);
		bp.setStringUtils(stringUtils);
		bp.setPaymentCalendarService(paymentCalendarService);
		bp.setRequestedReceiptList(requestedReceiptList);
		bp.setDocumentsService(this);		
		bp.setLogger(logger);
		bp.setUser(user);
		bp.setS(s);		
		bp.setPo(po);
		bp.setFileList(fileList);
		bp.setDocumentNumber(documentNumber);
		bp.setDocumentType(documentType);
		bp.setTipoComprobante(tipoComprobante);
		bp.setInvoiceAmount(invoiceAmount);
		Thread emailThreadSup = new Thread(bp);
		emailThreadSup.start();

		return "";
	}
	
	@SuppressWarnings("unused")
	public String validateInvoiceFromOrder(InvoiceDTO inv,
								  String addressBook, 
								  int documentNumber, 
								  String documentType,
								  String tipoComprobante,
								  PurchaseOrder po,
								  boolean sendVoucher,
								  String xmlContent,
								  String receiptList,
								  boolean specializedServices,
								  double miscellaneousAmount){
		
		try {
			boolean isTaxValidationOn = true;
			
			//ValidaciÃ³n de Proveedor
			Supplier s = supplierService.searchByAddressNumber(addressBook);
			if(s == null) {
				return "El proveedor no está registrado en el portal.";
			}
			
			//ValidaciÃ³n del SAT
			DecimalFormat currencyFormat = new DecimalFormat("$#,###.###");
			UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
			
			if(udcCfdi != null) {
				if(!"".equals(udcCfdi.getStrValue1())) {
					if("TRUE".equals(udcCfdi.getStrValue1())) {
						String vcfdi = validaComprobanteSAT(inv);
						if(!"".equals(vcfdi)) {
							return "Error de validación ante el SAT, favor de validar con su emisor fiscal.";
						}
						
						String vNull = validateInvNull(inv);
						if(!"".equals(vNull)) {
							return "Error al validar el archivo XML, no se encontró el campo " + vNull + ".";
						}
					}
				}
			}else {
				String vcfdi = validaComprobanteSAT(inv);
				if(!"".equals(vcfdi)) {
					return "Error de validación ante el SAT, favor de validar con su emisor fiscal.";
				}
				
				String vNull = validateInvNull(inv);
				if(!"".equals(vNull)) {
					return "Error al validar el archivo XML, no se encontró el campo " + vNull + ".";
				}
			}
			
			String rfcEmisor = inv.getRfcEmisor();
			boolean allRules = true;
			List<UDC> supExclList =  udcService.searchBySystem("NOCHECKSUP");
			if(supExclList != null) {
				for(UDC udc : supExclList) {
					if(rfcEmisor.equals(udc.getStrValue1())){
						allRules = false;
						break;
					}
				}
			}
			
			boolean currentYearRule = true;
			List<UDC> noCurrentYearList =  udcService.searchBySystem("NOCURRENTYEAR");
			if(noCurrentYearList != null) {
				for(UDC udc : noCurrentYearList) {
					if(rfcEmisor.equals(udc.getStrValue1())){
						currentYearRule = false;
						break;
					}
				}
			}
			
			List<Receipt> requestedReceiptList = null;
			List<Receipt> receiptArray= purchaseOrderService.getOrderReceipts(documentNumber, addressBook, documentType, po.getOrderCompany());
			if(receiptArray != null) {
				String[] idList = receiptList.split(",");
				requestedReceiptList = new ArrayList<Receipt>();
				for(Receipt r : receiptArray) {
					if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
						requestedReceiptList.add(r);
					}
				}
			}else {
				return "No existen recibos por facturar.";
			}
			
			String fechaFactura = inv.getFechaTimbrado();
			fechaFactura = fechaFactura.replace("T", " ");
			SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
			Date invDate = null;
			try {
				invDate = sdf.parse(fechaFactura);
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}

			//ValidaciÃ³n CFDI VersiÃ³n 3.3
			if(AppConstants.CFDI_V3.equals(inv.getVersion())) {
				UDC udcVersion = udcService.searchBySystemAndKey("VERSIONCFDI", "VERSION33");
				if(udcVersion != null) {
					try {
						boolean isVersionValidationOn = udcVersion.isBooleanValue();
						if(isVersionValidationOn) {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							String strLastDateAllowed = udcVersion.getStrValue1();
							Date dateLastDateAllowed = formatter.parse(strLastDateAllowed);
							if(invDate.compareTo(dateLastDateAllowed) > 0) {
								return "La versión del CFDI no es válida.";
							}
						}
					} catch (Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();
						return "Error al obtener la fecha de timbrado de la factura";
					}
				}
			}
			
			if(allRules) {
				
				String invCurrency = inv.getMoneda().trim();
				double exchangeRate = inv.getTipoCambio();
				
				String domesticCurrency = AppConstants.DEFAULT_CURRENCY;
				List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
				if(comUDCList != null && !comUDCList.isEmpty()) {
					for(UDC company : comUDCList) {
						if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
							domesticCurrency = company.getStrValue2().trim();
							break;
						}
					}
				}
				
				List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
				if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
					for(UDC supplier : supDomUDCList) {
						if(supplier.getStrValue1().trim().equals(addressBook) && !"".equals(supplier.getStrValue2().trim())) {
							domesticCurrency = supplier.getStrValue2().trim();
							break;
						}
					}
				}
				
				//ValidaciÃ³n de Factura Repetida
				FiscalDocuments fd = fiscalDocumentService.getFiscalDocumentsByUuid(inv.getUuid());
				if(fd != null) {
					return "La factura que intenta ingresar ya fue cargada previamente.";
				}
				
				//ValidaciÃ³n de Tipo de Cambio
				if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {				
					if(exchangeRate == 0) {
						return "La moneda de la factura es " + invCurrency + " sin embargo, no está definido un tipo de cambio.";
					}
				}
				
				//ValidaciÃ³n de Moneda
				String oCurr = "";
				if("PME".equals(po.getCurrecyCode())) {//CÃ³digo de Moneda de Pesos en JDE
					oCurr = "MXN";
				}else {
					oCurr = po.getCurrecyCode();
				}
				if(!invCurrency.equals(oCurr)) {
					return "La moneda de la factura es " + invCurrency + " sin embargo, el código de moneda de la orden de compra es " + oCurr;
				}
				
				//ValidaciÃ³n de AÃ±o Actual
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, currentYear);
				cal.set(Calendar.DAY_OF_YEAR, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date startYear = cal.getTime();
				
				if(currentYearRule) {
					try {
						if(invDate.compareTo(startYear) < 0) {
							return "La fecha de emisión de la factura no puede ser anterior al primero de Enero del año en curso";
						}
					}catch(Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();
						return "Error al obtener la fecha de timbrado de la factura";
					}	
				}
				
				//ValidaciÃ³n de Mes
				
				UDC udcCurrentMonth = udcService.searchBySystemAndKey("VALIDARMESCURSO", "FACTURA");
				if (udcCurrentMonth != null && "TRUE".equals(udcCurrentMonth.getStrValue1())) {

					// Fecha Actual (Calendar)
					Calendar calCurrentDate = Calendar.getInstance();
					calCurrentDate.setTime(new Date());

					// Fecha Factura (Calendar)
					Calendar calInvoiceDate = Calendar.getInstance();
					calInvoiceDate.setTime(invDate);

					boolean isSameMonth = (calCurrentDate.get(Calendar.YEAR) == calInvoiceDate.get(Calendar.YEAR))
							&& (calCurrentDate.get(Calendar.MONTH) == calInvoiceDate.get(Calendar.MONTH));

					if (!isSameMonth) {
						return "La factura no fue generada durante el mes en curso.";
					}

				}
			    
			    //ValidaciÃ³n de Proveedores Incumplidos
				NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(s.getRfc(), 0, 0);
			    if (ncs != null && (
			      ncs.getRefDate1().contains("Definitivo") || 
			      ncs.getRefDate1().contains("Presunto") || 
			      ncs.getRefDate1().contains("Desvirtuado") || 
			      ncs.getRefDate2().contains("Definitivo") || 
			      ncs.getRefDate2().contains("Presunto") || 
			      ncs.getRefDate2().contains("Desvirtuado") || 
			      ncs.getStatus().contains("Definitivo") || 
			      ncs.getStatus().contains("Presunto") || 
			      ncs.getStatus().contains("Desvirtuado"))) {
			    	
			    	String altEmail = "";
						List<UDC> udcList =  udcService.searchBySystem("TAXALTEMAIL");
						if(udcList != null) {
							altEmail = udcList.get(0).getStrValue1();
						    EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
							emailAsyncSup.setProperties(
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE,
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER + " Número: " + s.getAddresNumber() + "<br /> Nombre: " + s.getRazonSocial() + "<br />",
									altEmail);
							emailAsyncSup.setMailSender(mailSenderObj);
							Thread emailThreadSup = new Thread(emailAsyncSup);
							emailThreadSup.start();
						}
			    	
						 EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						 emailAsyncSup.setProperties(
									AppConstants.EMAIL_INVOICE_REJECTED + " " + po.getOrderNumber() ,
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER_NOTIF + inv.getUuid() + "<br /> <br />" + AppConstants.ETHIC_CONTENT,
									s.getEmailSupplier());
							emailAsyncSup.setMailSender(mailSenderObj);
							Thread emailThreadSup = new Thread(emailAsyncSup);
							emailThreadSup.start();
						
						return "Los registros indican que cuenta con problemas fiscales y no se podrán cargar facturas en este momento.";

			    }
				
			    //ValidaciÃ³n de Emisor
				if(rfcEmisor != null) {
					if(!"".equals(rfcEmisor)) {
						if(!s.getRfc().equals(rfcEmisor)) {
							return "El RFC del emisor no pertenece al RFC del proveedor registrado como " + s.getRfc();
						}
					}
				}
				
				//ValidaciÃ³n de Receptor
				String cfdiReceptor = inv.getReceptor().getUsoCFDI();
				String rfcReceptor = inv.getRfcReceptor();			
				boolean receptorValido = false;
				List<UDC> receptores = udcService.searchBySystem("RECEPTOR");
				if(receptores != null) {
					for(UDC udc : receptores) {
						if(udc.getStrValue1().equals(rfcReceptor)) {
							receptorValido = true;
							break;
						}
					}
				}
				if(!receptorValido) {
					return "El RFC receptor " + inv.getRfcReceptor() + " no está permitido para la carga de facturas.";
				}
				
				//ValidaciÃ³n de CompaÃ±Ã­a
				boolean companyRfcIsValid = false;
				List<UDC> companyRfc = udcDao.searchBySystem("COMPANYRFC");
				if(companyRfc != null) {
					for(UDC udcrfc : companyRfc) {
						String cRfc = udcrfc.getStrValue1();
						String cRfcCompany = udcrfc.getUdcKey();
						if(cRfc.equals(inv.getRfcReceptor())) {
							if(cRfcCompany.equals(po.getCompanyKey())) {
								companyRfcIsValid = true;
								break;
							}
						}
					}
				}

				if(!companyRfcIsValid) {
					return "La compañía de la factura no corresponde a la compañía de la orden de compra";
				}

				//ValidaciÃ³n de USO CFDI
				boolean usoCFDIAllowed = false;
				List<UDC> udcList =  udcService.searchBySystem("USOCFDI");
				if(udcList != null) {
					for(UDC udc : udcList) {
						if(udc.getStrValue1().equals(cfdiReceptor)){
							usoCFDIAllowed = true;
							break;
						}
					}
				}
				
				if(!usoCFDIAllowed) {
					return "El uso CFDI " + cfdiReceptor + " no está permitido para su razón social.";
				}
				
				boolean paymentComplException = false;
				List<UDC> pcExceptionList =  udcService.searchBySystem("NOPAYMENTCOMPL");
				if(pcExceptionList != null) {
					for(UDC udc : pcExceptionList) {
						if(addressBook.equals(udc.getStrValue1())){
							paymentComplException = true;
							break;
						}
					}
				}
				
				//ValidaciÃ³n de Complementos de Pago Pendientes
				if(!paymentComplException && AppConstants.LOCAL_COUNTRY_CODE.equals(s.getCountry())) {
					String pendingList = purchaseOrderService.getPendingReceiptsComplPago(s.getAddresNumber());
					if(!"".equals(pendingList)){
						return "El sistema detectó que tiene las siguientes facturas (uuid) con COMPLEMENTOS DE PAGO pendientes de carga: <br /> " + pendingList;
					}
				}
				
				//ValidaciÃ³n de los importes del recibo			
				UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
				UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");				
				UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");				
				UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
				UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
				
				double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
				double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;
				
				double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
				double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
				double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
				
				double orderAmount = 0;
				double foreignOrderAmount = 0;
				double retainedAmount = 0;
				double retainedForeignAmount = 0;
				double currentRetainedAmount = 0;
				double invoiceAmount = 0;
				
				boolean isRetainedValidationOn = true;
				List<UDC> taxCodeExcList =  udcService.searchBySystem("NOCHECKRET");
				List<String> taxCodeList = new ArrayList<String>();
				if(taxCodeExcList != null && !taxCodeExcList.isEmpty()) {
					for(UDC taxCode : taxCodeExcList) {
						taxCodeList.add(taxCode.getStrValue1());
					}
				}
				
				//JAVILA
				//String receiptTaxCode = "";
				List<String> receiptTaxCode = new ArrayList<String>();
				for(Receipt r :requestedReceiptList) {
					if(r.getTaxCode() != null && !"".equals(r.getTaxCode().trim())) {
						//receiptTaxCode = r.getTaxCode().trim();
						receiptTaxCode.add(r.getTaxCode().trim());
						if(taxCodeList.contains(r.getTaxCode().trim().toUpperCase())) {
							isRetainedValidationOn = false;
							break;	
						}
					}
				}

				//JSAAVEDRA: ValidaciÃ³n de impuestos
				if(isTaxValidationOn) {
					String tasaOCuota = "";
					List<UDC> udcTax = udcDao.searchBySystem("TAXCODE");
					List<String> validTaxCodeList = new ArrayList<String>();
					
					if(udcTax != null) {
						for(UDC ut :udcTax) {
							validTaxCodeList.add(ut.getUdcKey().trim());
						}
					}

					//if(!"".equals(receiptTaxCode)) {
					if(receiptTaxCode != null || receiptTaxCode.size() != 0){
						if(validTaxCodeList.containsAll(receiptTaxCode)) {					
							List<String> cfdiTransTaxList = this.getTranslatedTaxList(inv);
							List<String> cfdiRetTaxList = this.getRetainedTaxList(inv);
							List<String> udcTransTaxList = new ArrayList<String>();
							List<String> udcRetTaxList = new ArrayList<String>();
							String udcValueT = "";
							String udcValueR = "";
							String tasasRequeridas = "";

							//TRASLADADOS
							//Obtiene lista de impuestos Trasladados requeridos con base en la UDC
							for(UDC ut :udcTax) {
								//if(ut.getUdcKey().equals(receiptTaxCode.trim()) && ut.getStrValue1() != null && !"".equals(ut.getStrValue1().trim()) && !"NA".equals(ut.getStrValue1().trim())) {
								if(receiptTaxCode.contains(ut.getUdcKey()) && ut.getStrValue1() != null && !"".equals(ut.getStrValue1().trim()) && !"NA".equals(ut.getStrValue1().trim())) {
									udcValueT = ut.getStrValue1();
									//udcTransTaxList = this.stringWithPipesToList(udcValueT);
									udcTransTaxList.addAll(this.stringWithPipesToList(udcValueT));
									//break;
								}
							}
							
							//Valida que el CFDI cuente con los impuestos Trasladado requeridos
							if(!udcTransTaxList.isEmpty()) {
								for(String transTaxValue : udcTransTaxList) {
									//Verifica si el CFDI cuenta con el impuesto Trasladado
									if(!cfdiTransTaxList.contains(transTaxValue)) {
										tasasRequeridas = udcValueT.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI no cumple con los impuestos trasladados requeridos, favor de verificarlo con el comprador.<br />Código de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos trasladados que se requiren: " + tasasRequeridas + ".";
									}
								}					
							}
							
							//Valida que el CFDI no tenga impuestos Trasladados adicionales a los impuestos requeridos
							if(!cfdiTransTaxList.isEmpty()) {					
								for(String transTaxValue : cfdiTransTaxList) {
									if(!udcTransTaxList.contains(transTaxValue)) {
										tasasRequeridas = udcValueT.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI contiene impuestos trasladados que no corresponden con el recibo, favor de verificarlo con el comprador.<br />Código de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos trasladados que se requiren: " + tasasRequeridas + ". Tasa no válida: " + transTaxValue + ".";
									}
								}
							}
							
							//RETENIDOS
							//Obtiene lista de impuestos Retenidos requeridos con base en la UDC
							for(UDC ut :udcTax) {
								//if(ut.getUdcKey().equals(receiptTaxCode.trim()) && ut.getStrValue2() != null && !"".equals(ut.getStrValue2().trim()) && !"NA".equals(ut.getStrValue2().trim())) {
								if(receiptTaxCode.contains(ut.getUdcKey()) && ut.getStrValue2() != null && !"".equals(ut.getStrValue2().trim()) && !"NA".equals(ut.getStrValue2().trim())) {
									udcValueR = ut.getStrValue2().trim();
									//udcRetTaxList = this.stringWithPipesToList(udcValueR);
									udcRetTaxList.addAll(this.stringWithPipesToList(udcValueR));
									//break;
								}
							}
							
							//Valida que el CFDI cuente con los impuestos Retenidos requeridos
							if(!udcRetTaxList.isEmpty()) {
								for(String retTaxValue : udcRetTaxList) {
									//Verifica si el CFDI cuenta con el impuesto Retenido
									if(!cfdiRetTaxList.contains(retTaxValue)) {
										tasasRequeridas = udcValueR.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI no cumple con los impuestos retenidos requeridos, favor de verificarlo con el comprador.<br />Código de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos retenidos que se requiren: " + tasasRequeridas + ".";
									}
								}
							}
							
							//Valida que el CFDI no tenga impuestos Retenidos adicionales a los impuestos requeridos
							if(!cfdiRetTaxList.isEmpty()) {					
								for(String retTaxValue : cfdiRetTaxList) {
									if(!udcRetTaxList.contains(retTaxValue)) {
										tasasRequeridas = udcValueR.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI contiene impuestos retenidos que no corresponden con el recibo, favor de verificarlo con el comprador.<br />Código de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos retenidos que se requiren: " + tasasRequeridas + ". Tasa no válida: " + retTaxValue + ".";
									}
								}
							}
							
						} else {
							return "El recibo no cuenta con un código de impuestos válido, favor de verificarlo con el comprador.<br />Código de impuestos del recibo: " + receiptTaxCode;
						}
					} else {
						return "El recibo no cuenta con un código de impuestos válido, favor de verificarlo con el comprador.";
					}
				}
				
				//Calcula montos de Recibos y de Retenciones
				for(Receipt r :requestedReceiptList) {
					if(AppConstants.RECEIPT_CODE_RETENTION.equals(r.getReceiptType())) {
						retainedAmount = retainedAmount + r.getAmountReceived();
						retainedForeignAmount = retainedForeignAmount + r.getForeignAmountReceived();
					} else {
						orderAmount = orderAmount + r.getAmountReceived();
						foreignOrderAmount = foreignOrderAmount + r.getForeignAmountReceived();
					}
				}
				
				//ValidaciÃ³n de Retenciones
				String tipoValidacion ="";
				
				if(isRetainedValidationOn) {
					double totalImporteMayorRetenido = 0;
					double totalImporteMenorRetenido = 0;
					double retainedTotalAmount = 0;
					
					if(domesticCurrency.equals(invCurrency)) {
						currentRetainedAmount = retainedAmount;
						totalImporteMayorRetenido = retainedAmount;
						totalImporteMenorRetenido = retainedAmount;
					} else {
						currentRetainedAmount = retainedForeignAmount;
						totalImporteMayorRetenido = retainedForeignAmount;
						totalImporteMenorRetenido = retainedForeignAmount;
					}
					
					retainedTotalAmount = inv.getTotalRetenidos();
					if(montoLimite != 0) {
						if(retainedTotalAmount >= montoLimite) {
							totalImporteMayorRetenido = totalImporteMayorRetenido + montoLimiteMax;
							totalImporteMenorRetenido = totalImporteMenorRetenido - montoLimiteMin;
							tipoValidacion = "Por Monto";
						}else {
							totalImporteMayorRetenido = totalImporteMayorRetenido + (totalImporteMayorRetenido * porcentajeMax);
							totalImporteMenorRetenido = totalImporteMenorRetenido - (totalImporteMenorRetenido * porcentajeMin);
							tipoValidacion = "Por porcentaje";
						}
					}
					
					totalImporteMayorRetenido = totalImporteMayorRetenido * 100;
					totalImporteMayorRetenido = Math.round(totalImporteMayorRetenido);
					totalImporteMayorRetenido = totalImporteMayorRetenido /100;	
					
					totalImporteMenorRetenido = totalImporteMenorRetenido * 100;
					totalImporteMenorRetenido = Math.round(totalImporteMenorRetenido);
					totalImporteMenorRetenido = totalImporteMenorRetenido /100;
					
					if(totalImporteMayorRetenido < retainedTotalAmount || totalImporteMenorRetenido > retainedTotalAmount) {					
						return "El total de los impuestos retenidos de su CFDI es " + currencyFormat.format(retainedTotalAmount) + " no coincide con el total de las retenciones del recibo seleccionado. Favor de verificarlo con el comprador.";
					}
				}

				//ValidaciÃ³n de Montos
				double totalImporteMayor = 0;
				double totalImporteMenor = 0;
				double invoiceTotalAmount = 0;
				double currentInvoiceAmount = 0;
				
				if(domesticCurrency.equals(invCurrency)) {
					currentInvoiceAmount = orderAmount;
					totalImporteMayor = orderAmount;
					totalImporteMenor = orderAmount;
				} else {
					currentInvoiceAmount = foreignOrderAmount;
					totalImporteMayor = foreignOrderAmount;
					totalImporteMenor = foreignOrderAmount;
				}
								
				invoiceTotalAmount = inv.getSubTotal();
				
				if(montoLimite != 0) {
					if(invoiceTotalAmount >= montoLimite) {
						totalImporteMayor = totalImporteMayor + montoLimiteMax;
						totalImporteMenor = totalImporteMenor - montoLimiteMin;
						tipoValidacion = "Por Monto";
					}else {
						totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
						totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
						tipoValidacion = "Por porcentaje";
					}
				}
				
				totalImporteMayor = totalImporteMayor * 100;
				totalImporteMayor = Math.round(totalImporteMayor);
				totalImporteMayor = totalImporteMayor /100;	
				
				totalImporteMenor = totalImporteMenor * 100;
				totalImporteMenor = Math.round(totalImporteMenor);
				totalImporteMenor = totalImporteMenor /100;
				
				double discount = 0;
				
				if(inv.getDescuento() != 0) {
					discount = inv.getDescuento();
					discount = discount * 100;
					discount = Math.round(discount);
					discount = discount /100;	
				}
				
				invoiceAmount = inv.getSubTotal() - discount;
				invoiceAmount = invoiceAmount * 100;
				invoiceAmount = Math.round(invoiceAmount);
				invoiceAmount = invoiceAmount /100;
				
				if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
					return "El total de la factura " + currencyFormat.format(invoiceAmount) + " no coincide con el total de los recibos seleccionados " + currencyFormat.format(currentInvoiceAmount) + ". Tipo de validación:" + tipoValidacion;
				}
			}
		} catch (NumberFormatException e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return "Ocurrió un error al validar los datos de la factura.";
		}
		
		return "";
	}

	public String saveInvoiceFromOrder(FileUploadBean uploadItem, PurchaseOrder po, String receiptList, InvoiceDTO inv, String tipoComprobante) {
		
		try {			
			Supplier supplier = supplierService.searchByAddressNumber(po.getAddressNumber());
			
			List<Receipt> requestedReceiptList = null;
			List<Receipt> receiptArray= purchaseOrderService.getOrderReceipts(po.getOrderNumber(), po.getAddressNumber(), po.getOrderType(), po.getOrderCompany());
			if(receiptArray != null) {
				String[] idList = receiptList.split(",");
				requestedReceiptList = new ArrayList<Receipt>();
				for(Receipt r : receiptArray) {
					if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
						requestedReceiptList.add(r);
					}
				}
			}
			
			String fechaFactura = inv.getFechaTimbrado();
			fechaFactura = fechaFactura.replace("T", " ");
			SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
			Date invDate = null;
			try {
				invDate = sdf.parse(fechaFactura);
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}
			
			String firstApprover = "";
			String firstApproverEmail = "";
			
			UDC firstApproverUser = udcDao.searchBySystemAndKey("APPROVERINV", "FIRST_APPROVER");
			if(firstApproverUser != null && firstApproverUser.getStrValue1() != null && !firstApproverUser.getStrValue1().trim().isEmpty()) {
				firstApprover = firstApproverUser.getStrValue1();			
				Users u = usersService.searchCriteriaUserName(firstApprover);
				if(u != null) {
					firstApproverEmail = u.getEmail();
				}
			}

			//Fecha de Vencimiento
			//Date estimatedPaymentDate = this.getEstimatedPaymentDate(supplier); //Se calcula en la Ãºltima aprobaciÃ³n
			
			FiscalDocuments o = new FiscalDocuments();
			o.setAccountingAccount("");
			o.setGlOffset("");
			o.setTaxCode(supplier.getTaxRate()); 			
			o.setCurrencyMode("MXN".equals(inv.getMoneda()) ? AppConstants.CURRENCY_MODE_DOMESTIC : AppConstants.CURRENCY_MODE_FOREIGN);			
			o.setPaymentTerms(supplier.getDiasCredito());
			o.setSupplierName(supplier.getName());
			o.setImpuestos(inv.getTotalImpuestos());
			o.setFolio(inv.getFolio()!=null?inv.getFolio():"");
			o.setSerie(inv.getSerie()!=null?inv.getSerie():"");
			o.setUuidFactura(inv.getUuid());
			o.setType("E".equals(inv.getTipoComprobante()) ? AppConstants.NC_FIELD_UDC:AppConstants.INVOICE_FIELD_UDC);
			o.setAddressNumber(po.getAddressNumber());
			o.setRfcEmisor(inv.getRfcEmisor());
			o.setStatus(AppConstants.STATUS_INPROCESS);
			o.setSubtotal(inv.getSubTotal());
			o.setAmount(inv.getTotal());
			o.setMoneda(inv.getMoneda());
			o.setCurrencyCode(inv.getMoneda());
			o.setInvoiceDate(inv.getFechaTimbrado());
			o.setDescuento(inv.getDescuento());
			o.setImpuestos(inv.getImpuestos());
			o.setRfcReceptor(inv.getRfcReceptor());
			o.setMetodoPago(inv.getMetodoPago());
			o.setFormaPago(inv.getFormaPago());
			//o.setEstimatedPaymentDate(estimatedPaymentDate); //Se calcula en la Ãºltima aprobaciÃ³n
			o.setInvoiceUploadDate(new Date());
			o.setOrderCompany(po.getOrderCompany());
			o.setOrderNumber(po.getOrderNumber());
			o.setOrderType(po.getOrderType());
			o.setApprovalStatus(AppConstants.STATUS_INPROCESS);
			o.setApprovalStep(AppConstants.STATUS_APPROVALFIRSTSTEP);
			o.setCurrentApprover(firstApprover);
			o.setEmailApprover(firstApproverEmail);
			o.setNextApprover("");
			o.setInvoiceType(AppConstants.INV_TYPE_RECEIPT);
			o.setResponsibleUser1(firstApprover);
			fiscalDocumentDao.saveDocument(o);
			
			po.setInvoiceAmount(po.getInvoiceAmount() + inv.getTotal());
			po.setOrderStauts(AppConstants.STATUS_OC_PENDING);
			po.setInvoiceUploadDate(invDate);
			po.setSentToWns(null);
			//po.setEstimatedPaymentDate(estimatedPaymentDate); //Se calcula en la Ãºltima aprobaciÃ³n
			if(AppConstants.ORDER_TYPE_WITHOUT_RECEIPTS.equals(po.getOrderType())) {
				po.setInvDate(invDate);
				po.setFolio(inv.getFolio());
				po.setSerie(inv.getSerie());
				po.setInvoiceUuid(inv.getUuid());
				po.setInvoiceUploadDate(new Date());
				po.setFormaPago(inv.getFormaPago());
				po.setMetodoPago(inv.getMetodoPago());		
			}
			purchaseOrderService.updateOrders(po);
			
			for(Receipt r :requestedReceiptList) {
				r.setInvDate(invDate);
				r.setFolio(inv.getFolio());
				r.setSerie(inv.getSerie());
				r.setUuid(inv.getUuid());
				//r.setEstPmtDate(estimatedPaymentDate); //Se calcula en la Ãºltima aprobaciÃ³n
				r.setUploadInvDate(new Date());
				r.setStatus(AppConstants.STATUS_OC_PENDING);
				r.setFormaPago(inv.getFormaPago());
				r.setMetodoPago(inv.getMetodoPago());
			}
			purchaseOrderService.updateReceipts(requestedReceiptList);
			
			//Guardar Documentos		
			//XML Factura
			String ct = uploadItem.getFile().getContentType();		
			UserDocument doc = new UserDocument(); 		
			doc.setAddressBook(po.getAddressNumber());
			doc.setDocumentNumber(po.getOrderNumber());
			doc.setDocumentType(po.getOrderType());
			doc.setContent(uploadItem.getFile().getBytes());
			doc.setType(ct.trim());
			doc.setName(uploadItem.getFile().getOriginalFilename());
			doc.setSize(uploadItem.getFile().getSize());
			doc.setStatus(true);
			doc.setAccept(true);
			doc.setFiscalType(tipoComprobante);
			doc.setType("text/xml");
			doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
			doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
			doc.setUuid(inv.getUuid());
			doc.setUploadDate(new Date());
			doc.setFiscalRef(0);
			documentsDao.saveDocuments(doc);
			
			//PDF Factura
			doc = new UserDocument(); 
			doc.setAddressBook(po.getAddressNumber());
			doc.setDocumentNumber(po.getOrderNumber());
			doc.setDocumentType(po.getOrderType());
			doc.setContent(uploadItem.getFileTwo().getBytes());
			doc.setType(uploadItem.getFileTwo().getContentType().trim());
			doc.setName(uploadItem.getFileTwo().getOriginalFilename());
			doc.setSize(uploadItem.getFileTwo().getSize());
			doc.setStatus(true);
			doc.setAccept(true);
			doc.setFiscalType(tipoComprobante);
			doc.setType("application/pdf");
			doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
			doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
			doc.setUuid(inv.getUuid());
			doc.setUploadDate(new Date());
			doc.setFiscalRef(0);
			documentsDao.saveDocuments(doc);
			
			//Evidencia de Sello de OC
			if(uploadItem.getFileThree().getSize() > 0) {
				doc = new UserDocument(); 
				doc.setAddressBook(po.getAddressNumber());
				doc.setDocumentNumber(po.getOrderNumber());
				doc.setDocumentType(po.getOrderType());
				doc.setContent(uploadItem.getFileThree().getBytes());
				doc.setType(uploadItem.getFileThree().getContentType().trim());
				doc.setName(uploadItem.getFileThree().getOriginalFilename());
				doc.setSize(uploadItem.getFileThree().getSize());
				doc.setStatus(true);
				doc.setAccept(true);
				doc.setFiscalType("EvidenciaOC");
				doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
				doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
				doc.setUuid(inv.getUuid());
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsDao.saveDocuments(doc);	
			}
			
			//Anexo 1 (Opcional)
			if(uploadItem.getFileFour().getSize() > 0) {
				doc = new UserDocument(); 
				doc.setAddressBook(po.getAddressNumber());
				doc.setDocumentNumber(po.getOrderNumber());
				doc.setDocumentType(po.getOrderType());
				doc.setContent(uploadItem.getFileFour().getBytes());
				doc.setType(uploadItem.getFileFour().getContentType().trim());
				doc.setName(uploadItem.getFileFour().getOriginalFilename());
				doc.setSize(uploadItem.getFileFour().getSize());
				doc.setStatus(true);
				doc.setAccept(true);
				doc.setFiscalType("Anexo");
				doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
				doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
				doc.setUuid(inv.getUuid());
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsDao.saveDocuments(doc);
			}
			
			//Anexo 2 (Opcional)
			if(uploadItem.getFileFive().getSize() > 0) {
				doc = new UserDocument(); 
				doc.setAddressBook(po.getAddressNumber());
				doc.setDocumentNumber(po.getOrderNumber());
				doc.setDocumentType(po.getOrderType());
				doc.setContent(uploadItem.getFileFive().getBytes());
				doc.setType(uploadItem.getFileFive().getContentType().trim());
				doc.setName(uploadItem.getFileFive().getOriginalFilename());
				doc.setSize(uploadItem.getFileFive().getSize());
				doc.setStatus(true);
				doc.setAccept(true);
				doc.setFiscalType("Anexo");
				doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
				doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
				doc.setUuid(inv.getUuid());
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsDao.saveDocuments(doc);
			}
			
			//Anexo 3 (Opcional)
			if(uploadItem.getFileSix().getSize() > 0) {
				doc = new UserDocument(); 
				doc.setAddressBook(po.getAddressNumber());
				doc.setDocumentNumber(po.getOrderNumber());
				doc.setDocumentType(po.getOrderType());
				doc.setContent(uploadItem.getFileSix().getBytes());
				doc.setType(uploadItem.getFileSix().getContentType().trim());
				doc.setName(uploadItem.getFileSix().getOriginalFilename());
				doc.setSize(uploadItem.getFileSix().getSize());
				doc.setStatus(true);
				doc.setAccept(true);
				doc.setFiscalType("Anexo");
				doc.setFolio(inv.getFolio()!=null?inv.getFolio():"");
				doc.setSerie(inv.getSerie()!=null?inv.getSerie():"");
				doc.setUuid(inv.getUuid());
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsDao.saveDocuments(doc);
			}
			
			//NotificaciÃ³n Proveedor		
			EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			emailAsyncSup.setProperties(AppConstants.EMAIL_INV_ACCPET_BUYER + po.getOrderNumber(),
					this.stringUtils.prepareEmailContent(String.format(AppConstants.FISCAL_DOC_MAIL_MSJ_FAC_PROV,
							new Object[] {inv.getFolio(), inv.getUuid(), AppConstants.EMAIL_PORTAL_LINK, inv.getFolio(), inv.getUuid(), AppConstants.EMAIL_PORTAL_LINK})),
					supplier.getEmailSupplier());
			emailAsyncSup.setMailSender(this.mailSenderObj);
			Thread emailThreadSup = new Thread(emailAsyncSup);
			emailThreadSup.start();
			
			//NotificaciÃ³n Aprobador
			EmailServiceAsync emailAsyncSup1 = new EmailServiceAsync();
			emailAsyncSup1.setProperties(AppConstants.EMAIL_INV_ACCPET_BUYER + po.getOrderNumber(),
					this.stringUtils.prepareEmailContent(String.format(AppConstants.FISCAL_DOC_MAIL_MSJ_FAC_COMP,
							new Object[] {inv.getFolio(), inv.getUuid(), supplier.getRazonSocial(), AppConstants.EMAIL_PORTAL_LINK })),
					firstApproverEmail);
			emailAsyncSup1.setMailSender(this.mailSenderObj);
			Thread emailThreadSup1 = new Thread(emailAsyncSup1);
			emailThreadSup1.start();
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return "Ocurrió un error al guardar los archivos de la factura.";
		}
		
		return "";
	}
	
	public Date getEstimatedPaymentDate(Supplier s) {
		Date estimatedPaymentDate = new Date();
		int diasCred = 30;
		
		try {
			if(s.getDiasCredito() != null && !s.getDiasCredito().trim().isEmpty()) {
				diasCred = Integer.valueOf(s.getDiasCredito().trim());
			}
			
			if(s.getPmtTrmCxC() != null && !s.getPmtTrmCxC().trim().isEmpty()) {
				UDC pmtTermsUdc = udcService.searchBySystemAndKey("PMTTERMS", s.getPmtTrmCxC().trim());
				if(pmtTermsUdc != null) {
					diasCred = pmtTermsUdc.getIntValue();
				}				
			}
			
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, diasCred);
			
			List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, s.getAddresNumber());
			if(pc != null) {
				if(pc.size() > 0) {
					estimatedPaymentDate = pc.get(0).getPaymentDate();
				}else {
					estimatedPaymentDate = c.getTime();
				}
			}else {
				estimatedPaymentDate = c.getTime();
			}
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
		return estimatedPaymentDate;
	}
	
	@SuppressWarnings({ "unused"})
	public String validateCreditNoteFromOrder(InvoiceDTO inv,String addressBook, 
								  int documentNumber, 
								  String documentType,
								  String tipoComprobante,
								  PurchaseOrder po,
								  boolean sendVoucher,
								  String xmlContent,
								  String receiptList){
		
		String invCurrency = inv.getMoneda().trim();
		double exchangeRate = inv.getTipoCambio();
		
		UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
		if(udcCfdi != null) {
			if(!"".equals(udcCfdi.getStrValue1())) {
				if("TRUE".equals(udcCfdi.getStrValue1())) {
					String vcfdi = validaComprobanteSAT(inv);
					if(!"".equals(vcfdi)) {
						return "Error de validación ante el SAT, favor de validar con su emisor fiscal.";
					}
				}
			}
		}else {
			String vcfdi = validaComprobanteSAT(inv);
			if(!"".equals(vcfdi)) {
				return "Error de validación ante el SAT, favor de validar con su emisor fiscal.";
			}
		}
		
		String rfcEmisor = inv.getRfcEmisor();
		boolean allRules = true;
		List<UDC> supExclList =  udcService.searchBySystem("NOCHECKSUP");
		if(supExclList != null) {
			for(UDC udc : supExclList) {
				if(rfcEmisor.equals(udc.getStrValue1())){
					allRules = false;
					break;
				}
			}
		}
		
		String resp = "";
		Supplier s = supplierService.searchByAddressNumber(addressBook);
		if(s == null) {
			return "El proveedor no existe en la base de datos";
		}
		String emailRecipient = (s.getEmailSupplier());
		List<Receipt> requestedReceiptList = null;
		List<Receipt> receiptArray= purchaseOrderService.getNegativeOrderReceipts(documentNumber, addressBook, documentType,"");
		
		String fechaFactura = inv.getFechaTimbrado();
		fechaFactura = fechaFactura.replace("T", " ");
		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
		Date invDate = null;
		try {
			invDate = sdf.parse(fechaFactura);
		}catch(Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
		//ValidaciÃ³n CFDI VersiÃ³n 3.3
		if(AppConstants.CFDI_V3.equals(inv.getVersion())) {
			UDC udcVersion = udcService.searchBySystemAndKey("VERSIONCFDI", "VERSION33");
			if(udcVersion != null) {
				try {
					boolean isVersionValidationOn = udcVersion.isBooleanValue();
					if(isVersionValidationOn) {
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String strLastDateAllowed = udcVersion.getStrValue1();
						Date dateLastDateAllowed = formatter.parse(strLastDateAllowed);
						if(invDate.compareTo(dateLastDateAllowed) > 0) {
							return "La versión del CFDI no es válida.";
						}
					}
				} catch (Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
					return "Error al obtener la fecha de timbrado de la factura";
				}
			}
		}
		
		String domesticCurrency = AppConstants.DEFAULT_CURRENCY;
		List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
		if(comUDCList != null && !comUDCList.isEmpty()) {
			for(UDC company : comUDCList) {
				if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
					domesticCurrency = company.getStrValue2().trim();
					break;
				}
			}
		}

		List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
		if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
			for(UDC supplier : supDomUDCList) {
				if(supplier.getStrValue1().trim().equals(addressBook) && !"".equals(supplier.getStrValue2().trim())) {
					domesticCurrency = supplier.getStrValue2().trim();
					break;
				}
			}
		}
		
		if(allRules) {		
		if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {			
			if(exchangeRate == 0) {
				return "La moneda de la nota de crédito es " + invCurrency + " sin embargo, no existe definido un tipo de cambio.";
			}
		}
		
		String oCurr = "";
		if("PME".equals(po.getCurrecyCode())) {
			oCurr = "MXN";
		}else {
			oCurr = po.getCurrecyCode();
		}
		if(!invCurrency.equals(oCurr)) {
			return "La moneda de la nota de crédito es " + invCurrency + " sin embargo, el cÃ³digo de moneda de la orden de compra es " + oCurr;
		}
		
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, currentYear);
		cal.set(Calendar.DAY_OF_YEAR, 1);    
		Date startYear = cal.getTime();
		try {
			if(invDate.compareTo(startYear) < 0) {
				return "La fecha de emisión de la factura no puede ser anterior al primero de Enero del año actual";
			}
		}catch(Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return "Error al obtener la fecha de timbrado de la factura";
		}


		if(receiptArray != null) {
			String[] idList = receiptList.split(",");
			requestedReceiptList = new ArrayList<Receipt>();
			for(Receipt r : receiptArray) {
				if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
					requestedReceiptList.add(r);
				}
			}
			
			if(requestedReceiptList.isEmpty()) {
				return "No existen recibos para cargar la nota de crédito";
			}			
		}else {
			return "No existen recibos para cargar la nota de crédito";
	    }
				
		String cfdiRel = inv.getCfdiRelacionado();
		if(cfdiRel != null) {
			List<Receipt> invReceipts = purchaseOrderService.getSuplierInvoicedReceipts(addressBook, cfdiRel);
			if(invReceipts == null) {
				return "No existe la factura relacionada al CFDI de la nota de crédito";
			}else {
				if(invReceipts.size() > 0) {
					double invTotalAmt = 0;
					double invForeignTotalAmt = 0;
					double credTotalAmt = 0;					
					credTotalAmt = inv.getSubTotal();
					
					for(Receipt r : invReceipts) {
						invTotalAmt = invTotalAmt + r.getAmountReceived();
						invForeignTotalAmt = invForeignTotalAmt + r.getForeignAmountReceived();
					}
					if(domesticCurrency.equals(invCurrency)) {
						if(invTotalAmt < credTotalAmt) {
							return "El total de la nota de crédito excede el total de las facturas correlacionadas";
						}
					} else {
						if(invForeignTotalAmt < credTotalAmt) {
							return "El total de la nota de crédito excede el total de las facturas correlacionadas";
						}
					}
				}else {
					return "No existe la factura relacionada al CFDI de la nota de crédito";
				}
			}
		}else {
			return "No existe CFDI relacionado en el documento";
		}
		// Valida subtotales
		
		if(s != null) {
			NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(s.getRfc(), 0, 0);
		    if (ncs != null && (
		      ncs.getRefDate1().contains("Definitivo") || 
		      ncs.getRefDate1().contains("Presunto") || 
		      ncs.getRefDate1().contains("Desvirtuado") || 
		      ncs.getRefDate2().contains("Definitivo") || 
		      ncs.getRefDate2().contains("Presunto") || 
		      ncs.getRefDate2().contains("Desvirtuado") || 
		      ncs.getStatus().contains("Definitivo") || 
		      ncs.getStatus().contains("Presunto") || 
		      ncs.getStatus().contains("Desvirtuado"))) {
		    	
		    	String altEmail = "";
					List<UDC> udcList =  udcService.searchBySystem("TAXALTEMAIL");
					if(udcList != null) {
						altEmail = udcList.get(0).getStrValue1();
					    EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						emailAsyncSup.setProperties(
								AppConstants.EMAIL_NO_COMPLIANCE_INVOICE,
								AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER + " Número: " + s.getAddresNumber() + "br /> Nombre: " + s.getRazonSocial(),
								altEmail);
						emailAsyncSup.setMailSender(mailSenderObj);
						Thread emailThreadSup = new Thread(emailAsyncSup);
						emailThreadSup.start();
					}
		    	
					 EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					 emailAsyncSup.setProperties(
								AppConstants.EMAIL_INVOICE_REJECTED + " " + po.getOrderNumber() ,
								AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER_NOTIF,
								altEmail);
						emailAsyncSup.setMailSender(mailSenderObj);
						Thread emailThreadSup = new Thread(emailAsyncSup);
						emailThreadSup.start();
					
					return "Los registros indican que cuenta con problemas fiscales y no se podrán cargar facturas en este momento.";

		    } 
		}else {
			return "El proveedor no existe en el catálogo de la aplicación";
		}
		
		
		String buyerEmail = po.getEmail();
		cal = Calendar.getInstance();
		invDate = null;
		Date orderDate = null;
		try {
			fechaFactura = inv.getFechaTimbrado();
			fechaFactura = fechaFactura.replace("T", " ");
			sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
			invDate = sdf.parse(fechaFactura);
			orderDate = po.getDateRequested();
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
		UDC udcDate = udcService.searchBySystemAndKey(AppConstants.NO_VALIDATE_DATE, "SKIP");
		if(udcDate != null) {
			if(!udcDate.getStrValue1().equals(s.getTipoProductoServicio())){
				if(invDate.before(orderDate)) {
					return "Error: La fecha de la nota de crédito no puede ser anterior a la fecha de emisión de la orden.";
				}
			}
		}

		
		if(rfcEmisor != null) {
			if(!"".equals(rfcEmisor)) {
				if(!s.getRfc().equals(rfcEmisor)) {
					return "La nota de crédito ingresada no pertenece al RFC del emisor del proveedor registrado como " + s.getRfc();
				}
			}
		}
		
		boolean receptorValido = false;
		List<UDC> receptores = udcService.searchBySystem("RECEPTOR");
		if(receptores != null) {
			for(UDC udc : receptores) {
				if(udc.getStrValue1().equals(inv.getRfcReceptor().trim())) {
					receptorValido = true;
					break;
				}
			}
		}
		if(!receptorValido) {
			return "El RFC receptor " + inv.getRfcReceptor() + " no es permitido para carga de documentos fiscales.";
		}
				
		// ValidaciÃ³n los importes del recibo:		
		if(AppConstants.NC_FIELD.equals(tipoComprobante)){
			Users u = usersService.searchCriteriaUserName(po.getAddressNumber());
			
			UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
			UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");
			
			UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");
			
			UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
			UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
			
			double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
			double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;
			
			double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
			double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
			double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
			
			double orderAmount = 0;
			double foreignOrderAmount = 0;
			double invoiceAmount = 0;			
			double discount = 0;

			for(Receipt r :requestedReceiptList) {
				orderAmount = orderAmount + Math.abs(r.getAmountReceived());
				foreignOrderAmount = foreignOrderAmount + Math.abs(r.getForeignAmountReceived());
			}

			String tipoValidacion ="";			
			double totalImporteMayor = 0;
			double totalImporteMenor = 0;
			double invoiceTotalAmount = 0;
			
			if(domesticCurrency.equals(invCurrency)) {
				totalImporteMayor = orderAmount;
				totalImporteMenor = orderAmount;
			} else {
				totalImporteMayor = foreignOrderAmount;
				totalImporteMenor = foreignOrderAmount;
			}
			
			invoiceTotalAmount = inv.getSubTotal();
			if(montoLimite != 0) {
				if(invoiceTotalAmount >= montoLimite) {
					totalImporteMayor = totalImporteMayor + montoLimiteMax;
					totalImporteMenor = totalImporteMenor - montoLimiteMin;
					tipoValidacion = "Por Monto";
				}else {
					totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
					totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
					tipoValidacion = "Por porcentaje";
				}
			}
			
			totalImporteMayor = totalImporteMayor * 100;
			totalImporteMayor = Math.round(totalImporteMayor);
			totalImporteMayor = totalImporteMayor /100;	
			
			totalImporteMenor = totalImporteMenor * 100;
			totalImporteMenor = Math.round(totalImporteMenor);
			totalImporteMenor = totalImporteMenor /100;				
			
			if(inv.getDescuento() != 0) {
				discount = inv.getDescuento();
				discount = discount * 100;
				discount = Math.round(discount);
				discount = discount /100;	
			}
			invoiceAmount = inv.getSubTotal() - discount;
			invoiceAmount = invoiceAmount * 100;
			invoiceAmount = Math.round(invoiceAmount);
			invoiceAmount = invoiceAmount/100;
			
			if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
				return "El total de la nota de crédito no coincide con el total de los recibos seleccionados. Tipo de validación:" + tipoValidacion;
				
			}else {				
				po.setInvoiceAmount(po.getInvoiceAmount() + inv.getTotal());
		        po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
		        po.setInvoiceUploadDate(invDate);
		        po.setSentToWns(null);
		        purchaseOrderService.updateOrders(po);
		        
		        for(Receipt r :requestedReceiptList) {
		        	r.setQuantityReceived(Math.abs(r.getQuantityReceived()));
		        	r.setInvDate(invDate);
		        	r.setFolio(inv.getFolio());
		        	r.setSerie(inv.getSerie());
		        	r.setUuid(inv.getUuid());
		        	r.setUploadInvDate(new Date());
					r.setStatus(AppConstants.STATUS_OC_INVOICED);
				}
				purchaseOrderService.updateReceipts(requestedReceiptList);
			}

		}
		}
		
		if(sendVoucher) {
			try {				
				if(domesticCurrency.equals(invCurrency)) {
					resp = "DOC:" + eDIService.createNewVoucher(po, inv, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
				} else {
					ForeingInvoice fi = new ForeingInvoice();
					fi.setSerie(inv.getSerie());
					fi.setFolio(inv.getFolio());
					fi.setUuid(inv.getUuid());
					fi.setExpeditionDate(inv.getFechaTimbrado());
					resp = "DOC:" + eDIService.createNewForeignVoucher(po, fi, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
				}
				
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(AppConstants.EMAIL_NC_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(AppConstants.EMAIL_CN_ACCEPTED + po.getOrderNumber() + "-" + po.getOrderType() + "<br /> <br />" + AppConstants.ETHIC_CONTENT), emailRecipient);
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();				
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return "";
			}
		}
		return "";
	}
	
	@SuppressWarnings("unused")
	public String validateInvoiceFromOrderWitCreditNote(InvoiceDTO inv, InvoiceDTO cn, 
								                     String addressBook, 
													 int documentNumber, 
													 String documentType,
													 String tipoComprobante,
													 PurchaseOrder po,
													 boolean sendVoucher,
													 String xmlInvContent,
													 String xmlNCContent){

		
		Supplier s = supplierService.searchByAddressNumber(addressBook);
		String resp="";
		String emailRecipient = (s.getEmailSupplier());
		
		String vcfdi = validaComprobanteSAT(inv);
		if(!"".equals(vcfdi)) {
			return vcfdi;
		}
		

		String rfcEmisor = inv.getRfcEmisor();
		if(rfcEmisor != null) {
			if(!"".equals(rfcEmisor)) {
				if(!s.getRfc().equals(rfcEmisor)) {
					return "La factura no pertenece al emisor " + s.getRfc();
				}
			}
		}
		
		rfcEmisor = cn.getRfcEmisor();
		if(rfcEmisor != null) {
			if(!"".equals(rfcEmisor)) {
				if(!s.getRfc().equals(rfcEmisor)) {
					return "La nota de crédito no pertenece al emisor " + s.getRfc();
				}
			}
		}
		

		boolean receptorFacturaValido = false;
		List<UDC> receptores = udcService.searchBySystem("RECEPTOR");
		if(receptores != null) {
			for(UDC udc : receptores) {
				if(udc.getStrValue1().equals(inv.getRfcReceptor().trim())) {
					receptorFacturaValido = true;
					break;
				}
			}
		}
		
		boolean receptorNCValido = false;
		if(receptores != null) {
			for(UDC udc : receptores) {
				if(udc.getStrValue1().equals(cn.getRfcReceptor().trim())) {
					receptorNCValido = true;
					break;
				}
			}
		}
		
		
		
		if(!receptorNCValido || !receptorFacturaValido) {
			return "El receptor de uno de los documentos no es permitido. Verifique que ambos documentos tengan Emisor/Receptor correcto.";
		}
		

		if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
			
			UDC udc = udcService.searchBySystemAndKey(AppConstants.INVOICE_FIELD_UDC, "MF_PAGO");
			if(udc != null){
				if(!udc.getStrValue1().equals(inv.getMetodoPago())){
					return "El método de pago de la FACTURA debe ser " + udc.getStrValue1();	
				}
				if(!udc.getStrValue2().equals(inv.getFormaPago())){
					return "La forma de pago de la FACTURA debe ser " + udc.getStrValue2();	
				}
			}else{
				return "No existe método de pago para la factura";
			}
			
			List<PurchaseOrder> list = purchaseOrderService.getPendingPaymentOrders(documentNumber, 
                    addressBook, 
                    documentType);

			if (list != null && !list.isEmpty()) {
				String str = "";
				for (PurchaseOrder o : list) {
					str = str + o.getOrderNumber() + ",";
				}
				return "Los registros detectaron que tiene las siguientes órdenes de compra PAGADAS con COMPLEMENTOS DE PAGO Pendientes: <br /> "
						+ str;
			}
			
			Users u = usersService.searchCriteriaUserName(po.getAddressNumber());
			double orderAmount = po.getOrderAmount();
			double invoiceAmount = inv.getTotal();
			double cnAmount = cn.getTotal();
			double transAmount = invoiceAmount - cnAmount;
			
			if(orderAmount != transAmount) {
				return "El valor de la factura menos la nota de crédito no coinciden con el total recibido en la orde de compra.";
			}
			
			po.setInvoiceAmount(transAmount);
	        purchaseOrderService.updateOrders(po);
	        
			if(sendVoucher) {
				
				if(AppConstants.DEFAULT_CURRENCY.equals(inv.getMoneda())) {
					resp = "DOC:" + eDIService.createNewVoucher(po, inv, 0, s, null, AppConstants.NN_MODULE_VOUCHER);
				} else {
					ForeingInvoice fi = new ForeingInvoice();
					fi.setSerie(inv.getSerie());
					fi.setFolio(inv.getFolio());
					fi.setUuid(inv.getUuid());
					fi.setExpeditionDate(inv.getFechaTimbrado());
					resp = "DOC:" + eDIService.createNewForeignVoucher(po, fi, 0, s, null, AppConstants.NN_MODULE_VOUCHER);
				}
			}
		}

		if(sendVoucher) {
			emailService.sendEmail(AppConstants.EMAIL_INVOICE_SUBJECT, AppConstants.ETHIC_CONTENT_INVOICE, emailRecipient);
		}
		return "";
	}

	@SuppressWarnings("unused")
	public String processInvoiceAndCreditNoteFromOrder(InvoiceDTO inv, InvoiceDTO cn,
									                    String addressBook, 
														int documentNumber, 
														String documentType,
														PurchaseOrder po){
		
		String resp = "";
		eDIService.createJournalEntries(inv, cn, addressBook, documentNumber, documentType, po);
		return "";
	}	
	
	
	
	
	public String validateInvoiceFromOrderWithoutPayment(InvoiceDTO inv, String addressBook, int documentNumber, String documentType,
			String tipoComprobante, PurchaseOrder po, String xmlContent) {

		Supplier s = supplierService.searchByAddressNumber(addressBook);
		String emailRecipient = (s.getEmailSupplier());

		if (AppConstants.INVOICE_FIELD.equals(tipoComprobante)) {

			String rfcEmisor = inv.getRfcEmisor();
			if (rfcEmisor != null) {
				if (!"".equals(rfcEmisor)) {
					if (!s.getRfc().equals(rfcEmisor)) {
						return "La factura no pertenece al emisor " + s.getRfc();
					}
				}
			}
			
			String vcfdi = validaComprobanteSAT(inv);
			if(!"".equals(vcfdi)) {
				return vcfdi;
			}
		}

		emailService.sendEmail(AppConstants.EMAIL_INVOICE_SUBJECT,
				AppConstants.EMAIL_INVOICE_ACCEPTED_NOPAYMENT + po.getOrderNumber() + "-" + po.getOrderType() + "<br /> <br />" + AppConstants.ETHIC_CONTENT, emailRecipient);
		return "";
	}
	
	public void rejectInvoice(String addressBook, 
			  int documentNumber, 
			  String documentType) {
		
		PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
    	po.setInvoiceUuid("");
    	po.setStatus("");
    	purchaseOrderService.updateOrders(po);
    	Supplier s = supplierService.searchByAddressNumber(addressBook);
		String emailRecipient = (s.getEmailSupplier());
    	
    	emailService.sendEmail(AppConstants.EMAIL_INVOICE_SUBJECT, AppConstants.EMAIL_INVOICE_REJECTED + po.getOrderNumber() + "-" + po.getOrderType(), emailRecipient);

	}
	
	public void updateDocumentList(List<UserDocument> list) {
		documentsDao.updateDocumentList(list);
	}
	
	public String validaComprobanteSAT(InvoiceDTO inv) {	
		String payload = PayloadProducer.getCFDIPayload(inv);
		String response = HTTPRequestService.performSoapCall(payload);
		if(response != null) {
			JSONObject xmlJSONObj = XML.toJSONObject(response, true);
			JsonElement jelement = new JsonParser().parse(xmlJSONObj.toString());
			JsonObject jobject = jelement.getAsJsonObject();
			JsonElement soapEnvelope = jobject.get("s:Envelope").getAsJsonObject().get("s:Body").getAsJsonObject().get("ConsultaResponse").getAsJsonObject();
			JsonElement result = soapEnvelope.getAsJsonObject().get("ConsultaResult");
			
			String codigoEstatus = NullValidator.isNull(result.getAsJsonObject().get("a:CodigoEstatus").toString());
			String esCancelable = NullValidator.isNull(result.getAsJsonObject().get("a:EsCancelable").toString());
			String estado = NullValidator.isNull(result.getAsJsonObject().get("a:Estado").toString());
			String estatusCancelacion = NullValidator.isNull(result.getAsJsonObject().get("a:EstatusCancelacion").toString());

			codigoEstatus = codigoEstatus.replace("\"", "").trim();
			esCancelable = esCancelable.replace("\"", "").trim();
			estado = estado.replace("\"", "").trim();
			estatusCancelacion = estatusCancelacion.replace("\"", "").trim();
			
			if(!AppConstants.CFDI_SUCCESS_MSG.equals(codigoEstatus) || !AppConstants.CFDI_SUCCESS_MSG_ACTIVE.equals(estado)) {
				String errorMsg = "El documento no es aceptado ante el SAT. Se recibe el siguiente mensaje :<br>" +
			                       " - CodigoEstatus: " + codigoEstatus + "<br />" +
			                       " - EsCancelable: " + esCancelable + "<br />" +
			                       " - Estado: " + estado + "<br />" +
			                       " - EstatusCancelacion: " + estatusCancelacion + "<br />" ;
				return errorMsg;
			}
			
		}else {
			return "Error de validación ante el SAT, favor de validar con su emisor fiscal";	
		}
		
		return "";
		
	}
	
	public String validaComprobanteSATPagos(String xmlContent) {		
		return null;	
	}


	public UserDocument searchCriteriaByOrderNumberFiscalType(int orderNumber, 
			                                              String orderType, 
			                                              String addressNumber,
			                                              String type){
		
		return documentsDao.searchCriteriaByOrderNumberFiscalType(orderNumber, orderType, addressNumber, type);		
	}

	public String validaComplementoPago(String xmlString, String addressBook, InvoiceDTO inv) {
		 
		List<String> orders = new ArrayList<String>();
		String res = "";
			UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
			if(udcCfdi != null) {
				if(!"".equals(udcCfdi.getStrValue1())) {
					if("TRUE".equals(udcCfdi.getStrValue1())) {
	            		res = validaComprobanteSATPagos(xmlString);
	            		if(!"".equals(res)) {
	                    	return "El documento cargado no es aceptable ante el SAT. Verifique su archivo e intente nuevamente.";
	            		}
					}
				}
			}else {
     		res = validaComprobanteSATPagos(xmlString);
     		if(!"".equals(res)) {
             	return "El documento cargado no es aceptable ante el SAT. Verifique su archivo e intente nuevamente.";
     		}
			}
			
 		Supplier s = supplierService.searchByAddressNumber(addressBook);
 		
 		String rfcEmisor = inv.getRfcEmisor();
 		if(rfcEmisor != null) {
 			if(!"".equals(rfcEmisor)) {
 				if(!s.getRfc().equals(rfcEmisor)) {
                  return "El complemento de pago no pertenece al emisor " + s.getRfc();
 				}
 			}
 		}
 		
 		boolean receptorValido = false;
 		List<UDC> receptores = udcService.searchBySystem("RECEPTOR");
 		if(receptores != null) {
 			for(UDC udc : receptores) {
 				if(udc.getStrValue1().equals(inv.getRfcReceptor().trim())) {
 					receptorValido = true;
 					break;
 				}
 			}
 		}
 		
 		if(!receptorValido) {
         	return "El receptor " + inv.getRfcReceptor() + " no es permitido para carga de complementos";
 		}
     	
 		List<Pago> pagos = inv.getComplemento().getPago().getPago();
 		for(Pago p : pagos) {
 			List<DoctoRelacionado> dList = p.getDoctoRelacionado();
     		double montoPago = Double.valueOf(p.getMonto());
				double totalPropinas = 0;
				double montoFactura = 0;
				String uuid = "";
 			for(DoctoRelacionado d : dList) {
 				uuid = d.getIdDocumento().trim();
 				PurchaseOrder po = purchaseOrderService.searchbyOrderUuid(uuid);
 				if(po == null) {
                 	return "Error: El uuid " + uuid + " contenido en el complemento, no tiene una factura relacionada. Verifique que su complemento de pago contenga las facturas que previamenta ha enviado a SEPASA";
 				}else {
 	 				orders.add(String.valueOf(po.getId()));
 					if(!d.getMonedaDR().equals(po.getCurrecyCode())) {
	                    return "Error: La clave de moneda para el " + uuid + " son diferentes en el complemento y la factura.";
 					}
 					if(!d.getMetodoDePagoDR().equals(po.getPaymentType())) {
	                    return "Error: El metodo de pago para el " + uuid + " son diferentes en el complemento y la factura";
 					}
 					
 					String subStr = "";
 					for(PurchaseOrderDetail pd : po.getPurchaseOrderDetail()) {
 						subStr= pd.getItemDescription().trim();
 						if(subStr.contains(AppConstants.PROPINA_TEXT)) {
 							totalPropinas = totalPropinas + pd.getExtendedPrice();
 						}    						
 					}
 					
 					montoFactura = montoFactura + po.getInvoiceAmount() - po.getRelievedAmount();
					montoFactura = montoFactura * 100;
					montoFactura = (double) Math.round(montoFactura);
					montoFactura = montoFactura /100;
 					
 					if(d.getImpPagado() != null) {
 						if(!"".equals(d.getImpPagado())) {
 							Double impPagado = Double.valueOf(d.getImpPagado());
 							impPagado = impPagado * 100;
 							impPagado = (double) Math.round(impPagado);
 							impPagado = impPagado /100;
 							
 							double currentInvoiceAmount = po.getInvoiceAmount() - po.getRelievedAmount();
							currentInvoiceAmount = currentInvoiceAmount * 100;
							currentInvoiceAmount = (double) Math.round(currentInvoiceAmount);
							currentInvoiceAmount = currentInvoiceAmount /100;
 							
							if(impPagado != currentInvoiceAmount) {
     	                    	return "Error: El importe de la línea de pago para el " + uuid + " es diferente en el complemento y la factura. Importe Complemento=" + impPagado + " / Importe Factura=" + po.getInvoiceAmount();
         					}
 						}
 					}else {
	                    return "Error: El valor del importe pagado es incorrecto";
 					}
 				}
 			}

 			montoFactura = montoFactura + totalPropinas;
 			montoFactura = montoFactura * 100;
 			montoFactura = (double) Math.round(montoFactura);
 			montoFactura = montoFactura /100;
 			if(montoFactura != montoPago) {
             	return "Error: El importe total de pago del complemento es diferente al total de sus facturas considerando sus propinas. UUID: " + uuid + " /  Total Complemento:" + montoPago + " / Total Facturas:" + montoFactura;
 			}
 
 		}
 		
 		if("".equals(res) && orders.size() > 0){
    		
    		for(String i : orders) {
    			int id = Integer.valueOf(i);
    			PurchaseOrder po = purchaseOrderService.getOrderById(id);
    			
    			if(po.getPaymentUuid() == null) {
    				po.setPaymentUuid("");
    			}
    			
    			if("".equals(po.getPaymentUuid())) {
    				UserDocument doc = new UserDocument(); 
                	doc.setAddressBook(po.getAddressNumber());
                	doc.setDocumentNumber(po.getOrderNumber());
                	doc.setDocumentType(po.getOrderType());
                	doc.setContent(xmlString.getBytes());
                	doc.setName("COMPL_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml");
                	doc.setSize(xmlString.length());
                	doc.setStatus(true);
                	doc.setAccept(true);
                	doc.setFiscalType("ComplementoPago");
                	doc.setType("text/plain");
                	//doc.setType(tipoComprobante);
                	doc.setFolio(inv.getFolio());
                	doc.setSerie(inv.getSerie());
                	doc.setUuid(inv.getUuid());
                	doc.setUploadDate(new Date());
                	doc.setFiscalRef(0);
                	save(doc, new Date(), "");
                	
                	po.setPaymentUuid(inv.getUuid());
                	po.setStatus(AppConstants.STATUS_LOADCP);
                	po.setOrderStauts(AppConstants.STATUS_OC_PAYMENT_COMPL);

                	purchaseOrderService.updateOrders(po);
    			}else {
    				return "Ya se ha cargado el complemento de pago anteriormente";
    			}
    		} 
    	}
 		

		return "";
	}

	private byte[] getByteArrayFromZipInputStream(String fileName, String fileExt, int fileSize, ZipInputStream zis) {
		byte[] byteArrayFile;
		
		try {
	    	File tempFile = File.createTempFile(fileName, ".".concat(fileExt));
	        FileOutputStream out = new FileOutputStream(tempFile.getPath());
	        byte[] byteBuff = new byte[fileSize];
	        int bytesRead = 0;
	        
	        while ((bytesRead = zis.read(byteBuff)) != -1)
	        {
	            out.write(byteBuff, 0, bytesRead);
	        }
	        
	    	byteArrayFile = Files.readAllBytes(tempFile.toPath());	    	
	    	tempFile.delete();
	        out.close();
	        zis.closeEntry();
		} catch (Exception e) {	
			log4j.error("Exception" , e);
			e.printStackTrace();
			byteArrayFile = null;
		}
		
		return byteArrayFile;
	}

	@SuppressWarnings("unused")
	public void restoreInvoice(List<InvoiceRequestDTO> list) {
		
		try {
			for(InvoiceRequestDTO request : list) {
				log4j.info("\n" + request.toString());
				
				PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(request.getDocumentNumber(), request.getAddressBook(), request.getDocumentType());
				Supplier s = supplierService.searchByAddressNumber(request.getAddressBook());				
				List<UserDocument> docs = documentsDao.searchCriteria(request.getUuid());
				
				InvoiceDTO inv = new InvoiceDTO();
				for(UserDocument doc : docs) {					
					if(doc.getType().trim().equals("text/xml")) {
						ByteArrayInputStream stream = new  ByteArrayInputStream(doc.getContent());
						String xmlContent = IOUtils.toString(stream, "UTF-8");
						
						if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
							inv = xmlToPojoService.convertV4(xmlContent);
						} else {
							inv = xmlToPojoService.convert(xmlContent);
						}
						log4j.info("Se crea InvoiceDTO..");
						break;
					}
				}
				
				List<Receipt> requestedReceiptList = null;
				List<Receipt> receiptArray= purchaseOrderService.getOrderReceipts(request.getDocumentNumber(), request.getAddressBook(), request.getDocumentType(),po.getOrderCompany());
				
				if(receiptArray != null) {
					//String[] idList = request.getReceiptIdList().split(",");
					requestedReceiptList = new ArrayList<Receipt>();
					for(Receipt r : receiptArray) {
						if(r.getUuid() != null && !r.getUuid().trim().isEmpty()) {
							if(request.getUuid().equals(r.getUuid().trim())) {
								requestedReceiptList.add(r);		
							}
						}
					}
				}
				log4j.info("Se obtienen " + requestedReceiptList.size() + " recibos.");
				
				if(requestedReceiptList != null && !requestedReceiptList.isEmpty()) {
					for(Receipt r :requestedReceiptList) {
			        	r.setFolio(inv.getFolio());
			        	r.setSerie(inv.getSerie());
			        	r.setUuid(inv.getUuid());
			        	
						if(AppConstants.RECEIPT_CODE_RETENTION.equals(r.getReceiptType())) {
							r.setAmountReceived(r.getAmountReceived() * -1);
							r.setForeignAmountReceived(r.getForeignAmountReceived() * -1);
							r.setQuantityReceived(Math.abs(r.getQuantityReceived()));
						}
					}
					
					String resp;
					if(AppConstants.DEFAULT_CURRENCY.equals(inv.getMoneda())) {
						resp = "DOC:" + eDIService.createNewVoucher(po, inv, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
					} else {
						ForeingInvoice fi = new ForeingInvoice();
						fi.setSerie(inv.getSerie());
						fi.setFolio(inv.getFolio());
						fi.setUuid(inv.getUuid());
						fi.setExpeditionDate(inv.getFechaTimbrado());
						resp = "DOC:" + eDIService.createNewForeignVoucher(po, fi, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
					}					
				}

				//Se envÃ­a el archivo PDF
				for(UserDocument doc : docs) {					
					if(doc.getType().trim().equals("application/pdf")) {
	                	File file = new File(System.getProperty("java.io.tmpdir")+"/"+ inv.getUuid() + ".pdf");
	                	Path filePath = Paths.get(file.getAbsolutePath());
	                	Files.write(filePath, doc.getContent());	                	
	                	this.sendFileToRemote(file, inv.getUuid() + ".pdf");	                	
	                	log4j.info("Se envía archivo " + inv.getUuid() + ".pdf");
		            	file.delete();
					}
				}
			}
			log4j.info("Termina proceso.....");
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	public void restoreInvoiceFile(List<String> list) {
		
		try {
			for(String uuid : list) {
				List<UserDocument> docs = documentsDao.searchCriteria(uuid);

				//Se envÃ­a el archivo PDF
				for(UserDocument doc : docs) {					
					if(doc.getType().trim().equals("application/pdf")) {
	                	File file = new File(System.getProperty("java.io.tmpdir")+"/"+ uuid + ".pdf");
	                	Path filePath = Paths.get(file.getAbsolutePath());
	                	Files.write(filePath, doc.getContent());	                	
	                	this.sendFileToRemote(file, uuid + ".pdf");	                	
	                	log4j.info("Se envÃ­a archivo " + uuid + ".pdf");
		            	file.delete();
					}
				}
			}
			log4j.info("Termina proceso.....");
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
	}

	@SuppressWarnings("unused")
	public void restoreForeignInvoice(List<InvoiceRequestDTO> list) {
		
		try {
			for(InvoiceRequestDTO request : list) {
				log4j.info("\n" + request.toString());
				
				PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(request.getDocumentNumber(), request.getAddressBook(), request.getDocumentType());
				Supplier s = supplierService.searchByAddressNumber(request.getAddressBook());
				
				ForeingInvoice fi = new ForeingInvoice();
				fi.setAddressNumber(request.getAddressBook());
				fi.setOrderNumber(request.getDocumentNumber());
				fi.setOrderType(request.getDocumentType());
				
				ForeignInvoiceTable fit = purchaseOrderDao.getForeignInvoice(fi);
				
				if(fit != null && fit.getUuid() != null && !fit.getUuid().trim().isEmpty() 
						&& fit.getUuid().trim().equals(request.getUuid())) {
					
					fi.setCountry(fit.getCountry());
					fi.setExpeditionDate(fit.getExpeditionDate());
					fi.setForeignCurrency(fit.getForeignCurrency());
					fi.setForeignDebit(fit.getForeignDebit());
					fi.setForeignDescription(fit.getForeignDescription());
					fi.setForeignNotes(fit.getForeignNotes());
					fi.setForeignRetention(fit.getForeignRetention());
					fi.setForeignSubtotal(fit.getForeignSubtotal());
					fi.setForeignTaxes(fit.getForeignTaxes());
					fi.setInvoiceNumber(fit.getInvoiceNumber());
					fi.setTaxId(fit.getTaxId());
					fi.setUsuarioImpuestos(fit.getUsuarioImpuestos());
					fi.setUuid(fit.getUuid());
				} else {
					log4j.info("No se encontrÃ³ la factura forÃ¡nea: " + request.getUuid() + ".");
					continue;
				}
				
				
				List<Receipt> requestedReceiptList = new ArrayList<Receipt>();
				List<Receipt> receiptArray= purchaseOrderService.getOrderReceipts(request.getDocumentNumber(), request.getAddressBook(), request.getDocumentType(),po.getOrderCompany());
				
				//Verificar que no estÃ© registrado el uuid en recibos
				if(receiptArray != null) {					
					for(Receipt r : receiptArray) {
						if(r.getUuid() != null && !r.getUuid().trim().isEmpty()) {
//							if(request.getUuid().equals(r.getUuid().trim())) {
								requestedReceiptList.add(r);		
//							}
						}
					}
				}
				
				if(requestedReceiptList.isEmpty()) {
					log4j.info("No se encontraron recibos para la factura forÃ¡nea: " + request.getUuid() + ".");
				} else {
					log4j.info("Se obtienen " + requestedReceiptList.size() + " recibos.");
					
					try {					
						String resp = "DOC:" + eDIService.createNewForeignVoucher(po, fi, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
						
						//Enviar primer archivo adjunto a la factura foranea
						try {
							List<UserDocument> listDocument = documentsDao.searchCriteria(request.getUuid());						
							if(listDocument != null && !listDocument.isEmpty()) {
								for(UserDocument document : listDocument) {
									if(document.getName() != null && document.getName().toLowerCase().contains(".pdf") 
											&& document.getContent() != null && org.apache.commons.lang.StringUtils.isBlank(document.getUuid())) {
										
					                	File file = new File(System.getProperty("java.io.tmpdir")+"/"+ fit.getUuid() + ".pdf");
					                	Path filePath = Paths.get(file.getAbsolutePath());
					                	Files.write(filePath, document.getContent());
					                	document.setUuid(fit.getUuid());
					                	this.sendFileToRemote(file, fit.getUuid() + ".pdf");				                
					                	this.update(document, null, null);
					                	log4j.info("FACTURA FORANEA: Se envÃ­a archivo " + fit.getUuid() + "");
					                	file.delete();
					                	break;
									}
								}
							}
						} catch (Exception e) {
							log4j.error("Exception" , e);
							e.printStackTrace();
						}
						
					}catch(Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();
					}
				}

			}
			log4j.info("Termina proceso.....");
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
	}
	
	public void sendFileToRemote(byte[] content, String fileName) {

		List<File> files = new ArrayList<File>();
		eDIService.sendFiles(files);
	}

	public void sendFileToRemote(File file, String fileName) {
		List<File> files = new ArrayList<File>();
		files.add(file);
		eDIService.sendFiles(files);
	}
	
	public String taxRoundingValidation(double importe, double tasaOCuota, double base) {
		
		if(base > 0 && importe > 0){
			double exp6 = (double) (Math.pow(10, -6)/2);
			double exp12 = (double) Math.pow(10, -12);
			double expDif = exp6 - exp12;
			
			double limInf = (base - exp6) * tasaOCuota;
			double limSup = (base + expDif) * tasaOCuota;
			
			String limInfStr = AppConstants.truncate(String.valueOf(limInf), 2);
			String limSupStr = AppConstants.round(limSup);
			
			double limInfDbl = Double.valueOf(limInfStr);
			double limSupDbl = Double.valueOf(limSupStr);
			
			if(importe >= limInfDbl) {
				if(importe <= limSupDbl) {
					return "";
				}else {
					return " LÃ­mite inferior " + limInfStr + " y superior "  + limSupStr;
				}
			}else {
				return " LÃ­mite inferior " + limInfStr + " y superior "  + limSupStr;
			}
		}
		
		return "";
		
	}
	
	private String validateInvNull(InvoiceDTO inv) {
		
		if(inv.getSello() == null) {
			return "Sello";
		}
		
		if(inv.getCertificado() == null) {
			return "Certificado";
		}
		
		if(inv.getLugarExpedicion() == null) {
			return "LugarExpedicion";
		}
		
		if(inv.getFecha() == null) {
			return "Fecha";
		}
		
		if(inv.getFechaTimbrado() == null) {
			return "FechaTimbrado";
		}
		
		if(inv.getSubTotal() == 0) {
			return "SubTotal";
		}
		
		if(inv.getMoneda() == null) {
			return "Moneda";
		}
		
		if(inv.getTotal() == 0) {
			return "Total";
		}
		
		if(inv.getRfcEmisor() == null) {
			return "RfcEmisor";
		}
		
		if(inv.getRfcReceptor() == null) {
			return "RfcReceptor";
		}
		
		if(inv.getReceptor() == null) {
			return "Receptor";
		}else {
			if(inv.getReceptor().getUsoCFDI() == null) {
				return "UsoCFDI";
			}
		}
		
		if(inv.getEmisor() == null) {
			return "Emisor";
		}else {
			if(inv.getEmisor().getRegimenFiscal() == null) {
				return "RegimenFiscal";
			}
		}
	
		if(inv.getConcepto() == null) {
			return "Concepto";
		}else {
			List<Concepto> conceptos = inv.getConcepto();
			if(conceptos != null) {
				for(Concepto concepto : conceptos) {
					
					if(concepto.getClaveProdServ() == null) {
						return "ClaveProdServ";
					}
					
					Impuestos impuestos = concepto.getImpuestos();
					if(impuestos != null) {
						Traslados traslados = impuestos.getTraslados();
						if(traslados != null) {
							List<Traslado> traslado = traslados.getTraslado();
							if(traslado != null) {
								for(Traslado t : traslado) {
									
									if(t.getTipoFactor() == null) {
										return "TipoFactor";
									} else {
										if("Tasa".equals(t.getTipoFactor())) {
											if(t.getTasaOCuota() == null) {
												return "TasaOCuota";
											}
										}									
									}
									
									//if(t.getBase() == null) {
									//	return "Base";
									//}
								}
							}
						}
					}					
					
					/*
					Retenciones retenciones = impuestos.getRetenciones();
					if(retenciones != null) {
						List<Retencion> retencion = retenciones.getRetencion();
						if(retencion != null) {
							for(Retencion t : retencion) {
								if(t.getTasaOCuota() == null) {
									return false;
								}
								
								if(t.getTipoFactor() == null) {
									//return false;
								}
								
								if(t.getBase() == null) {
									return false;
								}
							}
						}
					}
					*/
				}
			}
		}

		return "";
	}

	public List<UserDocumentDTO> getListFacByDate(Date poFromDate, Date poToDate, String userName, String role,String modo) {
		
		List<UserDocumentDTO>listaRes=new ArrayList<UserDocumentDTO>();
		
		listaRes=documentsDao.searchCriteriaByDate(poFromDate,poToDate,modo);
		
		return listaRes;
		
	}
	
public List<UserDocumentDTO> getListFac(String modo) {
		
		List<UserDocumentDTO>listaRes=new ArrayList<UserDocumentDTO>();
		
		listaRes=documentsDao.searchCriteriaListFac(modo);
		
		return listaRes;
		
	}
	

	
	
	public String createReportExcel(Users user,List<UserDocumentDTO> facturas,List<String>Encontradas,List<UserDocumentDTO> facturasaduanal,List<String> factFound, HashMap<String, String> listFilesPdf){
        try {
        	String filepat=udcService.searchBySystemAndKey("PATH", "TEMPPATH").getStrValue1()+UUID.randomUUID()+".xlsx";
            XSSFWorkbook wb = new XSSFWorkbook();
            
            wb.createSheet("Facturas");
            wb.createSheet("Facturas Aduanales");
            XSSFSheet  hssfSheet = wb.getSheetAt(0);
            
            XSSFRow hssfRow;
            int initRow=1;
            
            CellStyle cs = wb.createCellStyle();
            cs.setWrapText(true);
            hssfSheet.createRow(0).createCell(0,Cell.CELL_TYPE_STRING).setCellValue("Record_Type");
            hssfSheet.getRow(0).createCell(1,Cell.CELL_TYPE_STRING).setCellValue("No_de_Proveedor");
            hssfSheet.getRow(0).createCell(2,Cell.CELL_TYPE_STRING).setCellValue("Orden_de_Compra");
            hssfSheet.getRow(0).createCell(3,Cell.CELL_TYPE_STRING).setCellValue("No_de_Factura");
            hssfSheet.getRow(0).createCell(4,Cell.CELL_TYPE_STRING).setCellValue("UUID");
            hssfSheet.getRow(0).createCell(5,Cell.CELL_TYPE_STRING).setCellValue("Fecha_de_Ingreso_de_la_Factura");
            hssfSheet.getRow(0).createCell(6,Cell.CELL_TYPE_STRING).setCellValue("Estatus del Registro");
            hssfSheet.getRow(0).createCell(7,Cell.CELL_TYPE_STRING).setCellValue("Estatus del PDF"); ///22/05/10 se agrego la columna de estatus de pdf
            
            log4j.info("Encontrados en base: "+facturas.size());
            for (UserDocumentDTO docfac:facturas) {
            	hssfRow=hssfSheet.createRow(initRow);
            	initRow=initRow+1;
            	
            	XSSFCell cell0=hssfRow.createCell(0,Cell.CELL_TYPE_STRING);
            	cell0.setCellValue(docfac.getDocumentType()); 
            	cell0.setCellStyle(cs);
            	
            	XSSFCell cell1=hssfRow.createCell(1,Cell.CELL_TYPE_STRING);
            	cell1.setCellValue(docfac.getAddressBook());
            	cell1.setCellStyle(cs);
            	
            	XSSFCell cell2=hssfRow.createCell(2,Cell.CELL_TYPE_STRING);
            	cell2.setCellValue(docfac.getDocumentNumber()); 
            	cell2.setCellStyle(cs);
            	
            	XSSFCell cell3=hssfRow.createCell(3,Cell.CELL_TYPE_STRING);
            	cell3.setCellValue(docfac.getSerie()); 
            	cell3.setCellStyle(cs);
            	
            	XSSFCell cell4=hssfRow.createCell(4,Cell.CELL_TYPE_STRING);
            	cell4.setCellValue(docfac.getUuid()); 
            	cell4.setCellStyle(cs);
            	
            	XSSFCell cell5=hssfRow.createCell(5,Cell.CELL_TYPE_STRING);
            	cell5.setCellValue(docfac.getName());
            	cell5.setCellStyle(cs);
            	
            	for (String string : Encontradas) {
    				if(docfac.getUuid().trim().equals(string.trim())) {
    					XSSFCell cell6=hssfRow.createCell(6,Cell.CELL_TYPE_STRING);
    	            	cell6.setCellValue("Encontrado");
    	            	CellStyle csf = wb.createCellStyle();
    	                csf.setWrapText(true);
    	                csf.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
    	            	cell6.setCellStyle(csf);
    	            	
    				break;
    				}
    			}
            	
            	XSSFCell cell7=hssfRow.createCell(7,Cell.CELL_TYPE_STRING);
            	cell7.setCellValue(listFilesPdf.get(docfac.getUuid().trim())==null?"":"Encontrado");
            	cell7.setCellStyle(cs);
            	
            	
            }
            
            hssfSheet.autoSizeColumn(0);
            hssfSheet.autoSizeColumn(1);
            hssfSheet.autoSizeColumn(2);
            hssfSheet.autoSizeColumn(3);
            hssfSheet.autoSizeColumn(4);
            hssfSheet.autoSizeColumn(5);
            hssfSheet.autoSizeColumn(6);
            hssfSheet.autoSizeColumn(7);
            
            hssfSheet = wb.getSheetAt(1);
            
            hssfSheet.createRow(0).createCell(0,Cell.CELL_TYPE_STRING).setCellValue("Record_Type");
            hssfSheet.getRow(0).createCell(1,Cell.CELL_TYPE_STRING).setCellValue("No_de_Proveedor");
            hssfSheet.getRow(0).createCell(2,Cell.CELL_TYPE_STRING).setCellValue("Orden_de_Compra");
            hssfSheet.getRow(0).createCell(3,Cell.CELL_TYPE_STRING).setCellValue("No_de_Factura");
            hssfSheet.getRow(0).createCell(4,Cell.CELL_TYPE_STRING).setCellValue("UUID");
            hssfSheet.getRow(0).createCell(5,Cell.CELL_TYPE_STRING).setCellValue("Fecha_de_Ingreso_de_la_Factura");
            hssfSheet.getRow(0).createCell(6,Cell.CELL_TYPE_STRING).setCellValue("Estatus del Registro");
            hssfSheet.getRow(0).createCell(7,Cell.CELL_TYPE_STRING).setCellValue("Estatus del PDF");
            
            initRow=1;
            for (UserDocumentDTO docfac:facturasaduanal) {
            	hssfRow=hssfSheet.createRow(initRow);
            	initRow=initRow+1;
            	
            	XSSFCell cell0=hssfRow.createCell(0,Cell.CELL_TYPE_STRING);
            	cell0.setCellValue(docfac.getDocumentType()); 
            	cell0.setCellStyle(cs);
            	
            	XSSFCell cell1=hssfRow.createCell(1,Cell.CELL_TYPE_STRING);
            	cell1.setCellValue(docfac.getAddressBook());
            	cell1.setCellStyle(cs);
            	
            	XSSFCell cell2=hssfRow.createCell(2,Cell.CELL_TYPE_STRING);
            	cell2.setCellValue(docfac.getDocumentNumber()); 
            	cell2.setCellStyle(cs);
            	
            	XSSFCell cell3=hssfRow.createCell(3,Cell.CELL_TYPE_STRING);
            	cell3.setCellValue(docfac.getSerie()); 
            	cell3.setCellStyle(cs);
            	
            	XSSFCell cell4=hssfRow.createCell(4,Cell.CELL_TYPE_STRING);
            	cell4.setCellValue(docfac.getUuid()); 
            	cell4.setCellStyle(cs);
            	
            	XSSFCell cell5=hssfRow.createCell(5,Cell.CELL_TYPE_STRING);
            	cell5.setCellValue(docfac.getName());
            	cell5.setCellStyle(cs);
            	
            	for (String string : factFound) {
    				if(docfac.getUuid().trim().equals(string.trim())) {
    					XSSFCell cell6=hssfRow.createCell(6,Cell.CELL_TYPE_STRING);
    	            	cell6.setCellValue("Encontrado");
    	            	CellStyle csf = wb.createCellStyle();
    	                csf.setWrapText(true);
    	                csf.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
    	            	cell6.setCellStyle(csf);
    	            	
    				break;
    				}
    			}
            	
            	XSSFCell cell7=hssfRow.createCell(7,Cell.CELL_TYPE_STRING);
            	cell7.setCellValue(listFilesPdf.get(docfac.getUuid().trim())==null?"":"Encontrado");
            	cell7.setCellStyle(cs);
            	
            	
            }
            
            hssfSheet.autoSizeColumn(0);
            hssfSheet.autoSizeColumn(1);
            hssfSheet.autoSizeColumn(2);
            hssfSheet.autoSizeColumn(3);
            hssfSheet.autoSizeColumn(4);
            hssfSheet.autoSizeColumn(5);
            hssfSheet.autoSizeColumn(6);
            hssfSheet.autoSizeColumn(7);
            
            
            
            FileOutputStream fos=new FileOutputStream(filepat);
            wb.write(fos);
            try {
				fos.close();
			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}
            File file=new File(filepat);
            emailService.sendEmailWithAttachExcel("Reporte de Facturas", "Reporte de facturas", user.getEmail(), file);
            
        } catch (FileNotFoundException fileNotFoundException) {
        	log4j.error("The file not exists (No se encontrÃ³ el fichero): " + fileNotFoundException);
        } catch (IOException ex) {
        	log4j.error("Error in file procesing (Error al procesar el fichero): " + ex);
        } finally {
          
        }
		return null;
    }
	
	public List<String> stringWithPipesToList(String currentString){
		List<String> list = new ArrayList<String>();		
		String[] splitArray = null;		
		
		try {
			if(!"".equals(currentString)) {
				currentString = currentString.replace(" ", "");
				
				if(currentString.contains("|")) {
					splitArray = currentString.split("\\|");
				} else {
					splitArray = new String[]{currentString};
				}
				
				list = Arrays.asList(splitArray);
			}
		} catch (Exception e) {	
			log4j.error("Exception" , e);
		}
		
		return list;
	}
	
	public List<String> getTranslatedTaxList(InvoiceDTO inv) {
		List<String> translatedTaxList = new ArrayList<String>();
		List<Concepto> conceptos = inv.getConcepto();
		if(conceptos != null) {
			for(Concepto concepto : conceptos) {
				Impuestos impuestos = concepto.getImpuestos();
				if(impuestos != null) {
					Traslados traslados = impuestos.getTraslados();
					if(traslados != null) {
						List<Traslado> traslado = traslados.getTraslado();
						if(traslado != null) {
							for(Traslado t : traslado) {
								if(t.getTasaOCuota() != null){
									String tasaOCuota = t.getTasaOCuota().trim();
									if(!"".equals(tasaOCuota) && !translatedTaxList.contains(tasaOCuota)) {
										translatedTaxList.add(tasaOCuota);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return translatedTaxList;
	}
	
	public List<String> getRetainedTaxList(InvoiceDTO inv) {
		List<String> retainedTaxList = new ArrayList<String>();
		List<Concepto> conceptos = inv.getConcepto();
		
		if(conceptos != null) {
			for(Concepto concepto : conceptos) {
				Impuestos impuestos = concepto.getImpuestos();
				if(impuestos != null) {
					Retenciones retenciones = impuestos.getRetenciones();
					if(retenciones != null) {
						List<Retencion> retencion = retenciones.getRetencion();
						if(retencion != null) {
							for(Retencion t : retencion) {
								if(t.getTasaOCuota()!= null) {
									String tasaOCuota = t.getTasaOCuota().trim();
									if(!"".equals(tasaOCuota) && !retainedTaxList.contains(tasaOCuota)) {
										retainedTaxList.add(tasaOCuota);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return retainedTaxList;
	}
	
	public double getISHAmount(InvoiceDTO inv) {
		double dblISHAmount = 0;
		
		if(inv.getTotalImpLocTraslados() > 0) {
			if(inv.getComplemento() != null && inv.getComplemento().getImpuestosLocales() != null
					&& inv.getComplemento().getImpuestosLocales().getTrasladosLocales() != null) {				
				for(TrasladosLocales tl :inv.getComplemento().getImpuestosLocales().getTrasladosLocales()) {					
					if(tl.getImpLocTrasladado() != null && AppConstants.LOCAL_TAX_ISH.equals(tl.getImpLocTrasladado().trim())
						&& tl.getImporte() != null && !"".equals(tl.getImporte().trim())) {
						dblISHAmount += Double.parseDouble(tl.getImporte().trim());
					}
				}
			}
			
			dblISHAmount = dblISHAmount * 100;
			dblISHAmount = Math.round(dblISHAmount);
			dblISHAmount = dblISHAmount /100;	
		}
		
		return dblISHAmount;
	}
	
	public List<UserDocument> searchCriteriaByIdList(long ticketId){
		Supplier sup = supplierService.searchByTicket(ticketId);
		List<Integer> idList = new ArrayList<Integer>();
		if(sup != null) {
			String fileList = sup.getFileList();
			String[] r1 = fileList.split("_FILE:");
			int inx = r1.length;
			for (int index = 0; index < inx; index++) {
				if(!"".equals(r1[index])) {
	        		String[] r2 = r1[index].split("_:_");
		        		if(r2 != null) {
			        		if(r2[0]  != "" && r2[0] != null){
			        			idList.add(Integer.valueOf(r2[0]));
		        		}
	        		}
				}
        	} 
		}
		
		List<UserDocument> docs = documentsDao.searchCriteriaByIdList(idList);
		if(docs != null) {
			for(UserDocument doc : docs) {
				doc.setContent(null);
				String originalString = String.valueOf(doc.getId());
				try {
				doc.setUuid(AESEncrypt.encrypt(originalString, AESEncrypt.AES_CUSTOM_KEY));
				}catch(Exception e) {
					return null;
				}
			}
		}
		return docs;
	}
	
	public UserDocument searchSecuredDocument(String uuid){
		try {
			String decryptedString = AESEncrypt.decrypt(uuid, AESEncrypt.AES_CUSTOM_KEY);
			int id = Integer.valueOf(decryptedString);
			return documentsDao.getDocumentById(id);
		}catch(Exception e) {
			return null;
		}
	}

	public List<UserDocument> searchCriteriaByUuidOnly(String uuid) {
		return documentsDao.searchCriteriaByUuidOnly(uuid);
	}
	
	@SuppressWarnings({ "unused" })
	public String validateInvoiceWithoutReceipt(InvoiceDTO inv,
								  String addressBook, 
								  long documentNumber, 
								  String documentType,
								  String tipoComprobante,
								  PurchaseOrder po,
								  boolean sendVoucher,
								  String xmlContent){
		boolean isTaxValidationOn = true;
		String receiptTaxCode = "";
		
		String resp = "";
		Supplier s = supplierService.searchByAddressNumber(addressBook);
		if(s == null) {
			return "El proveedor no existe en la base de datos.";
		}
		
		DecimalFormat currencyFormat = new DecimalFormat("$#,###.###");
		UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
		
		if(udcCfdi != null) {
			if(!"".equals(udcCfdi.getStrValue1())) {
				if("TRUE".equals(udcCfdi.getStrValue1())) {
					String vcfdi = validaComprobanteSAT(inv);
					if(!"".equals(vcfdi)) {
						return "Error de validaciÃ³n ante el SAT, favor de validar con su emisor fiscal.";
					}
					
					String vNull = validateInvNull(inv);
					if(!"".equals(vNull)) {
						return "Error al validar el archivo XML, no se encontrÃ³ el campo " + vNull + ".";
					}
				}
			}
		}else {
			String vcfdi = validaComprobanteSAT(inv);
			if(!"".equals(vcfdi)) {
				return "Error de validaciÃ³n ante el SAT, favor de validar con su emisor fiscal.";
			}
			
			String vNull = validateInvNull(inv);
			if(!"".equals(vNull)) {
				return "Error al validar el archivo XML, no se encontrÃ³ el campo " + vNull + ".";
			}
		}
		
		String rfcEmisor = inv.getRfcEmisor();
		boolean allRules = true;
		List<UDC> supExclList =  udcService.searchBySystem("NOCHECKSUP");
		if(supExclList != null) {
			for(UDC udc : supExclList) {
				if(rfcEmisor.equals(udc.getStrValue1())){
					allRules = false;
					break;
				}
			}
		}
				
		boolean isHotelSupplier = false;		
		List<UDC> hotelSupList =  udcService.searchBySystem("HOTELSUPPLIER");
		if(hotelSupList != null) {
			for(UDC udc : hotelSupList) {
				if(addressBook.equals(udc.getUdcKey())){
					isHotelSupplier = true;
					isTaxValidationOn = false;
					break;
				}
			}
		}
		
		boolean currentYearRule = true;
		List<UDC> noCurrentYearList =  udcService.searchBySystem("NOCURRENTYEAR");
		if(noCurrentYearList != null) {
			for(UDC udc : noCurrentYearList) {
				if(rfcEmisor.equals(udc.getStrValue1())){
					currentYearRule = false;
					break;
				}
			}
		}

		int diasCred = 30;
		/*
		if(s.getDiasCredito() != null && !s.getDiasCredito().isEmpty()) {
			diasCred = Integer.valueOf(s.getDiasCredito());
		} 
		*/
		
		String emailRecipient = (s.getEmailSupplier());
		//List<Receipt> requestedReceiptList = null;

	//	List<Receipt> receiptArray= purchaseOrderService.getOrderReceipts(documentNumber, addressBook, documentType, po.getOrderCompany());
		
		String fechaFactura = inv.getFechaTimbrado();
		fechaFactura = fechaFactura.replace("T", " ");
		SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
		Date invDate = null;
		try {
			invDate = sdf.parse(fechaFactura);
		}catch(Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}

		String invCurrency = inv.getMoneda().trim();
		double exchangeRate = inv.getTipoCambio();
		
		/*if(receiptArray != null) {
			String[] idList = receiptList.split(",");
			requestedReceiptList = new ArrayList<Receipt>();
			for(Receipt r : receiptArray) {
				if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
					requestedReceiptList.add(r);
				}
			}
		}else {
			return "No existen recibos por facturar.";
	    }*/

		String domesticCurrency = AppConstants.DEFAULT_CURRENCY;
		List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
		if(comUDCList != null && !comUDCList.isEmpty()) {
			for(UDC company : comUDCList) {
				if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
					domesticCurrency = company.getStrValue2().trim();
					break;
				}
			}
		}
		
		List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
		if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
			for(UDC supplier : supDomUDCList) {
				if(supplier.getStrValue1().trim().equals(addressBook) && !"".equals(supplier.getStrValue2().trim())) {
					domesticCurrency = supplier.getStrValue2().trim();
					break;
				}
			}
		}
		
		if(allRules) {
			List<Receipt> recUuidList = purchaseOrderService.getReceiptsByUUID(inv.getUuid());
			if(recUuidList != null) {
				if(recUuidList.size()>0)
					return "La factura que intenta ingresar ya se encuentra cargada previamente.";
			}
			
			List<UserDocument> docslist = documentsDao.searchCriteriaByUuidOnly(inv.getUuid());
			if(docslist != null && !docslist.isEmpty()) {
				return "La factura que intenta ingresar ya se encuentra cargada previamente.";
			}
			
			if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {				
				if(exchangeRate == 0) {
					return "La moneda de la factura es " + invCurrency + " sin embargo, no existe definido un tipo de cambio.";
				}
			}
			
			String oCurr = "";
			if("PME".equals(po.getCurrecyCode())) {
				oCurr = "MXN";
			}else {
				oCurr = po.getCurrecyCode();
			}
			if(!invCurrency.equals(oCurr)) {
				return "La moneda de la factura es " + invCurrency + " sin embargo, el cÃ³digo de moneda de la orden de compra es " + oCurr;
			}
			
			if(rfcEmisor != null) {
				if(!"".equals(rfcEmisor)) {
					if(!s.getRfc().equals(rfcEmisor)) {
						return "La factura ingresada no pertenece al RFC del emisor del proveedor registrado como " + s.getRfc();
					}
				}
			}
			
			//ValidaciÃ³n de Factura Repetida
			FiscalDocuments fd = fiscalDocumentService.getFiscalDocumentsByUuid(inv.getUuid());
			if(fd != null) {
				return "La factura que intenta ingresar ya fue cargada previamente.";
			}
			
			//ValidaciÃ³n de Tipo de Cambio
			if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {				
				if(exchangeRate == 0) {
					return "La moneda de la factura es " + invCurrency + " sin embargo, no estÃ¡ definido un tipo de cambio.";
				}
			}
						
			//ValidaciÃ³n de AÃ±o Actual
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, currentYear);
			cal.set(Calendar.DAY_OF_YEAR, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date startYear = cal.getTime();
			
			if(currentYearRule) {
				try {
					if(invDate.compareTo(startYear) < 0) {
						return "La fecha de emisiÃ³n de la factura no puede ser anterior al primero de Enero del aÃ±o en curso";
					}
				}catch(Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
					return "Error al obtener la fecha de timbrado de la factura";
				}	
			}
			
			//ValidaciÃ³n de Mes
			UDC udcCurrentMonth = udcService.searchBySystemAndKey("VALIDARMESCURSO", "FACTURA");
			if (udcCurrentMonth != null && "TRUE".equals(udcCurrentMonth.getStrValue1())) {

				// Fecha Actual (Calendar)
				Calendar calCurrentDate = Calendar.getInstance();
				calCurrentDate.setTime(new Date());

				// Fecha Factura (Calendar)
				Calendar calInvoiceDate = Calendar.getInstance();
				calInvoiceDate.setTime(invDate);

				boolean isSameMonth = (calCurrentDate.get(Calendar.YEAR) == calInvoiceDate.get(Calendar.YEAR))
						&& (calCurrentDate.get(Calendar.MONTH) == calInvoiceDate.get(Calendar.MONTH));

				if (!isSameMonth) {
					return "La factura no fue generada durante el mes en curso.";
				}
			}
			
			if(s != null) {
				NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(s.getRfc(), 0, 0);
			    if (ncs != null && (
			      ncs.getRefDate1().contains("Definitivo") || 
			      ncs.getRefDate1().contains("Presunto") || 
			      ncs.getRefDate1().contains("Desvirtuado") || 
			      ncs.getRefDate2().contains("Definitivo") || 
			      ncs.getRefDate2().contains("Presunto") || 
			      ncs.getRefDate2().contains("Desvirtuado") || 
			      ncs.getStatus().contains("Definitivo") || 
			      ncs.getStatus().contains("Presunto") || 
			      ncs.getStatus().contains("Desvirtuado"))) {
			    	
			    	String altEmail = "";
						List<UDC> udcList =  udcService.searchBySystem("TAXALTEMAIL");
						if(udcList != null) {
							altEmail = udcList.get(0).getStrValue1();
						    EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
							emailAsyncSup.setProperties(
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE,
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER + " Número: " + s.getAddresNumber() + "<br /> Nombre: " + s.getRazonSocial() + "<br />",
									altEmail);
							emailAsyncSup.setMailSender(mailSenderObj);
							Thread emailThreadSup = new Thread(emailAsyncSup);
							emailThreadSup.start();
						}
			    	
						 EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						 emailAsyncSup.setProperties(
									AppConstants.EMAIL_INVOICE_REJECTED + " " + po.getOrderNumber() ,
									AppConstants.EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER_NOTIF + inv.getUuid() + "<br /> <br />" + AppConstants.ETHIC_CONTENT,
									s.getEmailSupplier());
							emailAsyncSup.setMailSender(mailSenderObj);
							Thread emailThreadSup = new Thread(emailAsyncSup);
							emailThreadSup.start();
						
						return "Los registros indican que cuenta con problemas fiscales y no se podrán cargar facturas en este momento.";
	
			    } 
			}else {
				return "El proveedor no existe en el catálogo de la aplicación";
			}

			//ValidaciÃ³n de CompaÃ±Ã­a
			boolean companyRfcIsValid = false;
			List<UDC> companyRfc = udcDao.searchBySystem("COMPANYRFC");
			if(companyRfc != null) {
				for(UDC udcrfc : companyRfc) {
					String cRfc = udcrfc.getStrValue1();
					String cRfcCompany = udcrfc.getUdcKey();
					if(cRfc.equals(inv.getRfcReceptor())) {
						if(cRfcCompany.equals(po.getCompanyKey())) {
							companyRfcIsValid = true;
							break;
						}
					}
				}
			}

			if(!companyRfcIsValid) {
				return "La factura no pertenece al receptor asociado a la orden de compra";
			}
			
			String buyerEmail = po.getEmail();
			
			boolean paymentComplException = false;
			List<UDC> pcExceptionList =  udcService.searchBySystem("NOPAYMENTCOMPL");
			if(pcExceptionList != null) {
				for(UDC udc : pcExceptionList) {
					if(addressBook.equals(udc.getStrValue1())){
						paymentComplException = true;
						break;
					}
				}
			}
			
			if(!paymentComplException && AppConstants.LOCAL_COUNTRY_CODE.equals(s.getCountry()) && AppConstants.REF_METODO_PAGO.equals(inv.getMetodoPago())) {
				String pendingList = purchaseOrderService.getPendingReceiptsComplPago(s.getAddresNumber());
				if(!"".equals(pendingList)){
					return "El sistema detectó que tiene las siguientes facturas (uuid) COMPLEMENTOS DE PAGO pendientes de carga: <br /> " + pendingList;
				}
			}
	
			cal = Calendar.getInstance();
			invDate = null;
			Date orderDate = null;
			try {
				fechaFactura = inv.getFechaTimbrado();
				fechaFactura = fechaFactura.replace("T", " ");
				sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
				invDate = sdf.parse(fechaFactura);
				orderDate = po.getDateRequested();
			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}
	
			int pueOffSetDays = 0;
			UDC pueOffSet = udcService.searchBySystemAndKey(AppConstants.FACTURA_PUE, AppConstants.OFFSET_DAYS);
			if(pueOffSet != null) {
				if(pueOffSet.getStrValue1() != null) {
					pueOffSetDays = Integer.valueOf(pueOffSet.getStrValue1());
				}
			}
			
			String cfdiReceptor = inv.getReceptor().getUsoCFDI();
			String rfcReceptor = inv.getRfcReceptor();
			
			/*
			if(!AppConstants.REF_METODO_PAGO.equals(inv.getMetodoPago())){
				return "El mÃƒÂ©todo de pago permitido es " + AppConstants.REF_METODO_PAGO + " y su CFDI contiene el valor " + inv.getMetodoPago() + ". Favor de emitir nuevamente el CFDI con el mÃƒÂ©todo de pago antes mencionado.";			
			}*/
			
			/*
			if(!AppConstants.REF_FORMA_PAGO.equals(inv.getFormaPago())){
				return  "La forma de pago permitida es " + AppConstants.REF_FORMA_PAGO + " y su CFDI contiene el valor " + inv.getFormaPago() + ". Favor de emitir nuevamente el CFDI con la forma de pago antes mencionada";		
			}*/
			
			/*
			if (AppConstants.REF_METODO_PAGO_PUE.equals(inv.getMetodoPago())) {
				List<UDC> udcListPay =  udcService.searchBySystem("PAYPUEEXEP");
				boolean metdPayPueExcep = false;
				if(udcListPay!=null) {
					for(UDC udc : udcListPay) {
						if(udc.getStrValue1().equals(rfcEmisor)){
							metdPayPueExcep = true;
						}
					}
				}
				
				if(metdPayPueExcep) {
					return "El Proveedor "+rfcEmisor+" no tiene permitido el metodo de pago PUE.";
				}
			}*/

			/*if(!AppConstants.USO_CFDI.equals(cfdiReceptor)){
				List<UDC> udcList =  udcService.searchBySystem("CFDIEXC");
				boolean usoCfdiExcept = false;
				if(udcList != null) {
					for(UDC udc : udcList) {
						if(udc.getStrValue1().equals(rfcEmisor)){
							usoCfdiExcept = true;
						}
					}
				}
				
				if(usoCfdiExcept) {
					return "El uso CFDI " + cfdiReceptor + " no es permitido para su razÃ³n social";
				}				
			}*/
			
			//ValidaciÃ³n de USO CFDI
			boolean usoCFDIAllowed = false;
			List<UDC> udcList =  udcService.searchBySystem("USOCFDI");
			if(udcList != null) {
				for(UDC udc : udcList) {
					if(udc.getStrValue1().equals(cfdiReceptor)){
						usoCFDIAllowed = true;
						break;
					}
				}
			}
			
			if(!usoCFDIAllowed) {
				return "El uso CFDI " + cfdiReceptor + " no está permitido para su razón social.";
			}
			
			if(rfcEmisor != null) {
				if(!"".equals(rfcEmisor)) {
					if(!s.getRfc().equals(rfcEmisor)) {
						return "La factura ingresada no pertenece al RFC del emisor del proveedor registrado como " + s.getRfc();
					}
				}
			}
			
			boolean receptorValido = false;
			List<UDC> receptores = udcService.searchBySystem("RECEPTOR");
			if(receptores != null) {
				for(UDC udc : receptores) {
					if(udc.getStrValue1().equals(inv.getRfcReceptor().trim())) {
						receptorValido = true;
						break;
					}
				}
			}
			if(!receptorValido) {
				return "El RFC receptor " + inv.getRfcReceptor() + " no es permitido para carga de facturas.";
			}

			// ValidaciÃ³n de los importes del recibo:			
			if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
				Users u = usersService.searchCriteriaUserName(po.getAddressNumber());
				
				UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
				UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");				
				UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");				
				UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
				UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
				
				double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
				double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;
				
				double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
				double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
				double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
				
				double orderAmount = 0;
				double foreignOrderAmount = 0;
				double retainedAmount = 0;
				double retainedForeignAmount = 0;
				double currentRetainedAmount = 0;
				double invoiceAmount = 0;
				
				boolean isRetainedValidationOn = true;
				List<UDC> taxCodeExcList =  udcService.searchBySystem("NOCHECKRET");
				List<String> taxCodeList = new ArrayList<String>();
				if(taxCodeExcList != null && !taxCodeExcList.isEmpty()) {
					for(UDC taxCode : taxCodeExcList) {
						taxCodeList.add(taxCode.getStrValue1());
					}
				}
				
				//JAVILA
			/*	String receiptTaxCode = "";
				for(Receipt r :requestedReceiptList) {
					if(r.getTaxCode() != null && !"".equals(r.getTaxCode().trim())) {
						receiptTaxCode = r.getTaxCode().trim();
						if(taxCodeList.contains(r.getTaxCode().trim().toUpperCase())) {
							isRetainedValidationOn = false;
							break;	
						}
					}
				}*/

				//JSAAVEDRA: Valida los impuestos contenidos en el CFDI
				// VALIDACION DE IMPUESTOS DESACTIVADO DACG
				/*if(isTaxValidationOn) {
					String tasaOCuota = "";
					List<UDC> udcTax = udcDao.searchBySystem("F43121TXA1");
					List<String> validTaxCodeList = new ArrayList<String>();
					
					if(udcTax != null) {
						for(UDC ut :udcTax) {
							validTaxCodeList.add(ut.getUdcKey().trim());
						}
					}

					if(!"".equals(receiptTaxCode)) {
						if(validTaxCodeList.contains(receiptTaxCode)) {					
							List<String> cfdiTransTaxList = this.getTranslatedTaxList(inv);
							List<String> cfdiRetTaxList = this.getRetainedTaxList(inv);
							List<String> udcTransTaxList = new ArrayList<String>();
							List<String> udcRetTaxList = new ArrayList<String>();
							String udcValueT = "";
							String udcValueR = "";
							String tasasRequeridas = "";

							//TRASLADADOS
							//Obtiene lista de impuestos Trasladados requeridos con base en la UDC
							for(UDC ut :udcTax) {
								if(ut.getUdcKey().equals(receiptTaxCode.trim()) && ut.getStrValue2() != null && !"".equals(ut.getStrValue2().trim()) && !"NA".equals(ut.getStrValue2().trim())) {
									udcValueT = ut.getStrValue2();
									udcTransTaxList = this.stringWithPipesToList(udcValueT);
									
									if(udcTransTaxList.size() == cfdiTransTaxList.size())
									break;									
								}
							}
							
							//Valida que el CFDI cuente con los impuestos Trasladado requeridos
							if(!udcTransTaxList.isEmpty()) {
								for(String transTaxValue : udcTransTaxList) {
									//Verifica si el CFDI cuenta con el impuesto Trasladado
									if(!cfdiTransTaxList.contains(transTaxValue)) {
										tasasRequeridas = udcValueT.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI no cumple con los impuestos trasladados requeridos, favor de verificarlo con el comprador.<br />CÃ³digo de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos trasladados que se requiren: " + tasasRequeridas + ".";
									}
								}					
							}
							
							//Valida que el CFDI no tenga impuestos Trasladados adicionales a los impuestos requeridos
							if(!cfdiTransTaxList.isEmpty()) {					
								for(String transTaxValue : cfdiTransTaxList) {
									if(!udcTransTaxList.contains(transTaxValue)) {
										tasasRequeridas = udcValueT.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI contiene impuestos trasladados que no corresponden con el recibo, favor de verificarlo con el comprador.<br />CÃ³digo de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos trasladados que se requiren: " + tasasRequeridas + ". Tasa no vÃ¡lida: " + transTaxValue + ".";
									}
								}
							}
							
							//RETENIDOS
							//Obtiene lista de impuestos Retenidos requeridos con base en la UDC
							for(UDC ut :udcTax) {
								if(ut.getUdcKey().equals(receiptTaxCode.trim()) && ut.getStrValue1() != null && !"".equals(ut.getStrValue1().trim()) && !"NA".equals(ut.getStrValue1().trim())) {
									udcValueR = ut.getStrValue1().trim();
									udcRetTaxList = this.stringWithPipesToList(udcValueR);
									break;
								}
							}
							
							//Valida que el CFDI cuente con los impuestos Retenidos requeridos
							if(!udcRetTaxList.isEmpty()) {
								for(String retTaxValue : udcRetTaxList) {
									//Verifica si el CFDI cuenta con el impuesto Retenido
									if(!cfdiRetTaxList.contains(retTaxValue)) {
										tasasRequeridas = udcValueR.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI no cumple con los impuestos retenidos requeridos, favor de verificarlo con el comprador.<br />CÃ³digo de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos retenidos que se requiren: " + tasasRequeridas + ".";
									}
								}
							}
							
							//Valida que el CFDI no tenga impuestos Retenidos adicionales a los impuestos requeridos
							if(!cfdiRetTaxList.isEmpty()) {					
								for(String retTaxValue : cfdiRetTaxList) {
									if(!udcRetTaxList.contains(retTaxValue)) {
										tasasRequeridas = udcValueR.replace("|",", ").trim();
										if("".equals(tasasRequeridas)) {
											tasasRequeridas = "N/A";
										}
										return "El CFDI contiene impuestos retenidos que no corresponden con el recibo, favor de verificarlo con el comprador.<br />CÃ³digo de impuestos del recibo: " + receiptTaxCode + ". Tasas de impuestos retenidos que se requiren: " + tasasRequeridas + ". Tasa no vÃ¡lida: " + retTaxValue + ".";
									}
								}
							}
							
						} else {
							return "El recibo no cuenta con un cÃ³digo de impuestos vÃ¡lido, favor de verificarlo con el comprador.<br />CÃ³digo de impuestos del recibo: " + receiptTaxCode;
						}
					} else {
						return "El recibo no cuenta con un cÃ³digo de impuestos vÃ¡lido, favor de verificarlo con el comprador.";
					}
				}*/
				//JSAAVEDRA: Fin de validaciÃ³n de impuestos
				
				//Calcula montos de Recibos y de Retenciones
				/*for(Receipt r :requestedReceiptList) {
					if(AppConstants.RECEIPT_CODE_RETENTION.equals(r.getReceiptType())) {
						retainedAmount = retainedAmount + r.getAmountReceived();
						retainedForeignAmount = retainedForeignAmount + r.getForeignAmountReceived();
					} else {
						orderAmount = orderAmount + r.getAmountReceived();
						foreignOrderAmount = foreignOrderAmount + r.getForeignAmountReceived();
					}
				}*/
				
				orderAmount = po.getOrderAmount();
				foreignOrderAmount = po.getForeignAmount();
				
				//ValidaciÃ³n de Retenciones
				String tipoValidacion ="";
				//DACG EVITAR VALIDACION
				isRetainedValidationOn = false;
				if(isRetainedValidationOn) {
					double totalImporteMayorRetenido = 0;
					double totalImporteMenorRetenido = 0;
					double retainedTotalAmount = 0;
					
					if(domesticCurrency.equals(invCurrency)) {
						currentRetainedAmount = retainedAmount;
						totalImporteMayorRetenido = retainedAmount;
						totalImporteMenorRetenido = retainedAmount;
					} else {
						currentRetainedAmount = retainedForeignAmount;
						totalImporteMayorRetenido = retainedForeignAmount;
						totalImporteMenorRetenido = retainedForeignAmount;
					}
					
					retainedTotalAmount = inv.getTotalRetenidos();
					if(montoLimite != 0) {
						if(retainedTotalAmount >= montoLimite) {
							totalImporteMayorRetenido = totalImporteMayorRetenido + montoLimiteMax;
							totalImporteMenorRetenido = totalImporteMenorRetenido - montoLimiteMin;
							tipoValidacion = "Por Monto";
						}else {
							totalImporteMayorRetenido = totalImporteMayorRetenido + (totalImporteMayorRetenido * porcentajeMax);
							totalImporteMenorRetenido = totalImporteMenorRetenido - (totalImporteMenorRetenido * porcentajeMin);
							tipoValidacion = "Por porcentaje";
						}
					}
					
					totalImporteMayorRetenido = totalImporteMayorRetenido * 100;
					totalImporteMayorRetenido = Math.round(totalImporteMayorRetenido);
					totalImporteMayorRetenido = totalImporteMayorRetenido /100;	
					
					totalImporteMenorRetenido = totalImporteMenorRetenido * 100;
					totalImporteMenorRetenido = Math.round(totalImporteMenorRetenido);
					totalImporteMenorRetenido = totalImporteMenorRetenido /100;
					
					if(totalImporteMayorRetenido < retainedTotalAmount || totalImporteMenorRetenido > retainedTotalAmount) {					
						return "El total de los impuestos retenidos de su CFDI es " + currencyFormat.format(retainedTotalAmount) + " no coincide con el total de las retenciones de la orden de compra seleccionada. Favor de verificarlo con el comprador.";
					}
				}

				//ValidaciÃ³n de Montos
				double totalImporteMayor = 0;
				double totalImporteMenor = 0;
				double invoiceTotalAmount = 0;
				double currentInvoiceAmount = 0;
				
				if(domesticCurrency.equals(invCurrency)) {
					currentInvoiceAmount = orderAmount;
					totalImporteMayor = orderAmount;
					totalImporteMenor = orderAmount;
				} else {
					currentInvoiceAmount = foreignOrderAmount;
					totalImporteMayor = foreignOrderAmount;
					totalImporteMenor = foreignOrderAmount;
				}
								
				invoiceTotalAmount = inv.getSubTotal();
				
				if(montoLimite != 0) {
					if(invoiceTotalAmount >= montoLimite) {
						totalImporteMayor = totalImporteMayor + montoLimiteMax;
						totalImporteMenor = totalImporteMenor - montoLimiteMin;
						tipoValidacion = "Por Monto";
					}else {
						totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
						totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
						tipoValidacion = "Por porcentaje";
					}
				}
				
				totalImporteMayor = totalImporteMayor * 100;
				totalImporteMayor = Math.round(totalImporteMayor);
				totalImporteMayor = totalImporteMayor /100;	
				
				totalImporteMenor = totalImporteMenor * 100;
				totalImporteMenor = Math.round(totalImporteMenor);
				totalImporteMenor = totalImporteMenor /100;
				
				double discount = 0;
				
				if(inv.getDescuento() != 0) {
					discount = inv.getDescuento();
					discount = discount * 100;
					discount = Math.round(discount);
					discount = discount /100;	
				}
				
				invoiceAmount = inv.getSubTotal() - discount;
				
				//JSAAVEDRA: Montos extra para Proveedores Hoteleros
				/*if(isHotelSupplier) {
					//Se suma monto miscelÃ¡neo
					invoiceAmount += miscellaneousAmount;
					//Se suma impuesto ISH
					if(inv.getTotalImpLocTraslados() > 0) {
						invoiceAmount += getISHAmount(inv);
					}
				}*/
				
				invoiceAmount = invoiceAmount * 100;
				invoiceAmount = Math.round(invoiceAmount);
				invoiceAmount = invoiceAmount /100;
				
				if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
					return "El total de la factura " + currencyFormat.format(invoiceAmount) + " no coincide con el total de la orden de compra seleccionada " + currencyFormat.format(currentInvoiceAmount) + ". Tipo de validación:" + tipoValidacion;
				}else {
					
					List<UDC> pmtTermsUdc = udcService.searchBySystem("PMTTERMS");
					String pmtTermsCode = "";
					/*for(Receipt r :requestedReceiptList) {
						if(r.getPaymentTerms() != null && !"".equals(r.getPaymentTerms())) {
							for(UDC udcpmt : pmtTermsUdc) {
								if(udcpmt.getStrValue1().equals(r.getPaymentTerms().trim())) {
									diasCred = Integer.parseInt(udcpmt.getStrValue2());
									break;
								}
							}
						}
						if(r.getPaymentTerms() == null || "".equals(r.getPaymentTerms())) {
							diasCred = 30;
							break;
						}
					}*/
					
					Date estimatedPaymentDate = null;
					Date currentDate = new Date();
					if(currentDate != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(currentDate);
						c.add(Calendar.DATE, diasCred);
						List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, po.getAddressNumber());
						if(pc != null) {
							if(pc.size() > 0) {
								estimatedPaymentDate = pc.get(0).getPaymentDate();
							}else {
								estimatedPaymentDate = c.getTime();
							}
						}else {
							estimatedPaymentDate = c.getTime();
						}
					}

					/*po.setInvoiceAmount(po.getInvoiceAmount() + inv.getTotal());
			        po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
			        po.setInvoiceUploadDate(invDate);
			        po.setSentToWns(null);
			        po.setEstimatedPaymentDate(estimatedPaymentDate);*/
			      //  purchaseOrderService.updateOrders(po);
			        
			        /*for(Receipt r :requestedReceiptList) {
			        	r.setInvDate(invDate);
			        	r.setFolio(inv.getFolio());
			        	r.setSerie(inv.getSerie());
			        	r.setUuid(inv.getUuid());
			        	r.setEstPmtDate(estimatedPaymentDate);
						r.setStatus(AppConstants.STATUS_OC_INVOICED);
					}*/
				//	purchaseOrderService.updateReceipts(requestedReceiptList);
				}
	
			}
		
		
		}else {
			
				List<UDC> pmtTermsUdc = udcService.searchBySystem("PMTTERMS");
				String pmtTermsCode = "";
				/*for(Receipt r :requestedReceiptList) {
					if(r.getPaymentTerms() != null && !"".equals(r.getPaymentTerms())) {
						for(UDC udcpmt : pmtTermsUdc) {
							if(udcpmt.getStrValue1().equals(r.getPaymentTerms().trim())) {
								diasCred = Integer.parseInt(udcpmt.getStrValue2());
								break;
							}
						}
					}
					if(r.getPaymentTerms() == null || "".equals(r.getPaymentTerms())) {
						diasCred = 30;
						break;
					}
				}*/
				Date estimatedPaymentDate = null;
				if(invDate != null) {
					Calendar c = Calendar.getInstance();
					c.setTime(invDate);
					c.add(Calendar.DATE, diasCred);
					List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, po.getAddressNumber());
					if(pc != null) {
						if(pc.size() > 0) {
							estimatedPaymentDate = pc.get(0).getPaymentDate();
						}else {
							estimatedPaymentDate = c.getTime();
						}
					}else {
						estimatedPaymentDate = c.getTime();
					}
				}
								
				/*po.setInvoiceAmount(po.getInvoiceAmount() + inv.getTotal());
		        po.setInvoiceUploadDate(invDate);
		        po.setSentToWns(null);
		        po.setEstimatedPaymentDate(estimatedPaymentDate);*/
		        //purchaseOrderService.updateOrders(po);
		        
		      /*  for(Receipt r :requestedReceiptList) {
		        	r.setInvDate(invDate);
		        	r.setFolio(inv.getFolio());
		        	r.setSerie(inv.getSerie());
		        	r.setUuid(inv.getUuid());
		        	r.setEstPmtDate(estimatedPaymentDate);
					r.setStatus(AppConstants.STATUS_OC_INVOICED);
				}*/
				//purchaseOrderService.updateReceipts(requestedReceiptList);
				sendVoucher = true;
			
		}
	
		
		/*if(sendVoucher) {
			try {				
				List<Receipt> receipts = new ArrayList<>();
				if(domesticCurrency.equals(invCurrency)) {
					resp = "DOC:" + eDIService.createNewVoucherWithoutReceipt(po, inv, 0, s, receipts, AppConstants.NN_MODULE_VOUCHER);
				} else {
					ForeingInvoice fi = new ForeingInvoice();
					fi.setSerie(inv.getSerie());
					fi.setFolio(inv.getFolio());
					fi.setUuid(inv.getUuid());
					fi.setExpeditionDate(inv.getFechaTimbrado());
					resp = "DOC:" + eDIService.createNewForeignVoucherWithoutReceipt(po, fi, 0, s, receipts, AppConstants.NN_MODULE_VOUCHER);
				}
								
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(AppConstants.EMAIL_INV_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(AppConstants.ETHIC_CONTENT_INVOICE), emailRecipient);
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();				
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return e.toString();
			}
		}*/
		return resp;
	}

}
