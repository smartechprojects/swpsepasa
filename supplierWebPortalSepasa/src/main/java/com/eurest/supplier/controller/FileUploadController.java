package com.eurest.supplier.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.eurest.supplier.dao.CodigosSatDao;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.invoiceXml.DoctoRelacionado;
import com.eurest.supplier.invoiceXml.Pago;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.service.BatchProcessService;
import com.eurest.supplier.service.CodigosSATService;
import com.eurest.supplier.service.DataAuditService;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.EmailService;
import com.eurest.supplier.service.FTPService;
import com.eurest.supplier.service.FiscalDocumentService;
import com.eurest.supplier.service.OutSourcingService;
import com.eurest.supplier.service.PurchaseOrderService;
import com.eurest.supplier.service.SupplierService;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.FileUploadBean;
import com.eurest.supplier.util.Logger;

import net.sf.json.JSONObject;
 
@Controller
public class FileUploadController {
 
   	@Autowired
	DocumentsService documentsService;
   	
   	@Autowired
	PurchaseOrderService purchaseOrderService;
   	
   	@Autowired
   	FiscalDocumentService fiscalDocumentService;
   	
	@Autowired
	UdcService udcService;
	
	@Autowired
	SupplierService supplierService;
	
	@Autowired
	CodigosSATService codigosSATService;
	
	@Autowired
	private CodigosSatDao codigosSatDao;
	
	@Autowired
	Logger logger;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	FTPService ftpService;
	
	@Autowired
	OutSourcingService outSourcingService;
	
	@Autowired
	DataAuditService dataAuditService;

	private static final long MAX_SIZE = 26214400; //MAX SIZE 25MB
	private static final List<String> ALLOWED_FORMAT = Arrays.asList(new String[]{"doc","docx","xls","xlsx","pdf","jpg","jpeg","png","txt","gif"});
	
	private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(FileUploadController.class);
	
