package com.eurest.supplier.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.LogDataJEdwars;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.service.DataAuditService;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.FiscalDocumentService;
import com.eurest.supplier.service.JDERestService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.FileConceptUploadBean;
import com.eurest.supplier.util.FileUploadBean;
import com.eurest.supplier.util.LoggerJEdwars;
import com.eurest.supplier.util.StringUtils;

import net.sf.json.JSONObject;


@Controller
public class FiscalDocumentsController {
	
	@Autowired
	private FiscalDocumentService fiscalDocumentService;
	
	@Autowired
	DocumentsService documentsService;	
	
	@Autowired
	StringUtils stringUtils;
	
	@Autowired
	private JDERestService jDERestService;
	
	@Autowired
	LoggerJEdwars loggerJEdwars;
	  
	@Autowired
	DataAuditService dataAuditService;  
	
	Logger log4j = Logger.getLogger(FiscalDocumentsController.class);
	
	@RequestMapping(value ="/fiscalDocuments/view.action")
	public @ResponseBody Map<String, Object> view(@RequestParam int start,
			  									  @RequestParam int limit,
												  @RequestParam String addressNumber,
												  @RequestParam String status,
												  @RequestParam String uuid,
												  @RequestParam String documentType,
												  @RequestParam String pFolio,
												  HttpServletRequest request){	
		try{
			List<FiscalDocuments> list=null;
			int total=0;
			list = fiscalDocumentService.getFiscalDocuments(addressNumber, status, uuid, documentType, start, limit,pFolio);
			total = fiscalDocumentService.getTotalRecords(addressNumber, status, uuid, documentType, start, limit,pFolio);				
		    return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unused")
	@RequestMapping(value ="/fiscalDocuments/update.action")
	public @ResponseBody Map<String, Object> update(String recordId,
													String status,
													String notes,
													HttpServletResponse response){
		try{
			
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usr = auth.getName();
			
			String msg = "";
	        int id = Integer.parseInt(recordId);	        
			if(id != 0) {				
				msg = fiscalDocumentService.updateDocument(id, status, notes);				
				if("".equals(msg)) {
					return mapStrOk("Surgió un error en la actualización.");	
				}
			}

			return mapStrOk(msg);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/uploadInvoiceWithoutOrder.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadInvoiceFromOrder(FileUploadBean uploadItem,
    										  FileConceptUploadBean uploadConcept,
    										  BindingResult result, 
    										  String addressBook,
    										  String supCompany,    										  
											  String tipoComprobante,
											  String advancePayment,
								    		  HttpServletResponse response){
		response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	DataAudit dataAudit = new DataAudit();
    	Date currentDate = new Date();
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 			
		String usr = auth.getName();

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
	                	
	                	String res = fiscalDocumentService.validateConceptInvoiceWithoutOrder(uploadConcept, inv, addressBook, tipoComprobante, supCompany);
	                	
	        			if("".equals(res)) {
	        				double dblAdvancePayment = currencyToDouble(advancePayment);
		                	res = fiscalDocumentService.validateInvoiceWithoutOrder(uploadConcept, inv, addressBook, supCompany, tipoComprobante, dblAdvancePayment);
	        			}	                	
	                	
	                	if("".equals(res) || res.contains("DOC:")){	                		
	                		UserDocument doc = new UserDocument();

	                    	if(AppConstants.INVOICE_FIELD.equals(tipoComprobante)){	                    		
	                    		//Doc XML                    		
	                        	doc.setAddressBook(addressBook);
	                        	doc.setDocumentNumber(0);
	                        	doc.setDocumentType("Honorarios");
	                        	doc.setContent(uploadItem.getFile().getBytes());
	                        	doc.setType(ct.trim());
	                        	//doc.setName(uploadItem.getFile().getOriginalFilename());
	                        	doc.setName(inv.getUuid() + ".xml");
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
	                        	doc.setDescription("MainUUID_".concat(inv.getUuid()));
	                        	documentsService.save(doc, new Date(), "");
	                        	
	                        	//Doc PDF                    		
	                    		doc = new UserDocument(); 
	                        	doc.setAddressBook(addressBook);
	                        	doc.setDocumentNumber(0);
	                        	doc.setDocumentType("Honorarios");
	                        	doc.setContent(uploadItem.getFileTwo().getBytes());
	                        	doc.setType(uploadItem.getFileTwo().getContentType().trim());
	                        	//doc.setName(uploadItem.getFileTwo().getOriginalFilename());
	                        	doc.setName(inv.getUuid() + ".pdf");
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
	                        	doc.setDescription("MainUUID_".concat(inv.getUuid()));
	                        	documentsService.save(doc, new Date(), "");	                        	
	                    	}
	                    	
	                    	if(AppConstants.NC_FIELD.equals(tipoComprobante)) {	                    		
	                    		doc.setAddressBook(addressBook);
	                        	doc.setDocumentNumber(0);
	                        	doc.setDocumentType("E");
	                        	doc.setContent(uploadItem.getFile().getBytes());
	                        	doc.setType(ct.trim());
	                        	//doc.setName(uploadItem.getFile().getOriginalFilename());
	                        	doc.setName("NC_Without_OC_" + inv.getUuid()   + ".xml");
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
	                        	doc.setDescription("MainUUID_".concat(inv.getUuid()));
	                        	documentsService.save(doc, new Date(), "");
	                    		
	                    		doc = new UserDocument(); 
	                        	doc.setAddressBook(addressBook);
	                        	doc.setDocumentNumber(0);
	                        	doc.setDocumentType("E");
	                        	doc.setContent(uploadItem.getFileTwo().getBytes());
	                        	doc.setType(uploadItem.getFileTwo().getContentType().trim());
	                        	//doc.setName(uploadItem.getFileTwo().getOriginalFilename());
	                        	doc.setName("NC_Without__OC_" + inv.getUuid()   + ".pdf");
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
	                        	doc.setDescription("MainUUID_".concat(inv.getUuid()));
	                        	documentsService.save(doc, new Date(), "");
	                    	}
	                    	
	                    	dataAudit.setAction("UploadInvoiceWithoutOrder");
	                    	dataAudit.setAddressNumber(addressBook);
	                    	dataAudit.setCreationDate(currentDate);
	                    	dataAudit.setDocumentNumber(null);
	                    	dataAudit.setIp(request.getRemoteAddr());
	                    	dataAudit.setMessage("Uploaded Invoice With Order Successful");
	                    	dataAudit.setMethod("uploadInvoiceFromOrder");
	                    	dataAudit.setModule(AppConstants.FISCALDOCUMENT_MODULE);
	                    	dataAudit.setNotes(null);
	                    	dataAudit.setOrderNumber(null);
	                    	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
	                    	dataAudit.setStep(null);
	                    	dataAudit.setUser(usr);
	                    	dataAudit.setUuid(inv.getUuid());
	                    	
	                    	dataAuditService.save(dataAudit);
	                    	
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
        	json.put("success", false);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();
    }
	
	@RequestMapping(value ="/fiscalDocuments/getComplFiscalDocsByStatus.action")
	public @ResponseBody Map<String, Object> getOrderReceiptsByStatus(
												  @RequestParam String addressBook){	
		List<FiscalDocuments> list = null;
		int total=0;
		try{
				list = fiscalDocumentService.getComplPendingInvoice(addressBook);
				total = list.size();
				return mapOK(list, total);
		        
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/reSendJedwar.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String reSendJedwar(
								    		  HttpServletResponse response){
 
    	response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	

    	
        try{
        	List<LogDataJEdwars> logs=loggerJEdwars.getLogDataToSend();
        	for (LogDataJEdwars logDataJEdwars : logs) {
        		jDERestService.sendJournalEntriesReload(logDataJEdwars);
			}
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
        return json.toString();
	}
	
	public static String takeOffBOM(InputStream inputStream) throws IOException {
	    BOMInputStream bomInputStream = new BOMInputStream(inputStream);
	    return IOUtils.toString(bomInputStream, "UTF-8");
	}

	public Map<String,Object> mapOK(List<FiscalDocuments> list, int total){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
		modelMap.put("success", true);
		return modelMap;
	}

	public Map<String,Object> mapOK(FiscalDocuments obj){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", 1);
		modelMap.put("data", obj);
		modelMap.put("success", true);
		return modelMap;
	}

	public Map<String,Object> mapError(String msg){
		Map<String,Object> modelMap = new HashMap<String,Object>(2);
		modelMap.put("message", "Ha ocurrido un error al realizar la operación. Solicite al administrador los detalles del log.");
		modelMap.put("success", false);
		return modelMap;
	} 

	public Map<String,Object> mapStrOk(String msg){
		Map<String,Object> modelMap = new HashMap<String,Object>(2);
		modelMap.put("message", msg);
		modelMap.put("success", true);
		return modelMap;
	}
	
	public double currencyToDouble(String amount) {
		return Double.valueOf(amount.replace("$", "").replace(",", "").replace(" ", ""));
	}

	}