	 @RequestMapping(value = "/uploadInvoiceFromReceipt.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	    public @ResponseBody String uploadInvoiceFromReceipt(FileUploadBean uploadItem, 
	    										  BindingResult result, 
	    										  String orderCompany, 
	    										  String addressBook, 
												  int documentNumber, 
												  String documentType,
												  String tipoComprobante,
												  String receiptIdList,
									    		  HttpServletResponse response){
	 
		 
		 
		 
	    	response.setContentType("text/html");
	        response.setCharacterEncoding("UTF-8");
	    	JSONObject json = new JSONObject();
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 			
			String usr = auth.getName();
	        try{
	        
	        if (result.hasErrors()){
	            for(ObjectError error : result.getAllErrors()){
	                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
	            }
	 
	            json.put("success", false);
	            json.put("message", "Error_1");
	        }
	        
	        InvoiceDTO inv = null;
	        String ct = uploadItem.getFile().getContentType();
	        
	        if(uploadItem.getFileTwo() != null) {
	            String ctPdf = uploadItem.getFileTwo().getContentType();
	            if(!"application/pdf".equals(ctPdf)) {
	            	json.put("success", false);
	            	json.put("message", "Error_2");
	            	return json.toString();
	            }
	        }
	        

	        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
	            if("text/xml".equals(ct.trim())){
	                inv = documentsService.getInvoiceXml(uploadItem);
	                if(inv != null){
	                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
	                		json.put("success", false);
	                    	json.put("message", "Error_3");
	                    	return json.toString();
	                	}
	                	                	
	                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
	                    String xmlContent = IOUtils.toString(stream, "UTF-8");
	                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
	        			String xmlString = source.replace("?<?xml", "<?xml");
	                    DataAudit dataAudit = new DataAudit();
	                    Date currentDate = new Date();
	                    
	        			//JSAAVEDRA: Validación Códigos SAT
	        			Supplier s = supplierService.searchByAddressNumber(addressBook);
	        			if(s != null && s.isOutSourcing()) {
	        				String rOs = outSourcingService.validateInvoiceCodes(inv.getConcepto());
	        				if(!"".equals(rOs)) {
		                		json.put("success", false);
		                    	json.put("message", rOs);
		                    	return json.toString();
	        				}
	        			}
	        			
	        			PurchaseOrder po = purchaseOrderService.searchbyOrderAndAddressBookAndCompany(documentNumber, addressBook, documentType, orderCompany);
	                	String res = documentsService.validateInvoiceFromOrder(inv,
	                														   addressBook, 
																			   documentNumber, 
																			   documentType,
																			   tipoComprobante,
																			   po,
																			   true,
																			   xmlString,
																			   receiptIdList,
																			   false,
																			   0);
	                	
	                	
	                	if("".equals(res) || res.contains("DOC:")){
	                		
	                		res = documentsService.saveInvoiceFromOrder(uploadItem, po, receiptIdList, inv, tipoComprobante);                    		
	                		if("".equals(res)){
		                    	dataAudit.setAction("UploadInvoice");
		                    	dataAudit.setAddressNumber(po.getAddressNumber());
		                    	dataAudit.setCreationDate(currentDate);
		                    	dataAudit.setDocumentNumber(documentNumber+"");
		                    	dataAudit.setIp(request.getRemoteAddr());
		                    	dataAudit.setMessage("Uploaded Invoice Successful");
		                    	dataAudit.setMethod("uploadInvoiceFromReceipt");
		                    	dataAudit.setModule(AppConstants.SALESORDER_MODULE);
		                    	dataAudit.setNotes(null);
		                    	dataAudit.setOrderNumber(po.getOrderNumber()+"");
		                    	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
		                    	dataAudit.setStep(null);
		                    	dataAudit.setUser(usr);
		                    	dataAudit.setUuid(inv.getUuid());	                    	
		                    	dataAuditService.save(dataAudit);
		                    	
		        	            json.put("success", true);
		        	            json.put("message", inv.getMessage());
		        	            json.put("orderNumber", documentNumber);
		        	            json.put("orderType", documentType);
		        	            json.put("addressNumber", addressBook);
		        	            json.put("docNbr", res);
		        	            json.put("uuid", inv.getUuid());
	                		} else {
		                    	json.put("success", false);
		                    	json.put("message", res);
	                		}

	                	}else{
	                    	json.put("success", false);
	                    	json.put("message", res);
	                	}

	                }else{
	                	json.put("success", false);
	                	json.put("message", "Error_4");
	                }
	                
	            }else{
	            	json.put("success", false);
	            	json.put("message", "Error_5");
	            }
	        }else{
	        	
	            if(uploadItem.getFile() != null) {
	                String ctPdf = uploadItem.getFile().getContentType();
	                if(!"application/pdf".equals(ctPdf)) {
	                	json.put("success", false);
	                	json.put("message", "Error_6");
	                	return json.toString();
	                }
	            }
	        	
	        	PurchaseOrder po = purchaseOrderService.searchbyOrder(documentNumber, documentType);
	        	UserDocument doc = new UserDocument(); 
	        	
	        	if(po != null) {
	        		
	        		String fileName = uploadItem.getFile().getOriginalFilename();
	        		fileName = fileName.replaceAll(" ", "_");
	        		
	            	doc.setAddressBook(po.getAddressNumber());
	            	doc.setDocumentNumber(documentNumber);
	            	doc.setDocumentType(documentType);
	            	doc.setContent(uploadItem.getFile().getBytes());
	            	doc.setType(ct.trim());
	            	doc.setName(fileName);
	            	doc.setSize(uploadItem.getFile().getSize());
	            	doc.setStatus(true);
	            	doc.setAccept(true);
	            	doc.setFiscalType(tipoComprobante);
	            	doc.setUuid("");
	            	doc.setUploadDate(new Date());
	            	doc.setFiscalRef(0);
	            	documentsService.save(doc, new Date(), "");
	            	
	                json.put("success", true);
	                json.put("message", "El archivo ha sido cargado de forma exitosa");
	                json.put("orderNumber", documentNumber);
	                json.put("orderType", documentType);
	                json.put("addressNumber", addressBook);
	        	}else {
	                json.put("success", false);
	                json.put("message", "Error_7");
	        	}

	            
	        }
	        return json.toString();
	        
	        }catch(Exception e){
	        	log4j.error("Exception" , e);
	        	e.printStackTrace();
	        	json.put("success", false);
	            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
	        }
	        return json.toString();

	    }
	
	 
	 
	 @RequestMapping(value = "/uploadCreditNoteFromReceipt.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	    public @ResponseBody String uploadCreditNoteFromReceipt(FileUploadBean uploadItem, 
	    										  BindingResult result, 
	    										  String addressBook, 
												  int documentNumber, 
												  String documentType,
												  String tipoComprobante,
												  String receiptIdList,
									    		  HttpServletResponse response){
	 
	    	response.setContentType("text/html");
	        response.setCharacterEncoding("UTF-8");
	    	JSONObject json = new JSONObject();

	        try{

	        if (result.hasErrors()){
	            for(ObjectError error : result.getAllErrors()){
	                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
	            }
	 
	            json.put("success", false);
	            json.put("message", "Error_1");
	        }
	        
	        InvoiceDTO inv = null;
	        String ct = uploadItem.getFile().getContentType();
	        
	        if(uploadItem.getFileTwo() != null) {
	            String ctPdf = uploadItem.getFileTwo().getContentType();
	            if(!"application/pdf".equals(ctPdf)) {
	            	json.put("success", false);
	            	json.put("message", "Error_2");
	            	return json.toString();
	            }
	        }
	        
	        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
	            if("text/xml".equals(ct.trim())){
	                inv = documentsService.getInvoiceXml(uploadItem);
	                if(inv != null){
	                	
	                	if(AppConstants.NC_FIELD.equals(tipoComprobante) && !"E".equals(inv.getTipoComprobante())){
	                		json.put("success", false);
	                    	json.put("message", "Error_8");
	                    	return json.toString();
	                	}
	                	
	                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
	                    String xmlContent = IOUtils.toString(stream, "UTF-8");
	                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
	        			String xmlString = source.replace("?<?xml", "<?xml");
	                    
	        			PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
	                	String res = documentsService.validateCreditNoteFromOrder(inv,
	                														   addressBook, 
																			   documentNumber, 
																			   documentType,
																			   tipoComprobante,
																			   po,
																			   true,
																			   xmlString,
																			   receiptIdList);
	                	
	                	
	                	if("".equals(res) || res.contains("DOC:")){
	                    	UserDocument doc = new UserDocument(); 
	                    	doc.setAddressBook(po.getAddressNumber());
	                    	doc.setDocumentNumber(documentNumber);
	                    	doc.setDocumentType(documentType);
	                    	doc.setContent(uploadItem.getFile().getBytes());
	                    	doc.setType(ct.trim());
                        	String fileName = uploadItem.getFile().getOriginalFilename();
	                    	fileName = fileName.replace(" ", "_");
                        	doc.setName(fileName);
	                    	//doc.setName(uploadItem.getFile().getOriginalFilename());
	                    	doc.setName("NC_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + "_" + getTimeSuffix() + ".xml");
	                    	doc.setSize(uploadItem.getFile().getSize());
	                    	doc.setStatus(true);
	                    	doc.setAccept(true);
	                    	doc.setFiscalType(tipoComprobante);
	                    	doc.setType("text/xml");
	                    	doc.setFolio(inv.getFolio());
	                    	doc.setSerie(inv.getSerie());
	                    	doc.setUuid(inv.getUuid());
	                    	doc.setUploadDate(new Date());
	                    	doc.setFiscalRef(0);
	                    	documentsService.save(doc, new Date(), "");
	                    	
	                    	try {
	                        	//Send pdf to remote server
	                        	//CommonsMultipartFile cFile = uploadItem.getFileTwo();
	                    		//File convFile = new File(System.getProperty("java.io.tmpdir")+"/" + inv.getUuid() + ".pdf");
	                    		//cFile.transferTo(convFile);
	                    		//documentsService.sendFileToRemote(convFile, inv.getUuid() + ".pdf");
	                    		//ftpService.setServices(null, purchaseOrderService, documentsService, udcService, logger);
	    	                	//Send xml to SFTP server Nota de Crédito
	                    		//ftpService.sendToSftpServer(xmlString, uploadItem.getFile().getOriginalFilename());
	    	                	//Send PDF to SFTP server Nota de Crédito
	                    		//ftpService.sendToSftpServer(cFile.getInputStream(), uploadItem.getFile().getOriginalFilename());
	    	                	
	    	                	
							} catch (Exception e) {
								log4j.error("Exception" , e);
								e.printStackTrace();
							}
    	                	
	                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)){
	                    		po.setInvoiceUuid(inv.getUuid());
	                    		po.setInvoiceNumber(inv.getFolio() + "");
	                    		po.setPaymentType(inv.getMetodoPago());
	                    		po.setStatus(AppConstants.STATUS_OC_INVOICED);
	                    		po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
	                    	}
	                    	
	                    	purchaseOrderService.updateOrders(po);
	                    	
	        	            json.put("success", true);
	        	            json.put("message", inv.getMessage());
	        	            json.put("orderNumber", documentNumber);
	        	            json.put("orderType", documentType);
	        	            json.put("addressNumber", addressBook);
	        	            json.put("docNbr", res);
	        	            json.put("uuid", inv.getUuid());
	                	}else{
	                		json.put("success", false);
	                    	json.put("message", res);
	                	}

	                }else{
	                	json.put("success", false);
	                	json.put("message", "Error_4");
	                }
	                
	            }else{
	            	json.put("success", false);
	            	json.put("message", "Error_5");
	            }
	        }
	        return json.toString();
	        
	        }catch(Exception e){
	        	log4j.error("Exception" , e);
	        	e.printStackTrace();
	        	json.put("success", true);
	            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
	        }
	        return json.toString();

	}
	 
	 @RequestMapping(value = "/uploadReceiptInvoiceZip.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	    public @ResponseBody String uploadReceiptInvoiceZip(FileUploadBean uploadItem, 
	    										  BindingResult result, 
	    										  String addressBook, 
												  int documentNumber, 
												  String documentType,
												  String tipoComprobante,
												  String receiptIdList,
									    		  HttpServletResponse response){
	 
	    	response.setContentType("text/html");
	        response.setCharacterEncoding("UTF-8");
	    	JSONObject json = new JSONObject();

	        try{

	        if (result.hasErrors()){
	            for(ObjectError error : result.getAllErrors()){
	                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
	            }
	 
	            json.put("success", false);
	            json.put("message", "Error al cargar el archivo");
	        }
	        
	        String ct = uploadItem.getFile().getContentType();
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String usr = auth.getName();
	        
	        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
	            if("application/x-zip-compressed".equals(ct.trim()) ||
	            	"application/zip".equals(ct.trim())){
	            	String message = documentsService.validateInvZipFile(uploadItem, result, addressBook, documentNumber, documentType, tipoComprobante, receiptIdList, usr);
	            	
	            	if(StringUtils.isBlank(message)) {
	            		json.put("success", true);
	            	} else {
	            		json.put("success", false);	
	            		json.put("message", message);
	            	}
	            }else{
	            	json.put("success", false);
	            	json.put("message", "Para cargas de archivos fiscales de forma masiva, sólo se permiten archivos .zip");
	            }
	        }
	        return json.toString();
	        
	        }catch(Exception e){
	        	log4j.error("Exception" , e);
	        	e.printStackTrace();
	        	json.put("success", true);
	            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
	        }
	        return json.toString();

	 }
	
    @RequestMapping(value = "/upload.action", method = RequestMethod.POST)
    @ResponseBody public String create(FileUploadBean uploadItem, 
    								   BindingResult result, 
    								   String addressBook, 
    								   int documentNumber, 
    								   String documentType,
    		                           HttpServletResponse response,
    		                           HttpSession session){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }
        
        
       // SAAVI SECURITY - Validate file extension:
       if(isValidExtension(uploadItem.getFile().getOriginalFilename().trim())){
        //String ct = uploadItem.getFile().getContentType();
        //if("application/pdf".equals(ct.trim()) || "image/jpg".equals(ct.trim())|| "image/jpeg".equals(ct.trim())){
            
        	// SAAVI SECURITY - Validate file size:
	        if(uploadItem.getFile() != null){
	        	if(uploadItem.getFile().getSize() >  MAX_SIZE) {
	        		json.put("success", false);
	                json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " excede del limite permitido de 25MB.");
	                return json.toString();
	        	}
	        }
	        
	        if(uploadItem.getFileTwo() != null){
	        	if(uploadItem.getFileTwo().getSize() >  MAX_SIZE) {
	        		json.put("success", false);
	                json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " excede del limite permitido de 25MB.");
	                return json.toString();
	        	}
	        }
        
    		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    		String usr = auth.getName();
            documentsService.save(uploadItem, new Date(), usr, documentNumber, documentType, addressBook);

            json.put("success", true);
            json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " ha sido cargado exitosamente.");
            json.put("fileName", uploadItem.getFile().getOriginalFilename());

        }else{
        	json.put("success", false);
        	json.put("message", "Error - Se permiten solamente archivos de tipo: " + String.join(", ", ALLOWED_FORMAT));
        }
        

        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();

        }
        return json.toString();

    }
    
    @RequestMapping(value = "/uploadInvoiceFromOrder.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadInvoiceFromOrder(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  int documentNumber, 
											  String documentType,
											  String tipoComprobante,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }
        
        InvoiceDTO inv = null;
        String ct = uploadItem.getFile().getContentType();
        
        if(uploadItem.getFileTwo() != null) {
            String ctPdf = uploadItem.getFileTwo().getContentType();
            if(!"application/pdf".equals(ctPdf)) {
            	json.put("success", false);
            	json.put("message", "El documento cargado de tipo .pdf no es válido");
            	return json.toString();
            }
        }

        
        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
            if("text/xml".equals(ct.trim())){
                inv = documentsService.getInvoiceXml(uploadItem);
                if(inv != null){
                	
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no es de tipo FACTURA<br />(Tipo Comprobante = I" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.NC_FIELD.equals(tipoComprobante) && !"E".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no coresponde a una NOTA DE CREDITO<br />(Tipo Comprobante = E" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante) && !"P".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no corresponde a un COMPLEMENTO DE PAGO <br />(Tipo Comprobante = P" + ")");
                    	return json.toString();
                	}
                	
                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
                    String xmlContent = IOUtils.toString(stream, "UTF-8");
                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
        			String xmlString = source.replace("?<?xml", "<?xml");
                    
        			PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
                	String res = documentsService.validateInvoiceFromOrder(inv,
                														   addressBook, 
																		   documentNumber, 
																		   documentType,
																		   tipoComprobante,
																		   po,
																		   true,
																		   xmlString,
																		   "",
																		   false,
																		   0);
                	
                	
                	if("".equals(res) || res.contains("DOC:")){
                    	UserDocument doc = new UserDocument(); 
                    	doc.setAddressBook(po.getAddressNumber());
                    	doc.setDocumentNumber(documentNumber);
                    	doc.setDocumentType(documentType);
                    	doc.setContent(uploadItem.getFile().getBytes());
                    	doc.setType(ct.trim());
                    	//doc.setName(uploadItem.getFile().getOriginalFilename());
                    	doc.setName("FAC_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml");
                    	doc.setSize(uploadItem.getFile().getSize());
                    	doc.setStatus(true);
                    	doc.setAccept(true);
                    	doc.setFiscalType(tipoComprobante);
                    	doc.setType("text/xml");
                    	doc.setFolio(inv.getFolio());
                    	doc.setSerie(inv.getSerie());
                    	doc.setUuid(inv.getUuid());
                    	doc.setUploadDate(new Date());
                    	doc.setFiscalRef(0);
                    	documentsService.save(doc, new Date(), "");
                    	
                    	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
                    		
                    		doc = new UserDocument(); 
                        	doc.setAddressBook(po.getAddressNumber());
                        	doc.setDocumentNumber(documentNumber);
                        	doc.setDocumentType(documentType);
                        	doc.setContent(uploadItem.getFileTwo().getBytes());
                        	doc.setType(uploadItem.getFileTwo().getContentType().trim());
                        	//doc.setName(uploadItem.getFileTwo().getOriginalFilename());
                        	doc.setName("FAC_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".pdf");
                        	doc.setSize(uploadItem.getFileTwo().getSize());
                        	doc.setStatus(true);
                        	doc.setAccept(true);
                        	doc.setFiscalType(tipoComprobante);
                        	doc.setType("application/pdf");
                        	doc.setFolio(inv.getFolio());
                        	doc.setSerie(inv.getSerie());
                        	doc.setUuid(inv.getUuid());
                        	doc.setUploadDate(new Date());
                        	doc.setFiscalRef(0);
                        	documentsService.save(doc, new Date(), "");
                    		
                    		po.setInvoiceUuid(inv.getUuid());
                    		po.setInvoiceNumber(inv.getFolio() + "");
                    		po.setPaymentType(inv.getMetodoPago());
                    		po.setStatus(AppConstants.STATUS_OC_INVOICED);
                    		po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
                    	}
                    	
                    	
                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)){
                    		po.setPaymentUuid(inv.getUuid());
                    		po.setStatus(AppConstants.STATUS_LOADNC);
                    	}
                    	
                    	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante)){
                    		po.setCreditNotUuid(inv.getUuid());
                    		po.setStatus(AppConstants.STATUS_LOADCP);
                    	}
                    	
                    	
                    	purchaseOrderService.updateOrders(po);
                    	
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("orderNumber", documentNumber);
        	            json.put("orderType", documentType);
        	            json.put("addressNumber", addressBook);
        	            json.put("docNbr", res);
        	            json.put("uuid", inv.getUuid());
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}

                }else{
                	json.put("success", false);
                	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificación y tampoco sera cargado a la solicitud.");
                }
                
            }else{
            	json.put("success", false);
            	json.put("message", "Para cargas de archivos fiscales, sólo se permiten archivos .xml");
            }
        }else{
        	
            if(uploadItem.getFile() != null) {
                String ctPdf = uploadItem.getFile().getContentType();
                if(!"application/pdf".equals(ctPdf)) {
                	json.put("success", false);
                	json.put("message", "Sólo se permiten archivos tipo PDF");
                	return json.toString();
                }
            }
        	
        	PurchaseOrder po = purchaseOrderService.searchbyOrder(documentNumber, documentType);
        	UserDocument doc = new UserDocument(); 
        	
        	if(po != null) {
        		
        		String fileName = uploadItem.getFile().getOriginalFilename();
        		fileName = fileName.replaceAll(" ", "_");
        		
            	doc.setAddressBook(po.getAddressNumber());
            	doc.setDocumentNumber(documentNumber);
            	doc.setDocumentType(documentType);
            	doc.setContent(uploadItem.getFile().getBytes());
            	doc.setType(ct.trim());
            	doc.setName(fileName);
            	doc.setSize(uploadItem.getFile().getSize());
            	doc.setStatus(true);
            	doc.setAccept(true);
            	doc.setFiscalType(tipoComprobante);
            	doc.setUuid("");
            	doc.setUploadDate(new Date());
            	doc.setFiscalRef(0);
            	documentsService.save(doc, new Date(), "");
            	
                json.put("success", true);
                json.put("message", "El archivo ha sido cargado de forma exitosa");
                json.put("orderNumber", documentNumber);
                json.put("orderType", documentType);
                json.put("addressNumber", addressBook);
        	}else {
                json.put("success", false);
                json.put("message", "El archivo no pertenece al proveedor de la sesión");
        	}

            
        }
        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();

    }
    
	@SuppressWarnings("unused")
	@RequestMapping(value = "/uploadComplPago.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    @ResponseBody public String uploadInvoiceFromOrder(FileUploadBean uploadItem, String[] orders,  String[] invoices, String addressBook, 
    										           BindingResult result){
 
		
    	JSONObject json = new JSONObject();    	
    	String tipoComprobante = AppConstants.PAYMENT_FIELD;
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();

        try{
	        //Cambia a los uuids a mayúsculas 
	        Arrays.asList(invoices).replaceAll(String::toUpperCase);
	        
	        if (result.hasErrors()){
	            for(ObjectError error : result.getAllErrors()){
	                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
	            }
	 
	            json.put("success", false);
	            json.put("message", "Error al cargar el archivo");
	        }
	        
	        if(uploadItem.getFileTwo() != null) {
	            String ctPdf = uploadItem.getFileTwo().getContentType();
	            if(!"application/pdf".equals(ctPdf)) {
	            	json.put("success", false);
	            	json.put("message", "Error_2");
	            	return json.toString();
	            }
	        }
	        
	        InvoiceDTO inv = null;
	        String ct = uploadItem.getFile().getContentType();
	        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
	            if("text/xml".equals(ct.trim())){
	                inv = documentsService.getInvoiceXml(uploadItem);
	                if(inv != null){
	                	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante) && !"P".equals(inv.getTipoComprobante())){
	                		json.put("success", false);
	                    	json.put("message", "El documento cargado no corresponde a un COMPLEMENTO DE PAGO <br />(Tipo Comprobante = P" + ")");
	                    	return json.toString();
	                	}
	                	
	                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
	                    String xmlContent = IOUtils.toString(stream, "UTF-8");
	                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
	        			String xmlString = source.replace("?<?xml", "<?xml");
	                    
	                    String res = "";
	                    UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
	        			if(udcCfdi != null) {
	        				if(!"".equals(udcCfdi.getStrValue1())) {
	        					if("TRUE".equals(udcCfdi.getStrValue1())) {
	        	            		res = documentsService.validaComprobanteSAT(inv);
	        	            		if(!"".equals(res)) {
	        	            			json.put("success", false);
	        	                    	json.put("message", res);
	        	                    	return json.toString();
	        	            		}
	        					}
	        				}
	        			}else {
	                		res = documentsService.validaComprobanteSAT(inv);
	                		if(!"".equals(res)) {
		            			json.put("success", false);
		                    	json.put("message", res);
		                    	return json.toString();
	                		}
	        			}
	        			
	        			String fechaFactura = inv.getFechaTimbrado();
	        			fechaFactura = fechaFactura.replace("T", " ");
	        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        			Date invDate = null;
	        			try {
	        				invDate = sdf.parse(fechaFactura);
	        			}catch(Exception e) {
	        				log4j.error("Exception" , e);
	        				e.printStackTrace();
	        			}
	        			
	        			//Validación CFDI Versión 3.3
	        			/*
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
	    	    	            			json.put("success", false);
	    	    	                    	json.put("message", "La versión del CFDI no es válida.");
	    	    	                    	return json.toString();
	        							}
	        						}
	        					} catch (Exception e) {
	        						log4j.error("Exception" , e);
	        						e.printStackTrace();
	    	            			json.put("success", false);
	    	                    	json.put("message", "Error al obtener la fecha de timbrado del comprobante");
	    	                    	return json.toString();
	        					}
	        				}
	        			}
	                    */
	        			
	        			// START J.AVILA: Se desactiva la validación estricta de UUIDS de Facturas contenidas en el complemento
	        			/*
	                    List<String> invListCompl = new ArrayList<String>();
	        			List<Pago> pagosXml = inv.getComplemento().getPago().getPago();
	        			for(Pago p : pagosXml) {
	            			List<DoctoRelacionado> drList = p.getDoctoRelacionado();
	            			for(DoctoRelacionado dr : drList) {
	            				invListCompl.add(dr.getIdDocumento());
	            			}
	        			}
	        			
	        			if(invListCompl.size() != invoices.length) {
	        				json.put("success", false);
	                    	json.put("message", "La cantidad de facturas seleccionadas es diferente a la cantidad de facturas contenidas en el Complemento de Pago");
	                    	return json.toString();
	        			}
	        			
	        			List<String> invListSelectedOriginal = Arrays.asList(invoices); 
	        			List<String> invListSelected = new ArrayList<String>(); 
	        			for(String q : invListSelectedOriginal) {
	        				invListSelected.add(q.toUpperCase());
	        			}
	        			
	        			for(String uuidCompl : invListCompl) {
	        				 if(!invListSelected.contains(uuidCompl)) {
	        					 json.put("success", false);
	                         	json.put("message", "Existen facturas en su selección que no se encuentran dentro del Complemento de Pago");
	                         	return json.toString(); 
	        				 }
	        			 }
	        			 
	        			 for(String uuidSel : invListSelected) {
	        				 if(!invListCompl.contains(uuidSel)) {
	        					 json.put("success", false);
	                         	json.put("message", "Existen facturas en el Complemento de Pago que no se encuentran dentro de su selección de facturas");
	                         	return json.toString(); 
	        				 }
	        			 }
	        			 END JAVILA
	        			 */
	        			
	            		Supplier s = supplierService.searchByAddressNumber(addressBook);
	            		
	            		String rfcEmisor = inv.getRfcEmisor();
	            		if(rfcEmisor != null) {
	            			if(!"".equals(rfcEmisor)) {
	            				if(!s.getRfc().equals(rfcEmisor)) {
	    	            			json.put("success", false);
	    	                    	json.put("message", "El complemento de pago no pertenece al emisor " + s.getRfc());
	    	                    	return json.toString();
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
	            			json.put("success", false);
	                    	json.put("message", "El receptor " + inv.getRfcReceptor() + " no es permitido para carga de complementos");
	                    	return json.toString();
	            		}
	                	
	            		List<String> uuidList = new ArrayList<String>();
	            		List<Pago> pagos = inv.getComplemento().getPago().getPago();
	            		
	            		for(Pago p : pagos) {            		
	            			List<DoctoRelacionado> dList = p.getDoctoRelacionado();
	        				String uuid = "";
	        				
	        				if(dList != null && !dList.isEmpty()) {
	                			for(DoctoRelacionado d : dList) {
	                				uuid = d.getIdDocumento().trim();
	                				
	                				if(Arrays.asList(invoices).contains(uuid.toUpperCase())) {
	                            		List<Receipt> receiptList = purchaseOrderService.getReceiptsByUUID(uuid);
	                    				if(receiptList != null && !receiptList.isEmpty()) {
	                    					uuidList.add(uuid);
	                    					for(Receipt r : receiptList) {
	                    						String oCurr = "";
	                    						if("PME".equals(r.getCurrencyCode())) {
	                    							oCurr = "MXN";
	                    						}else {
	                    							oCurr = r.getCurrencyCode();
	                    						}
	                    						
	                    						if(!d.getMonedaDR().equals(oCurr)) {
	                        						json.put("success", false);
	                    	                    	json.put("message", "Error: La clave de moneda para el " + uuid + " son diferentes en el complemento y la factura.");
	                    	                    	return json.toString();
	                        					}
	                      					}
	                    				}	
	                				}                				
	                			}	
	        				} else {
	                			json.put("success", false);
	                        	json.put("message", "El complemento de pago cargado no cuenta con documentos relacionados, revise su comprobante.");
	                        	return json.toString();
	        				}
	            		}

	            		int orderNumber = 0;
	            		String orderType = "";
	            		String addressNumber = "";
	            		String documentNumber ="";
	            		if("".equals(res)){
	            			
	            			for(String uuid : uuidList) {
	            				//Actualiza registros de Receipt y Purchase Order
	            				List<Receipt> rList = purchaseOrderService.getReceiptsByUUID(uuid);
	            				for(Receipt r_ : rList) {
	            					r_.setComplPagoUuid(inv.getUuid());
	                    			r_.setStatus(AppConstants.STATUS_OC_PAYMENT_COMPL);
	                                purchaseOrderService.updateReceipt(r_);
	                                orderNumber = r_.getOrderNumber();
	                                orderType = r_.getOrderType();
	                                addressNumber = r_.getAddressNumber();
	                                documentNumber = documentNumber + r_.getDocumentNumber() +",";
	                                PurchaseOrder po_ = purchaseOrderService.searchbyOrderAndAddressBook(orderNumber, addressNumber, orderType);
	                                po_.setOrderStauts(AppConstants.STATUS_OC_PAYMENT_COMPL);
	                                purchaseOrderService.updateOrders(po_);
	            				}
	            				
	            				//Actualiza registros de Fiscal Documents
								FiscalDocuments fd = fiscalDocumentService.getFiscalDocumentsByUuid(uuid);
								if(fd != null) {
        							fd.setComplPagoUuid(inv.getUuid());
        							fd.setStatus(AppConstants.STATUS_COMPLEMENT);
        							fiscalDocumentService.updateDocument(fd);
								}
	            			}
	                		
	        				UserDocument doc = new UserDocument(); 
	                    	doc.setAddressBook(addressNumber);
	                    	doc.setDocumentNumber(orderNumber);
	                    	doc.setDocumentType(orderType);
	                    	doc.setContent(uploadItem.getFile().getBytes());
	                    	doc.setType(ct.trim());
	                    	//doc.setName(uploadItem.getFile().getOriginalFilename());
	                    	doc.setName("COMPL_" + inv.getUuid() + ".xml");
	                    	doc.setSize(uploadItem.getFile().getSize());
	                    	doc.setStatus(true);
	                    	doc.setAccept(true);
	                    	doc.setFiscalType(tipoComprobante);
	                    	doc.setType("text/xml");
	                    	//doc.setType(tipoComprobante);
	                    	doc.setFolio(inv.getFolio());
	                    	doc.setSerie(inv.getSerie());
	                    	doc.setUuid(inv.getUuid());
	                    	doc.setUploadDate(new Date());
	                    	doc.setFiscalRef(0);
	                    	documentsService.save(doc, new Date(), "");
	                    	
	                    	dataAuditService.saveDataAudit("UploadComplInvoice",addressNumber, currentDate, request.getRemoteAddr(),
	                   		userAuth, "COMPL_" + inv.getUuid() + ".xml", "uploadInvoiceFromOrder", 
	                   		"Uploaded Compl Payment Successful",documentNumber+"", orderNumber+"", null, 
	                   		inv.getUuid(), AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);
	                    	
	                		doc = new UserDocument(); 
	                    	doc.setAddressBook(addressNumber);
	                    	doc.setDocumentNumber(orderNumber);
	                    	doc.setDocumentType(orderType);
	                    	doc.setContent(uploadItem.getFileTwo().getBytes());
	                    	doc.setType(uploadItem.getFileTwo().getContentType().trim());
	                    	doc.setName("COMPL_" + inv.getUuid() + ".pdf");
	                    	doc.setSize(uploadItem.getFileTwo().getSize());
	                    	doc.setStatus(true);
	                    	doc.setAccept(true);
	                    	doc.setFiscalType(tipoComprobante);
	                    	doc.setType("application/pdf");
	                    	doc.setFolio(inv.getFolio());
	                    	doc.setSerie(inv.getSerie());
	                    	doc.setUuid(inv.getUuid());
	                    	doc.setUploadDate(new Date());
	                    	doc.setFiscalRef(0);
	                    	documentsService.save(doc, new Date(), "");
	                    	
	                    	dataAuditService.saveDataAudit("UploadComplInvoice",addressNumber, currentDate, request.getRemoteAddr(),
	    	                userAuth, "COMPL_" + inv.getUuid() + ".pdf", "uploadInvoiceFromOrder", 
	    	                "Uploaded Compl Payment Successful",documentNumber+"", orderNumber+"", null, 
	    	                inv.getUuid(), AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);
	                	} 

	        	            json.put("success", true);
	        	            json.put("message", inv.getMessage());
	        	            json.put("docNbr", res);
	        	            json.put("uuid", inv.getUuid());
	                    	
	                	}else{
	                    	json.put("success", false);
	                    	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificacion y tampoco sera cargado a la solicitud.");
	                	}

	                }else{
	                	json.put("success", false);
	                	json.put("message", "El formato del archivo no es aceptado.  <br />NO ha pasado la fase de verificacion y tampoco sera cargado a la solicitud.");
	                }
	                
	            }else{
	            	json.put("success", false);
	            	json.put("message", "Para cargas de archivos fiscales, sólo se permiten de tipo .xml");
	            }
	        
	        return json.toString();
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " + e.getMessage());
        }
        return json.toString();

    }

	@SuppressWarnings("unused")
	@RequestMapping(value = "/uploadComplPagoFD.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    @ResponseBody public String uploadComplPagoFD(FileUploadBean uploadItem, String[] documents, String[] invoices, String addressBook, 
    										           BindingResult result){
 
    	JSONObject json = new JSONObject();    	
    	String tipoComprobante = AppConstants.PAYMENT_FIELD;

        try{
            //Cambia a los uuids a mayúsculas 
            Arrays.asList(invoices).replaceAll(String::toUpperCase);
            
            if (result.hasErrors()){
                for(ObjectError error : result.getAllErrors()){
                    System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
                }
     
                json.put("success", false);
                json.put("message", "Error al cargar el archivo");
            }
            
            InvoiceDTO inv = null;
            String ct = uploadItem.getFile().getContentType();
            if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
                if("text/xml".equals(ct.trim())){
                    inv = documentsService.getInvoiceXml(uploadItem);
                    if(inv != null){
                    	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante) && !"P".equals(inv.getTipoComprobante())){
                    		json.put("success", false);
                        	json.put("message", "El documento cargado no corresponde a un COMPLEMENTO DE PAGO <br />(Tipo Comprobante = P" + ")");
                        	return json.toString();
                    	}
                    	
                        ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
                        String xmlContent = IOUtils.toString(stream, "UTF-8");
                        String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
            			String xmlString = source.replace("?<?xml", "<?xml");
                        
                        String res = "";
            			/*
                        UDC udcCfdi = udcService.searchBySystemAndKey("VALIDATE", "CFDI");
            			if(udcCfdi != null) {
            				if(!"".equals(udcCfdi.getStrValue1())) {
            					if("TRUE".equals(udcCfdi.getStrValue1())) {
            	            		res = documentsService.validaComprobanteSATPagos(xmlString);
            	            		if(!"".equals(res)) {
            	            			json.put("success", false);
            	                    	json.put("message", "El documento cargado no es aceptable ante el SAT. Verifique su archivo e intente nuevamente.");
            	                    	return json.toString();
            	            		}
            					}
            				}
            			}else {
                    		res = documentsService.validaComprobanteSATPagos(xmlString);
                    		if(!"".equals(res)) {
    	            			json.put("success", false);
    	                    	json.put("message", "El documento cargado no es aceptable ante el SAT. Verifique su archivo e intente nuevamente.");
    	                    	return json.toString();
                    		}
            			}
            			*/
            			
	        			String fechaFactura = inv.getFechaTimbrado();
	        			fechaFactura = fechaFactura.replace("T", " ");
	        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        			Date invDate = null;
	        			try {
	        				invDate = sdf.parse(fechaFactura);
	        			}catch(Exception e) {
	        				log4j.error("Exception" , e);
	        				e.printStackTrace();
	        			}
	        			
	        			//Validación CFDI Versión 3.3
	        			/*
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
	    	    	            			json.put("success", false);
	    	    	                    	json.put("message", "La versión del CFDI no es válida.");
	    	    	                    	return json.toString();
	        							}
	        						}
	        					} catch (Exception e) {
	        						log4j.error("Exception" , e);
	        						e.printStackTrace();
	    	            			json.put("success", false);
	    	                    	json.put("message", "Error al obtener la fecha de timbrado del comprobante");
	    	                    	return json.toString();
	        					}
	        				}
	        			}
	        			*/
	        			
                		Supplier s = supplierService.searchByAddressNumber(addressBook);
                		
                		String rfcEmisor = inv.getRfcEmisor();
                		if(rfcEmisor != null) {
                			if(!"".equals(rfcEmisor)) {
                				if(!s.getRfc().equals(rfcEmisor)) {
        	            			json.put("success", false);
        	                    	json.put("message", "El complemento de pago no pertenece al emisor " + s.getRfc());
        	                    	return json.toString();
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
                			json.put("success", false);
                        	json.put("message", "El receptor " + inv.getRfcReceptor() + " no es permitido para carga de complementos");
                        	return json.toString();
                		}
                		            		
                		List<Pago> pagos = inv.getComplemento().getPago().getPago();
                		for(Pago p : pagos) {
                			List<DoctoRelacionado> dList = p.getDoctoRelacionado();
            				String uuid = "";
            				
            				if(dList != null && !dList.isEmpty()) {
                    			for(DoctoRelacionado d : dList) {
                    				uuid = d.getIdDocumento().trim();
                    				
                    				if(Arrays.asList(invoices).contains(uuid.toUpperCase())) {
                        				List<FiscalDocuments> fdList = fiscalDocumentService.getFiscalDocuments(s.getAddresNumber(), AppConstants.STATUS_PAID, uuid, "", 0, 20,"");
                        				
                        				if(fdList != null && !fdList.isEmpty()) {
                        					for(FiscalDocuments fd : fdList) {
                        						String oCurr = "";
                        						if("PME".equals(fd.getCurrencyCode())) {
                        							oCurr = "MXN";
                        						}else {
                        							oCurr = fd.getCurrencyCode();
                        						}
                        						
                        						if(!d.getMonedaDR().equals(oCurr)) {
                            						json.put("success", false);
                        	                    	json.put("message", "Error: La clave de moneda para el uuid " + uuid + " son diferentes en el complemento y la factura.");
                        	                    	return json.toString();
                            					}
                          					}
                        					
                        					for(FiscalDocuments fd : fdList) {
                            					for(String i : documents) {
                            						if(fd.getId() == Integer.valueOf(i).intValue()) {
                            							fd.setComplPagoUuid(inv.getUuid());
                            							fd.setStatus(AppConstants.STATUS_COMPLEMENT);
                            							fiscalDocumentService.updateDocument(fd);
                            						}
                            					}	
                        					}
                        					
                            				UserDocument doc = new UserDocument(); 
                                        	doc.setAddressBook(s.getAddresNumber());
                                        	doc.setDocumentNumber(0);
                                        	doc.setDocumentType(AppConstants.PAYMENT_FIELD);
                                        	doc.setContent(uploadItem.getFile().getBytes());
                                        	doc.setType(ct.trim());
                                        	//doc.setName(uploadItem.getFile().getOriginalFilename());
                                        	doc.setDescription("MainUUID_".concat(inv.getUuid()));
                                        	doc.setName(inv.getUuid() + ".xml");
                                        	doc.setSize(uploadItem.getFile().getSize());
                                        	doc.setStatus(true);
                                        	doc.setAccept(true);
                                        	doc.setFiscalType(tipoComprobante);
                                        	doc.setType("text/xml");
                                        	//doc.setType(tipoComprobante);
                                        	doc.setFolio(inv.getFolio());
                                        	doc.setSerie(inv.getSerie());
                                        	doc.setUuid(inv.getUuid());
                                        	doc.setUploadDate(new Date());
                                        	doc.setFiscalRef(0);
                                        	documentsService.save(doc, new Date(), "");                                        
                        				}
                    				}
                    			}
            				} else {
                    			json.put("success", false);
                            	json.put("message", "El complemento de pago cargado no cuenta con documentos relacionados, revise su comprobante.");
                            	return json.toString();
            				}
                		}
                		
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("docNbr", res);
        	            json.put("uuid", inv.getUuid());
                        	
                    	}else{
                        	json.put("success", false);
                        	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificacion y tampoco sera cargado a la solicitud.");
                    	}

                    }else{
                    	json.put("success", false);
                    	json.put("message", "El formato del archivo no es aceptado.  <br />NO ha pasado la fase de verificacion y tampoco sera cargado a la solicitud.");                    	
                    }
                    
                }else{
                	json.put("success", false);
                	json.put("message", "Para cargas de archivos fiscales, sólo se permiten de tipo .xml");
                }
            return json.toString();
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();

    }
	
    @RequestMapping(value = "/uploadInvoiceFromOrderWithoutPayment.action", method = RequestMethod.POST)
    @ResponseBody public String uploadInvoiceFromOrderWithoutPayment(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  int documentNumber, 
											  String documentType,
											  String tipoComprobante,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }
        
        InvoiceDTO inv = null;
        String ct = uploadItem.getFile().getContentType();
        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
            if("text/xml".equals(ct.trim())){
                inv = documentsService.getInvoiceXml(uploadItem);
                if(inv != null){
                	
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no es de tipo FACTURA<br />(Tipo Comprobante = I" + ")");
                    	return json.toString();
                	}
                	
                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
                    String xmlContent = IOUtils.toString(stream, "UTF-8");
                	
        			PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
                	String res = documentsService.validateInvoiceFromOrderWithoutPayment(inv,
                														   addressBook, 
																		   documentNumber, 
																		   documentType,
																		   tipoComprobante,
																		   po,
																		   xmlContent);
                	if("".equals(res)){
                    	UserDocument doc = new UserDocument(); 
                    	doc.setAddressBook(addressBook);
                    	doc.setDocumentNumber(documentNumber);
                    	doc.setDocumentType(documentType);
                    	doc.setContent(uploadItem.getFile().getBytes());
                    	doc.setType(ct.trim());
                    	doc.setName(uploadItem.getFile().getOriginalFilename());
                    	doc.setSize(uploadItem.getFile().getSize());
                    	doc.setStatus(false);
                    	doc.setAccept(false);
                    	doc.setFiscalType(tipoComprobante);
                    	doc.setType(tipoComprobante);
                    	doc.setFolio(inv.getFolio());
                    	doc.setSerie(inv.getSerie());
                    	doc.setUuid(inv.getUuid());
                    	doc.setUploadDate(new Date());
                    	doc.setFiscalRef(0);
                    	documentsService.save(doc, new Date(), "");
                    	
                    	po.setStatus(AppConstants.STATUS_LOADINV_VALIDATE);
                    	purchaseOrderService.updateOrders(po);
                    	
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("orderNumber", documentNumber);
        	            json.put("orderType", documentType);
        	            json.put("addressNumber", addressBook);
        	            json.put("uuid", inv.getUuid());
        	            return json.toString();
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                    	return json.toString();
                	}

                }else{
                	json.put("success", false);
                	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificación y tampoco sera cargado a la solicitud.");
                	return json.toString();
                }
                
            }else{
            	json.put("success", false);
            	json.put("message", "Para cargas de archivos fiscales, sólo se permiten tipos .xml");
            }
        }
        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        
        return json.toString();
    }
    
    
    @RequestMapping(value = "/processInvoiceFromOrderWithoutPayment.action", method = RequestMethod.POST)
    @ResponseBody public String processInvoiceFromOrderWithoutPayment(
    										  String addressBook, 
											  int documentNumber, 
											  String documentType){
 
    	JSONObject json = new JSONObject();
        try{

        UserDocument doc = null;
        InvoiceDTO inv = null;
        List<UserDocument> list = documentsService.searchCriteriaByOrderNumber(documentNumber, documentType, addressBook);
        if(list != null) {
        	if(!list.isEmpty()) {
        		for(UserDocument o : list) {
        			if(!o.isAccept()) {
        				doc = o;
        				break;
        			}
        		}
        	}
        }
        
        if(doc!= null) {
        	String xmlContent = new String(doc.getContent());
        	inv = documentsService.getInvoiceXmlFromString(xmlContent);
        	if(inv != null) {
                
    			PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
            	String res = documentsService.validateInvoiceFromOrder(inv,
            														   addressBook, 
																	   documentNumber, 
																	   documentType,
																	   AppConstants.INVOICE_FIELD,
																	   po, 
																	   true,
																	   xmlContent,
																	   "",
																	   false,
																	   0);
            	if("".equals(res)){
                	doc.setStatus(true);
                	doc.setAccept(true);
                	documentsService.update(doc, new Date(), "");
                	
                	po.setInvoiceUuid(inv.getUuid());
                	po.setStatus(AppConstants.STATUS_LOADINV);
                	purchaseOrderService.updateOrders(po);
                	
                	json.put("success", true);
                	json.put("message", "La factura ha sido procesada de forma exitosa");
                	return json.toString();
    	            
            	}else {
                	json.put("success", false);
                	json.put("message", res);
                	return json.toString();
            	}
        		
        	}else {
	        	json.put("success", false);
	        	json.put("message", "No se ha podido recuperar la factura");
	        	return json.toString();
        	}
          }
        }catch(Exception e) {
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
            return json.toString();
        }
        
        json.put("success", false);
    	json.put("message", "Ha ocurrido un error inesperado mientras se procesaba la factura");
        return json.toString();
        
    }
    
    

    @RequestMapping(value = "/rejectInvoiceFromOrderWithoutPayment.action", method = RequestMethod.POST)
    @ResponseBody public String rejectInvoiceFromOrderWithoutPayment(
    										  String addressBook, 
											  int documentNumber, 
											  String documentType){
 
    	JSONObject json = new JSONObject();
        try{

        UserDocument doc = null;
        List<UserDocument> list = documentsService.searchCriteriaByOrderNumber(documentNumber, documentType, addressBook);
        if(list != null) {
        	if(!list.isEmpty()) {
        		for(UserDocument o : list) {
        			if(!o.isAccept()) {
        				doc = o;
        				break;
        			}
        		}
        	}
        }
        
        if(doc!= null) {
                	doc.setStatus(true);
                	doc.setAccept(true);
                	documentsService.delete(doc.getId(),"");
                	documentsService.rejectInvoice(addressBook, documentNumber, documentType);

                	json.put("success", true);
                	json.put("message", "La factura ha sido procesada de forma exitosa");
                	return json.toString();
            	}
        }catch(Exception e) {
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
            return json.toString();
        }
        
        json.put("success", false);
    	json.put("message", "Ha ocurrido un error inesperado");
    	return json.toString();
        
    }
    
    @RequestMapping(value = "/uploadInvoice.action", method = RequestMethod.POST)
    @ResponseBody public String uploadInvoice(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  String tipoComprobante,
											  int fdId,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }
        
        InvoiceDTO inv = null;
        String ct = uploadItem.getFile().getContentType();
        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
            if("text/xml".equals(ct.trim())){
                inv = documentsService.getInvoiceXml(uploadItem);
                if(inv != null){
                	
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no es de tipo FACTURA<br />(Tipo Comprobante = I" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.NC_FIELD.equals(tipoComprobante) && !"E".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no coresponde a una NOTA DE CREDITO<br />(Tipo Comprobante = E" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante) && !"P".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no corresponde a un COMPLEMENTO DE PAGO <br />(Tipo Comprobante = P" + ")");
                    	return json.toString();
                	}
                	
        			//String res = documentsService.validateInvoice(inv,addressBook,tipoComprobante);
                	
                	String res="";
                	if("".equals(res)){
                    	UserDocument doc = new UserDocument(); 
                    	doc.setAddressBook(addressBook);
                    	doc.setDocumentNumber(0);
                    	doc.setDocumentType("");
                    	doc.setContent(uploadItem.getFile().getBytes());
                    	doc.setType(ct.trim());
                    	doc.setName(uploadItem.getFile().getOriginalFilename());
                    	doc.setSize(uploadItem.getFile().getSize());
                    	doc.setStatus(true);
                    	doc.setAccept(true);
                    	doc.setFiscalType(tipoComprobante);
                    	doc.setType(tipoComprobante);
                    	doc.setFolio(inv.getFolio());
                    	doc.setSerie(inv.getSerie());
                    	doc.setUuid(inv.getUuid());
                    	doc.setUploadDate(new Date());
                    	doc.setFiscalRef(fdId);
                    	documentsService.save(doc, new Date(), "");
                    	
                    	FiscalDocuments fd = null;
                    	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
                    		fd = new FiscalDocuments();
                    	}
                    	
                    	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante)){
                    		fd = fiscalDocumentService.getById(fdId);
                    	}
                    	
                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)){
                    		fd = fiscalDocumentService.getById(fdId);
                    	}
                    	
                    	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
                        	fd.setFolio(inv.getFolio());
                        	fd.setSerie(inv.getSerie());
                    		//fd.setInvoiceUploadDate(new Date());
                    		fd.setUuidFactura(inv.getUuid());
                    		fd.setUuidPago("");
                    		fd.setUuidNotaCredito("");
                    		fd.setFolioPago("");
                    		fd.setSeriePago("");
                    		fd.setFolioNC("");
                    		fd.setSerieNC("");
                        	fd.setStatus(AppConstants.STATUS_LOADINV);
                        	fd.setAddressNumber(addressBook);
                        	fd.setAmount(inv.getTotal());
                        	fd.setSubtotal(inv.getSubTotal());
                        	fd.setDescuento(inv.getDescuento());
                        	fd.setImpuestos(inv.getTotalImpuestos());
                        	fd.setRfcEmisor(inv.getRfcEmisor());
                        	fd.setRfcReceptor(inv.getRfcReceptor());
                        	fd.setMoneda(inv.getMoneda());
                        	fd.setInvoiceDate(inv.getFechaTimbrado());
                    	}
                    	
                    	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante)){
                    		fd.setFolioPago(inv.getFolio());
                    		fd.setSeriePago(inv.getSerie());
                    		fd.setUuidPago(inv.getUuid());
                    		//fd.setPaymentUploadDate(new Date());
                        	fd.setStatus(AppConstants.STATUS_LOADCP);
                    	}
                    	
                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)){
                    		fd.setUuidNotaCredito(inv.getUuid());
                    		//fd.setCreditNoteUploadDate(new Date());
                    		fd.setFolioNC(inv.getFolio());
                    		fd.setSerieNC(inv.getSerie());
                        	fd.setStatus(AppConstants.STATUS_LOADNC);
                    	}
                    	
                    	if(fdId == 0)
                    	    fiscalDocumentService.saveDocument(fd);
                    	else
                    		fiscalDocumentService.updateDocument(fd);
                    	
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("addressNumber", addressBook);
        	            json.put("uuid", inv.getUuid());
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}

                }else{
                	json.put("success", false);
                	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificación y tampoco sera cargado a la solicitud.");
                }
                
            }else{
            	json.put("success", false);
            	json.put("message", "Para cargas de archivos fiscales, sólo se permiten tipos .xml");
            }
        }else{
        	UserDocument doc = new UserDocument(); 
        	doc.setAddressBook(addressBook);
        	doc.setDocumentNumber(0);
        	doc.setDocumentType("");
        	doc.setContent(uploadItem.getFile().getBytes());
        	doc.setType(ct.trim());
        	doc.setName(uploadItem.getFile().getOriginalFilename());
        	doc.setSize(uploadItem.getFile().getSize());
        	doc.setStatus(true);
        	doc.setAccept(true);
        	doc.setFiscalType(tipoComprobante);
        	doc.setUuid("");
        	doc.setUploadDate(new Date());
        	doc.setFiscalRef(fdId);
        	documentsService.save(doc, new Date(), "");
        	
            json.put("success", true);
            json.put("message", "El archivo ha sido cargado de forma exitosa");
            json.put("addressNumber", addressBook);
            
        }
        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();

    }
    
    @RequestMapping(value = "/uploadExcelSuppliers.action", method = RequestMethod.POST)
    @ResponseBody public String uploadSuppliers(FileUploadBean uploadItem, 
    								   BindingResult result, HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }

        if(uploadItem.getFile().getOriginalFilename().endsWith(".xlsx") || uploadItem.getFile().getOriginalFilename().endsWith(".xls") ){

        	org.json.JSONObject count = documentsService.processExcelFile(uploadItem);
        	//String respProcEF = count.getString("message");
        	
        	if(count.getBoolean("success")) {
        		json.put("success", true);
                json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " ha sido cargado exitosamente.");
                json.put("fileName", uploadItem.getFile().getOriginalFilename());
                json.put("count", count.getInt("count"));
        	}else {
        		json.put("success", false);
        		json.put("error_data_template", true);
            	json.put("message_es", count.getString("message_es"));
            	json.put("message_en", count.getString("message_en"));
        	}
            
        }else{
        	json.put("success", false);
        	json.put("message", "Error: Sólo se permiten archivos tipo .xlsx o .xls");
        }

        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
        	json.put("message", e.getMessage());

        }
        return json.toString();

    }
    
    
    @RequestMapping(value = "/uploadExcelCodigosSat.action", method = RequestMethod.POST)
    @ResponseBody public String uploadCodigosSat(FileUploadBean uploadItem, 
    								   BindingResult result, HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		String ip = request.getRemoteAddr();
 		Date currentDate = new Date();

        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }

        if(uploadItem.getFile().getOriginalFilename().endsWith(".xlsx") || uploadItem.getFile().getOriginalFilename().endsWith(".xls") ){

        	Workbook workbook = null;
        	workbook = WorkbookFactory.create(uploadItem.getFile().getInputStream());
        	 //int count = codigosSATService.processExcelFile(uploadItem);
        	 BatchProcessService bps = new BatchProcessService();
        	 bps.setCodigoSatDao(codigosSatDao);
        	 bps.setLogger(logger);
        	 bps.setFile(workbook);
        	 Thread codSatThread = new Thread(bps);
        	 codSatThread.start();

            json.put("success", true);
            json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " ha sido sometido al proceso batch.");
            json.put("fileName", uploadItem.getFile().getOriginalFilename());
            json.put("count", 1);
            
			dataAuditService.saveDataAudit("UploadExcelCodigosSat", null, currentDate, ip,
	 		userAuth, uploadItem.getFile().getOriginalFilename(), "save", "Upload Excel CodigosSat Successful" ,null, null, null, 
	 		null, AppConstants.STATUS_COMPLETE, AppConstants.SAT_MODULE);
            
        }else{
        	json.put("success", false);
        	json.put("message", "Error: Sólo se permiten archivos tipo .xlsx o .xls");
        }

        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
        	json.put("message", e.getMessage());

        }
        return json.toString();

    }


    @RequestMapping("/uploadResources.action")
    @ResponseBody public String  uploadResources(FileUploadBean file,
    							   BindingResult result, 
 								   String addressBook, 
 								   int documentNumber, 
 								   String documentType,
 								   String tipoComprobante,
 		                           HttpServletResponse response){

    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();

        String invCT = file.getFile().getContentType();
        String cnCT = file.getFileTwo().getContentType();

        if("text/xml".equals(invCT) && "text/xml".equals(cnCT) ){
            InvoiceDTO inv = documentsService.getInvoiceXml(file);
            InvoiceDTO cn = documentsService.getCreditNoteXml(file);
            
            if(inv != null && cn != null){
            	
            	if(!"I".equals(inv.getTipoComprobante())){
            		json.put("success", false);
                	json.put("message", "La factura cargada no es de tipo Tipo Comprobante = I");
                	return json.toString();
            	}
            	
            	if(!"E".equals(cn.getTipoComprobante())){
            		json.put("success", false);
            		json.put("message", "La nota de crédito cargada no es de tipo Tipo Comprobante = E");
                	return json.toString();
            	}
            	
            	PurchaseOrder po = purchaseOrderService.getOrderByOrderAndAddresBook(documentNumber, addressBook, documentType);
            	
            	if(po != null) {
                	String res = "";
                	String xmlInvContent = "";
                	String xmlCNContent = "";
                	
            		try {
            		ByteArrayInputStream stream = new  ByteArrayInputStream(file.getFile().getBytes());
        			xmlInvContent = IOUtils.toString(stream, "UTF-8");
        			
            		stream = new  ByteArrayInputStream(file.getFileTwo().getBytes());
        			xmlCNContent = IOUtils.toString(stream, "UTF-8");
        			
                	res = documentsService.validateInvoiceFromOrderWitCreditNote(inv, cn,
 						   addressBook, 
 						   documentNumber, 
 						   documentType,
 						   AppConstants.INVOICE_FIELD,
 						   po, false,
 						   xmlInvContent,
 						   xmlCNContent);
        			
            		}catch(Exception e) {
            			log4j.error("Exception" , e);
            			e.printStackTrace();
            		}
                	
                		if("".equals(res)) {
                			
                			if(!"".equals(xmlInvContent) && !"".equals(xmlCNContent)) {
                				
                    			res = documentsService.processInvoiceAndCreditNoteFromOrder(inv,
										   cn,
		                                   addressBook, 
		           						   documentNumber, 
		          						   documentType,
		          						   po);
                    			
                    			if("".equals(res)){
                                	UserDocument doc = new UserDocument(); 
                                	doc.setAddressBook(po.getAddressNumber());
                                	doc.setDocumentNumber(documentNumber);
                                	doc.setDocumentType(documentType);
                                	doc.setContent(file.getFile().getBytes());
                                	doc.setType(invCT.trim());
                                	doc.setName(file.getFile().getOriginalFilename());
                                	doc.setSize(file.getFile().getSize());
                                	doc.setStatus(true);
                                	doc.setAccept(true);
                                	doc.setFiscalType(inv.getTipoComprobante());
                                	doc.setType(inv.getTipoComprobante());
                                	doc.setFolio(inv.getFolio());
                                	doc.setSerie(inv.getSerie());
                                	doc.setUuid(inv.getUuid());
                                	doc.setUploadDate(new Date());
                                	doc.setFiscalRef(0);
                                	documentsService.save(doc, new Date(), "");
                                	
                                	doc = new UserDocument(); 
                                	doc.setAddressBook(po.getAddressNumber());
                                	doc.setDocumentNumber(documentNumber);
                                	doc.setDocumentType(documentType);
                                	doc.setContent(file.getFileTwo().getBytes());
                                	doc.setType(cn.getTipoComprobante());
                                	doc.setName(file.getFileTwo().getOriginalFilename());
                                	doc.setSize(file.getFileTwo().getSize());
                                	doc.setStatus(true);
                                	doc.setAccept(true);
                                	doc.setFiscalType(cn.getTipoComprobante());
                                	doc.setType(cn.getTipoComprobante());
                                	doc.setFolio(cn.getFolio());
                                	doc.setSerie(cn.getSerie());
                                	doc.setUuid(cn.getUuid());
                                	doc.setUploadDate(new Date());
                                	doc.setFiscalRef(0);
                                	documentsService.save(doc, new Date(), "");
                                	
                                	po.setInvoiceUuid(inv.getUuid());
                                	po.setCreditNotUuid(cn.getUuid());
                                	po.setStatus(AppConstants.STATUS_LOADINV);
                                	purchaseOrderService.updateOrders(po);
                                	
                                	json.put("success", false);
                            		json.put("message", "Los archivos han sido cargados exitosamente");
                                	return json.toString();
                    			}
                			}
                			

                            	
                		}else {
                			json.put("success", false);
                    		json.put("message", res);
                        	return json.toString();
                		}

	
            	}else {
            		json.put("success", false);
            		json.put("message", "La orden de compra no existe para ese proveedor");
            	}
            	

            	
            }else {
            	json.put("success", false);
                json.put("message", "Uno de los archivos no corresponde al formato de factura electrónica");
            }
        }else {
        	json.put("success", false);
            json.put("message", "Los archivos deben estar en formato .xml");
        }
        
        return json.toString();
    	
    }
    
    
    @RequestMapping(value = "/uploadForeignAdditional.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadForeignAdditional(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  int documentNumber, 
											  String documentType,
											  String voucherType,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
    	
    	try {
            if (result.hasErrors()){
                for(ObjectError error : result.getAllErrors()){
                    System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
                }
     
                json.put("success", false);
                json.put("message", "Error_1");
            }
            
            String ct = uploadItem.getFile().getContentType();
        	PurchaseOrder po = purchaseOrderService.searchbyOrderAndAddressBook(documentNumber, addressBook, documentType);
        	UserDocument doc = new UserDocument(); 
        	
        	if(po != null) {
        		
        		String invPrefix = "FACT_FOR_";
        		
        		if(AppConstants.NC_FIELD.equals(voucherType)) {
        			invPrefix = "NC_FOR_";
        		}
        		
            	doc.setAddressBook(po.getAddressNumber());
            	doc.setDocumentNumber(documentNumber);
            	doc.setDocumentType(documentType);
            	doc.setContent(uploadItem.getFile().getBytes());
            	doc.setType(ct.trim());
            	doc.setName(invPrefix + uploadItem.getFile().getOriginalFilename());
            	doc.setSize(uploadItem.getFile().getSize());
            	doc.setStatus(true);
            	doc.setAccept(true);
            	doc.setFiscalType("Otros");
            	doc.setUuid("");
            	doc.setUploadDate(new Date());
            	doc.setFiscalRef(0);
            	documentsService.save(doc, new Date(), "");     
            	
            	dataAuditService.saveDataAudit("UploadForeign",po.getAddressNumber(), currentDate, request.getRemoteAddr(),
                userAuth, invPrefix + uploadItem.getFile().getOriginalFilename(), "uploadForeignAdditional", 
                "Uploaded Foreign Invoice Successful",documentNumber+"", po.getOrderNumber()+"", null, 
                null, AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);                    	
            	
                json.put("success", true);
                json.put("message", "Success");
                json.put("orderNumber", documentNumber);
                json.put("orderType", documentType);
                json.put("addressNumber", addressBook);
        	}else {
                json.put("success", false);
                json.put("message", "Error_7");
        	}
        	
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
            json.put("success", false);
            json.put("message", "Error_1");
		}
        return json.toString();

    }
    
    @RequestMapping(value = "/uploadForeignAdditionalFD.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadForeignAdditionalFD(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook,
    										  String company,
    										  String currentUuid,
    										  String invoiceNumber,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
    	

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            } 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        } else {
            String uuid = "";
            if("".equals(currentUuid)) {
            	uuid = getUuidNoOC(addressBook, company);
            } else {
            	uuid = currentUuid;
            }
            
            String ct = uploadItem.getFile().getContentType();        
            UserDocument doc = new UserDocument(); 
        	doc.setAddressBook(addressBook);
        	doc.setDocumentNumber(0);
        	doc.setDocumentType("Honorarios");
        	doc.setContent(uploadItem.getFile().getBytes());
        	doc.setType(ct.trim());
        	doc.setName(uploadItem.getFile().getOriginalFilename());
        	doc.setSize(uploadItem.getFile().getSize());
        	doc.setStatus(true);
        	doc.setAccept(true);
        	doc.setFiscalType("Factura");
        	doc.setUuid(uuid);
        	doc.setFolio(invoiceNumber);
        	doc.setSerie("");
        	doc.setUploadDate(new Date());
        	doc.setFiscalRef(0);
        	doc.setDescription("MainUUID_".concat(uuid));
        	documentsService.save(doc, new Date(), "");    
        	
         	dataAuditService.saveDataAudit("UploadForeignAdditionalFD",addressBook, currentDate, request.getRemoteAddr(),
            userAuth, uploadItem.getFile().getOriginalFilename(), "uploadForeignAdditionalFD", 
            "Uploaded Foreign Invoice FD Successful",null, null, null, 
            uuid, AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);   
        	
            json.put("success", true);
            json.put("message", "El archivo ha sido cargado de forma exitosa");
            json.put("addressNumber", addressBook);
            json.put("uuid", uuid);        	
        }

        return json.toString();
    }
       
    
    public Map<String,Object> mapOK(String obj){
    	Map<String,Object> modelMap = new HashMap<String,Object>(2);
    	modelMap.put("message", obj);
    	modelMap.put("success", true);
    	return modelMap;
    }
 
    public Map<String,Object> mapError(String msg){
    	Map<String,Object> modelMap = new HashMap<String,Object>(2);
    	modelMap.put("message", msg);
    	modelMap.put("success", false);
    	return modelMap;
    }
    
	public static String takeOffBOM(InputStream inputStream) throws IOException {
	    BOMInputStream bomInputStream = new BOMInputStream(inputStream);
	    return IOUtils.toString(bomInputStream, "UTF-8");
	}
	
	private String getTimeSuffix() {
		StringBuilder str = new StringBuilder();
		str.append(Calendar.getInstance().get(Calendar.YEAR));
		str.append("-");
		String month = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.MONTH ) + 1),2,"0");
		String day = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH )),2,"0");
		str.append(month);
		str.append(day);
		str.append("-");
		String hour = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY )),2,"0");
		String minute = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE )),2,"0");
		str.append(hour);
		str.append(minute);
		return str.toString();
	}
	
	private String getUuidNoOC(String addressNumber, String company) {
		StringBuilder str = new StringBuilder();
		String supNbr = org.apache.commons.lang.StringUtils.leftPad(addressNumber,8,"0");
		str.append(supNbr);
		str.append("-");
		str.append(Calendar.getInstance().get(Calendar.YEAR));
		str.append("-");
		String month = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.MONTH ) + 1),2,"0");
		String day = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH )),2,"0");
		str.append(month);
		str.append(day);
		str.append("-");
		String hour = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY )),2,"0");
		String minute = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.MINUTE )),2,"0");
		str.append(hour);
		str.append(minute);
		str.append("-");
		String second = org.apache.commons.lang.StringUtils.leftPad(String.valueOf(Calendar.getInstance().get(Calendar.SECOND)),2,"0");
		str.append(second);
		str.append(org.apache.commons.lang.StringUtils.leftPad(company,6,"0"));
		str.append("NOOC");
		return str.toString();
	}
	
	public boolean isValidExtension(String filename) {
		boolean isValid = false;
		int ext = filename.lastIndexOf(".");
		if (ext >= 0) {
			for (String compare : ALLOWED_FORMAT) {
				String currentExtension = filename.substring(ext + 1);
				if (currentExtension.equalsIgnoreCase(compare)) {
					isValid = true;
				}
			}
		}
		return isValid;
	}
	
	public double currencyToDouble(String amount) {
		return Double.valueOf(amount.replace("$", "").replace(",", "").replace(" ", ""));
	}
	
	@RequestMapping(value = "/uploadInvoiceWithoutOrderPUO.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadInvoiceFromOrderWithoutOrderPUO(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  String tipoComprobante,
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	

    	
        try{

        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error al cargar el archivo");
        }
        
        InvoiceDTO inv = null;
        String ct = uploadItem.getFile().getContentType();
        
        if(uploadItem.getFileTwo() != null) {
            String ctPdf = uploadItem.getFileTwo().getContentType();
            if(!"application/pdf".equals(ctPdf)) {
            	json.put("success", false);
            	json.put("message", "El documento cargado de tipo .pdf no es vÃ¡lido");
            	return json.toString();
            }
        }

        
        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
            if("text/xml".equals(ct.trim())){
                inv = documentsService.getInvoiceXml(uploadItem);
                if(inv != null){
                	if(inv.getFolio()==null||inv.getFolio().equals("")){
                		inv.setFolio(inv.getUuid().substring(inv.getUuid().length() - 8));
                	}
                	
                	if(inv.getSerie()==null||inv.getSerie().equals("")){
                		inv.setSerie(inv.getUuid().substring(inv.getUuid().length() - 8));
                	}
                	
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no es de tipo FACTURA<br />(Tipo Comprobante = I" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.NC_FIELD.equals(tipoComprobante) && !"E".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no coresponde a una NOTA DE CREDITO<br />(Tipo Comprobante = E" + ")");
                    	return json.toString();
                	}
                	
                	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante) && !"P".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "El documento cargado no corresponde a un COMPLEMENTO DE PAGO <br />(Tipo Comprobante = P" + ")");
                    	return json.toString();
                	}
                	
                	
                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
                    String xmlContent = IOUtils.toString(stream, "UTF-8");
                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
        			String xmlString = source.replace("?<?xml", "<?xml");
                    
                	String res = fiscalDocumentService.validateInvoiceFromOrderPUO(inv,
                														   addressBook, 
																		   tipoComprobante,
																		   true,
																		   xmlString,
																		   "");
                	if("".equals(res) || res.contains("DOC:")){
                		res = fiscalDocumentService.saveInvoiceWitoutOC(uploadItem,inv,
								   addressBook, 
								   tipoComprobante,
								   true,
								   xmlString,
								   "");
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}
                	
                	
                	if("".equals(res)){
                		
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("orderNumber", 0);
        	            json.put("orderType", "I");
        	            json.put("addressNumber", addressBook);
        	            json.put("docNbr", res);
        	            json.put("uuid", inv.getUuid());
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}

                }else{
                	json.put("success", false);
                	json.put("message", "El archivo no es aceptado.  <br />NO ha pasado la fase de verificación y tampoco sera cargado a la solicitud.");
                }
                
            }else{
            	json.put("success", false);
            	json.put("message", "Para cargas de archivos fiscales, sólo se permiten archivos .xml");
            }
        }
        
        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();

    }	
	
	@RequestMapping(value = "/uploadInvoiceWithoutReceipt.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadInvoiceWithoutReceipt(FileUploadBean uploadItem, 
    										  BindingResult result, 
    										  String addressBook, 
											  int documentNumber, 
											  String documentType,
											  String tipoComprobante,
											  String companyKey,
								    		  HttpServletResponse response){
 
	 
	 
	 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 			
		String usr = auth.getName();
        try{

      
        
        if (result.hasErrors()){
            for(ObjectError error : result.getAllErrors()){
                System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
            }
 
            json.put("success", false);
            json.put("message", "Error_1");
        }
        
        InvoiceDTO inv = null;
        String ct = uploadItem.getFile().getContentType();
        
        if(uploadItem.getFileTwo() != null) {
            String ctPdf = uploadItem.getFileTwo().getContentType();
            if(!"application/pdf".equals(ctPdf)) {
            	json.put("success", false);
            	json.put("message", "Error_2");
            	return json.toString();
            }
        }
        

        if(!AppConstants.OTHER_FIELD.equals(tipoComprobante)){
            if("text/xml".equals(ct.trim())){
                inv = documentsService.getInvoiceXml(uploadItem);
                if(inv != null){
                	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante) && !"I".equals(inv.getTipoComprobante())){
                		json.put("success", false);
                    	json.put("message", "Error_3");
                    	return json.toString();
                	}
                	                	
                    ByteArrayInputStream stream = new  ByteArrayInputStream(uploadItem.getFile().getBytes());
                    String xmlContent = IOUtils.toString(stream, "UTF-8");
                    String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
        			String xmlString = source.replace("?<?xml", "<?xml");
                    DataAudit dataAudit = new DataAudit();
                    Date currentDate = new Date();
                    
        			//JSAAVEDRA: Validación Códigos SAT
        			Supplier s = supplierService.searchByAddressNumber(addressBook);
        			if(s != null && s.isOutSourcing()) {
        				String rOs = outSourcingService.validateInvoiceCodes(inv.getConcepto());
        				if(!"".equals(rOs)) {
	                		json.put("success", false);
	                    	json.put("message", rOs);
	                    	return json.toString();
        				}
        			}
        			String orderCompany = companyKey;
        			PurchaseOrder po = purchaseOrderService.searchbyOrderAndAddressBookAndCompany(documentNumber, addressBook, documentType, orderCompany);
        			String res = documentsService.validateInvoiceWithoutReceipt(inv,
                														   addressBook, 
																		   documentNumber, 
																		   documentType,
																		   tipoComprobante,
																		   po,
																		   true,
																		   xmlString);
                	
                	
        			/*if(res.contains("DOC:")){
                    	UserDocument doc = new UserDocument(); 
                    	doc.setAddressBook(po.getAddressNumber());
                    	doc.setDocumentNumber(documentNumber);
                    	doc.setDocumentType(documentType);
                    	doc.setContent(uploadItem.getFile().getBytes());
                    	doc.setType(ct.trim());
                    	String fileName = uploadItem.getFile().getOriginalFilename();
                 	    fileName = fileName.replace(" ", "_");
                 	    doc.setName(fileName);
                    	//doc.setName("FAC_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + "_" + getTimeSuffix() + ".xml");
                    	doc.setSize(uploadItem.getFile().getSize());
                    	doc.setStatus(true);
                    	doc.setAccept(true);
                    	doc.setFiscalType(tipoComprobante);
                    	doc.setType("text/xml");
                    	doc.setFolio(inv.getFolio());
                    	doc.setSerie(inv.getSerie());
                    	doc.setUuid(inv.getUuid());
                    	doc.setUploadDate(new Date());
                    	doc.setFiscalRef(0);
                    	documentsService.save(doc, new Date(), "");
                    	
                    	dataAudit.setAction("UploadInvoice");
                    	dataAudit.setAddressNumber(po.getAddressNumber());
                    	dataAudit.setCreationDate(currentDate);
                    	dataAudit.setDocumentNumber(documentNumber+"");
                    	dataAudit.setIp(request.getRemoteAddr());
                    	dataAudit.setMessage("Uploaded Invoice Successful");
                    	dataAudit.setMethod("uploadInvoiceWithoutReceipt");
                    	dataAudit.setModule(AppConstants.SALESORDER_MODULE);
                    	dataAudit.setNotes(null);
                    	dataAudit.setOrderNumber(po.getOrderNumber()+"");
                    	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
                    	dataAudit.setStep(null);
                    	dataAudit.setUser(usr);
                    	dataAudit.setUuid(inv.getUuid());
                    	
                    	dataAuditService.save(dataAudit);
                    	
                    	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){
                    		
                    		doc = new UserDocument(); 
                        	doc.setAddressBook(po.getAddressNumber());
                        	doc.setDocumentNumber(documentNumber);
                        	doc.setDocumentType(documentType);
                        	doc.setContent(uploadItem.getFileTwo().getBytes());
                        	doc.setType(uploadItem.getFileTwo().getContentType().trim());
                        	fileName = uploadItem.getFileTwo().getOriginalFilename();
                        	fileName = fileName.replace(" ", "_");
                        	doc.setName(fileName);
                        	//doc.setName("FAC_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + "_" + getTimeSuffix() + ".pdf");
                        	doc.setSize(uploadItem.getFileTwo().getSize());
                        	doc.setStatus(true);
                        	doc.setAccept(true);
                        	doc.setFiscalType(tipoComprobante);
                        	doc.setType("application/pdf");
                        	doc.setFolio(inv.getFolio());
                        	doc.setSerie(inv.getSerie());
                        	doc.setUuid(inv.getUuid());
                        	doc.setUploadDate(new Date());
                        	doc.setFiscalRef(0);
                        	documentsService.save(doc, new Date(), "");
                        	
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
                				documentsService.save(doc, new Date(), "");	
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
                				documentsService.save(doc, new Date(), "");
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
                				documentsService.save(doc, new Date(), "");
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
                				documentsService.save(doc, new Date(), "");
                			}
    	                	           	
                        	po.setInvoiceUuid(inv.getUuid());
                    		po.setInvoiceNumber(inv.getFolio() + "");
                    		po.setPaymentType(inv.getMetodoPago());
                    		po.setStatus(AppConstants.STATUS_OC_INVOICED);
                    		po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
                    	}   	      
                    	
                    	
                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)){
                    		po.setPaymentUuid(inv.getUuid());
                    		po.setStatus(AppConstants.STATUS_LOADNC);
                    	}
                    	
                    	if(AppConstants.PAYMENT_FIELD.equals(tipoComprobante)){
                    		po.setCreditNotUuid(inv.getUuid());
                    		po.setStatus(AppConstants.STATUS_LOADCP);
                    	}
                    	
                    	
                    	purchaseOrderService.updateOrders(po);
                    	
        	            json.put("success", true);
        	            json.put("message", inv.getMessage());
        	            json.put("orderNumber", documentNumber);
        	            json.put("orderType", documentType);
        	            json.put("addressNumber", addressBook);
        	            json.put("docNbr", res);
        	            json.put("uuid", inv.getUuid());
                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}*/
                	
                	if("".equals(res) || res.contains("DOC:")){
                		
                		res = documentsService.saveInvoiceFromOrder(uploadItem, po, "", inv, tipoComprobante);                    		
                		if("".equals(res)){
	                    	dataAudit.setAction("UploadInvoice");
	                    	dataAudit.setAddressNumber(po.getAddressNumber());
	                    	dataAudit.setCreationDate(currentDate);
	                    	dataAudit.setDocumentNumber(documentNumber+"");
	                    	dataAudit.setIp(request.getRemoteAddr());
	                    	dataAudit.setMessage("Uploaded Invoice Successful");
	                    	dataAudit.setMethod("uploadInvoiceFromReceipt");
	                    	dataAudit.setModule(AppConstants.SALESORDER_MODULE);
	                    	dataAudit.setNotes(null);
	                    	dataAudit.setOrderNumber(po.getOrderNumber()+"");
	                    	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
	                    	dataAudit.setStep(null);
	                    	dataAudit.setUser(usr);
	                    	dataAudit.setUuid(inv.getUuid());	                    	
	                    	dataAuditService.save(dataAudit);
	                    	
	        	            json.put("success", true);
	        	            json.put("message", inv.getMessage());
	        	            json.put("orderNumber", documentNumber);
	        	            json.put("orderType", documentType);
	        	            json.put("addressNumber", addressBook);
	        	            json.put("docNbr", res);
	        	            json.put("uuid", inv.getUuid());
                		} else {
	                    	json.put("success", false);
	                    	json.put("message", res);
                		}

                	}else{
                    	json.put("success", false);
                    	json.put("message", res);
                	}

                }else{
                	json.put("success", false);
                	json.put("message", "Error_4");
                }
                
            }else{
            	json.put("success", false);
            	json.put("message", "Error_5");
            }
        }else{
        	
            if(uploadItem.getFile() != null) {
                String ctPdf = uploadItem.getFile().getContentType();
                if(!"application/pdf".equals(ctPdf)) {
                	json.put("success", false);
                	json.put("message", "Error_6");
                	return json.toString();
                }
            }
        	
        	PurchaseOrder po = purchaseOrderService.searchbyOrder(documentNumber, documentType);
        	UserDocument doc = new UserDocument(); 
        	
        	if(po != null) {
        		
        		String fileName = uploadItem.getFile().getOriginalFilename();
        		fileName = fileName.replaceAll(" ", "_");
        		
            	doc.setAddressBook(po.getAddressNumber());
            	doc.setDocumentNumber(documentNumber);
            	doc.setDocumentType(documentType);
            	doc.setContent(uploadItem.getFile().getBytes());
            	doc.setType(ct.trim());
            	doc.setName(fileName);
            	doc.setSize(uploadItem.getFile().getSize());
            	doc.setStatus(true);
            	doc.setAccept(true);
            	doc.setFiscalType(tipoComprobante);
            	doc.setUuid("");
            	doc.setUploadDate(new Date());
            	doc.setFiscalRef(0);
            	documentsService.save(doc, new Date(), "");
            	
                json.put("success", true);
                json.put("message", "El archivo ha sido cargado de forma exitosa");
                json.put("orderNumber", documentNumber);
                json.put("orderType", documentType);
                json.put("addressNumber", addressBook);
        	}else {
                json.put("success", false);
                json.put("message", "Error_7");
        	}

            
        }
        return json.toString();
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();

    }
	
}
