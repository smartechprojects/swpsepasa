package com.eurest.supplier.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.model.OutSourcingDocument;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.EmailService;
import com.eurest.supplier.service.OutSourcingService;
import com.eurest.supplier.service.SupplierService;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.service.XmlToPojoService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.MultiFileUploadBean;

import net.sf.json.JSONObject;


@Controller
public class OutSourcingController {
	
   	@Autowired
   	OutSourcingService outSourcingService;
   	
   	@Autowired
	DocumentsService documentsService;
   	
	@Autowired
	SupplierService supplierService;
	
	@Autowired
	UdcService udcService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	XmlToPojoService xmlToPojoService;
	
	Logger log4j = Logger.getLogger(OutSourcingController.class);
	
    @RequestMapping(value = "/rejectOutSourcingDocument.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public @ResponseBody String rejectOutSourcingDocument(@RequestParam int id, @RequestParam String notes, 
   														  @RequestParam String frequency, @RequestParam String uuid, 
   														  HttpServletResponse response){

     	
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth.getName();
		
		if("INV".equals(frequency)) {
			outSourcingService.rejectInvoice(uuid, notes, user);
		}else {
			outSourcingService.rejectDocument(id, notes, user);
		}
		json.put("success", true);
    	json.put("message", "El documento ha sido rechazado y el porveedor ha sido notificado");
    	return json.toString();    	
    }
    
    @RequestMapping(value = "/approveOutSourcingDocument.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public @ResponseBody String approveOutSourcingDocument(@RequestParam int id, @RequestParam String notes, 
												   		   @RequestParam String frequency, @RequestParam String uuid,
												   		   HttpServletResponse response){

     	
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth.getName();
		
		if("INV".equals(frequency)) {
			outSourcingService.approveInvoice(uuid, notes, user);
		}else {
			outSourcingService.approveDocument(id, notes, user);
		}
		
		json.put("success", true);
    	json.put("message", "El documento ha sido aprobado y el porveedor ha sido notificado");
    	return json.toString();    	
    }
	
	@RequestMapping(value = "/uploadOSReplacementFile.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String uploadOSReplacementFile(@ModelAttribute MultiFileUploadBean multiFileUploadBean, 
	   				    										  String addressNumber,
	   				    										  int id,
	   												    		  HttpServletResponse response){
	    	
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
   		
    	List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
    	if(uploadedFiles.size() == 1) {
	        MultipartFile file = uploadedFiles.get(0);
		   		if("".equals(outSourcingService.saveReplacementFile(file, addressNumber, id))) {
		   			json.put("success", true);
		        	json.put("message", "");
		        	return json.toString();
		        	
		   		}else {
		   			json.put("success", false);
		        	json.put("message", "Ha ocurrido un error inesperado");
		        	return json.toString();
		   		}
	        
    	}else {
   			json.put("success", false);
        	json.put("message", "Ha ocurrido un error inesperado");
        	return json.toString();
   		}

    }
	
    @RequestMapping(value = "/uploadMonthlyFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public String uploadMonthlyFilesFiles(Model model, @ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    										  String addressNumber,
   				    										  String month, 
 				    										  String year,
   												    		  HttpServletResponse response){
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
   		String res = outSourcingService.saveMonthlyDocs(multiFileUploadBean, addressNumber, month, year);
   		
   		if("".equals(res)) {
   			model.addAttribute("message", "_SUCCESSMONTH");
   			return "outsourcingFiles";
   		}else {
   			//model.addAttribute("message", "_ERROR");
   			model.addAttribute("message", Base64.getEncoder().encodeToString(res.getBytes()));
   			return "redirect:home.action";
   		}
   		//return "login";
    }
    
    @RequestMapping(value = "/uploadBimonthlyFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public String uploadBimonthlyFilesFiles(Model model, @ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    										  String addressNumber,
   												    		  HttpServletResponse response){
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
   		
   		if("".equals(outSourcingService.saveBimonthlyDocs(multiFileUploadBean, addressNumber))) {
   			model.addAttribute("message", "_SUCCESSSECOND");
   		}else {
   			model.addAttribute("message", "_ERROR");
   		}
   		return "login";
    }
    
    @RequestMapping(value = "/uploadQuarterlyFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public String uploadQuarterlyFilesFiles(Model model, @ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    										  String addressNumber,
   												    		  HttpServletResponse response){
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
   		
   		if("".equals(outSourcingService.saveQuarterlyDocs(multiFileUploadBean, addressNumber))) {
   			model.addAttribute("message", "_SUCCESSQUARTER");
   		}else {
   			model.addAttribute("message", "_ERROR");
   		}
   		return "login";
    }
    
    @RequestMapping(value = "/uploadBaseLineFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public String uploadBaseLineFiles(Model model, 
   									  @ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    				  String addressNumber,
   				    				  String effectiveDate,
   				    				  HttpServletResponse response){
    	
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
   		
   		if("".equals(outSourcingService.saveBaseLineDocument(multiFileUploadBean, effectiveDate, addressNumber))){
   			model.addAttribute("message", "_SUCCESS");
   		}else {
   			model.addAttribute("message", "_ERROR");
   		}
   		return "login";
    }
	
    @RequestMapping(value = "/uploadOSInvoiceFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public @ResponseBody String uploadOSInvoiceFiles(@ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    										  String addressNumber,
   				    										  int orderNumber,
   				    										  String orderType,
   												    		  HttpServletResponse response){
    	
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
    	JSONObject json = new JSONObject();
   		
   		if("".equals(outSourcingService.saveInvoiceDocs(multiFileUploadBean, addressNumber, orderNumber, orderType))) {
   			json.put("success", true);
        	json.put("message", "");
        	return json.toString();
   		}else {
   			json.put("success", true);
        	json.put("message", "Ha ocurrido un error inesperado");
        	return json.toString();
   		}
    }
    
    @RequestMapping(value = "/supplier/searchOSDocuments.action", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> searchOSDocuments(@RequestParam int start,
															   @RequestParam int limit,
															   @RequestParam String status,
															   @RequestParam String supplierName,
															   @RequestParam String supplierNumber,
															   @RequestParam String documentType,
															   //@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
															   //@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
															   @RequestParam int periodMonth,
															   @RequestParam int periodYear,
															   @RequestParam String roleType,
															   HttpServletRequest request){	
    	
    	try{
			List<OutSourcingDocument> list=null;
			int total=0;
			List<String> docTypeList = new ArrayList<String>();
			
			int monthLoad = 0;
			int yearLoad = 0;
			
			monthLoad = periodMonth;
			yearLoad = periodYear;
			
			if(!"".equals(roleType)) {
				List<UDC> udcList = udcService.searchBySystem("OSDOCUMENT");
				if(udcList != null) {
					for(UDC udc : udcList) {
						if(roleType.equals(udc.getStrValue2())) {
							docTypeList.add(udc.getUdcKey());
						}
					}
				}				
			}
					    
			list = outSourcingService.searchDocsByQuery(supplierName, status, documentType, supplierNumber, start, limit, monthLoad, yearLoad);
			if(list != null) {
				total = outSourcingService.searchDocsByQueryCount(supplierName, status, documentType, supplierNumber, monthLoad, yearLoad);
			}
			return mapOKList(list, total);
		    
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
   	
    }
    
    @RequestMapping(value ="/supplier/openOSDocument.action", method = RequestMethod.GET)
    public void downloadFile(HttpServletResponse response, 
    			             @RequestParam int id) throws IOException {
     
    	OutSourcingDocument doc = outSourcingService.getDocumentById(id);
		String fileName = doc.getName();
		String contentType = doc.getType();
		response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length", String.valueOf(doc.getContent().length));
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        InputStream is = new ByteArrayInputStream(doc.getContent());
        byte[] bytes = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(bytes)) != -1) {
            response.getOutputStream().write(bytes, 0, bytesRead);
        }
        is.close();
    }
    
    @RequestMapping(value = "/uploadOutSoucingSupplierFiles.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
   	public String uploadOutSoucingSupplierFiles(Model model, @ModelAttribute MultiFileUploadBean multiFileUploadBean, 
   				    										  String addressNumber,
   												    		  HttpServletResponse response){
   		response.setContentType("text/html");
   		response.setCharacterEncoding("UTF-8");
   		
   		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
   		
   		try {
   			if(uploadedFiles.size() > 1) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				
   				UserDocument doc = new UserDocument();
   				doc.setAddressBook(addressNumber);
   		    	doc.setDocumentNumber(0);
   		    	doc.setDocumentType("OUTSRC");
   		    	doc.setContent(uploadedFiles.get(0).getBytes());
   		    	doc.setType(uploadedFiles.get(0).getContentType());
   		    	doc.setName("STPS_OUTSRC_" + uploadedFiles.get(0).getOriginalFilename());
   		    	doc.setSize(uploadedFiles.get(0).getSize());
   		    	doc.setStatus(true);
   		    	doc.setAccept(true);
   		    	doc.setFiscalType("Otros");
   		    	doc.setFolio("");
   		    	doc.setSerie("");
   		    	doc.setUuid("");
   		    	doc.setUploadDate(new Date());
   		    	doc.setFiscalRef(0);
   		    	documentsService.save(doc, new Date(), "");
   		    	
   		    	doc = new UserDocument();
   				doc.setAddressBook(addressNumber);
   		    	doc.setDocumentNumber(0);
   		    	doc.setDocumentType("OUTSRC");
   		    	doc.setContent(uploadedFiles.get(1).getBytes());
   		    	doc.setType(uploadedFiles.get(1).getContentType());
   		    	doc.setName("IMSS_OUTSRC_" + uploadedFiles.get(1).getOriginalFilename());
   		    	doc.setSize(uploadedFiles.get(1).getSize());
   		    	doc.setStatus(true);
   		    	doc.setAccept(true);
   		    	doc.setFiscalType("Otros");
   		    	doc.setFolio("");
   		    	doc.setSerie("");
   		    	doc.setUuid("");
   		    	doc.setUploadDate(new Date());
   		    	doc.setFiscalRef(0);
   		    	documentsService.save(doc, new Date(), "");
   			
   		    	UDC udc = udcService.searchBySystemAndKey("OUTSOURCING", "EMAIL");
   		    	if(udc != null){
   		    		String outSrcApprovalEmail = udc.getStrValue1();
   		    		String msg = AppConstants.OUTSOURCING_APPROVAL_MESSAGE;
   		    		msg = msg.replace("_SUPPLIER_", s.getRazonSocial());
   		    		msg = msg.replace("_LINK_", AppConstants.EMAIL_PORTAL_LINK_PUBLIC + "/approveOutsourcingRequest?ab=" + s.getAddresNumber() + "&token=" + com.eurest.supplier.util.StringUtils.randomString(12));
   			    	emailService.sendEmailAttachMultiPart(AppConstants.OUTSOURCING_APPROVAL_SUBJECT.replace("_SUPPLIER_", s.getRazonSocial()), msg, outSrcApprovalEmail, multiFileUploadBean, 2);
   		    	}

   		    	model.addAttribute("message", "_SUCCESS");
   				model.addAttribute("userName", s.getRazonSocial());
   				model.addAttribute("name", s.getRazonSocial());
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			model.addAttribute("message", "_ERROR");
   		}
   		
   		return "outsourcingFiles";
    }
    
	@RequestMapping(value = "/uploadOutSourcingInvoiceDocuments.action", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String uploadOutSourcingInvoiceDocuments(@ModelAttribute MultiFileUploadBean multiFileUploadBean, 
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
    	
    	try {
	    	List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();

    		String ctXml = uploadedFiles.get(0).getContentType();
    		if(!"text/xml".equals(ctXml)) {
            	json.put("success", false);
            	json.put("message", "El archivo .xml no es válido");
            	return json.toString();
            }
	    	
    		String ctPdf = uploadedFiles.get(1).getContentType();
    		if(!"application/pdf".equals(ctPdf)) {
            	json.put("success", false);
            	json.put("message", "El archivo .pdf no es válido");
            	return json.toString();
            }
    		
    		String ctZip = uploadedFiles.get(2).getContentType();
    		if(!"application/x-zip-compressed".equals(ctZip) && !"application/zip".equals(ctZip)) {
            	json.put("success", false);
            	json.put("message", "El archivo .zip no es válido: " + ctZip);
            	return json.toString();
            }
    		
    		ByteArrayInputStream stream = new  ByteArrayInputStream(uploadedFiles.get(0).getBytes());
			String xmlContent = IOUtils.toString(stream, "UTF-8");
            String source = takeOffBOM(IOUtils.toInputStream(xmlContent, "UTF-8"));
			String xmlString = source.replace("?<?xml", "<?xml");			
			InvoiceDTO inv = null;
			
			if(xmlString.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				inv = xmlToPojoService.convertV4(xmlString);
			} else {
				inv = xmlToPojoService.convert(xmlString);
			}
			
			String res = outSourcingService.validateInvoiceFromOrder(inv, addressBook, documentNumber, documentType, 
					                                                 tipoComprobante, receiptIdList, xmlString, 
					                                                 uploadedFiles, orderCompany);
			
			if(!"".equals(res)) {
		    	json.put("success", false);
	            json.put("message", res);
	            return json.toString();
			}
			

	    	json.put("success", true);
            json.put("message", "Success");
            
    	} catch (Exception e) {
    		log4j.error("Exception" , e);
			e.printStackTrace();
            json.put("success", false);
            json.put("message", "Error_1");
		}
        return json.toString();
    	
    }
    
	
	public Map<String, Object> mapOK(OutSourcingDocument obj) {
		Map<String, Object> modelMap = new HashMap<String, Object>(3);
		modelMap.put("total", 1);
		modelMap.put("data", obj);
		modelMap.put("success", true);
		return modelMap;
	}

	
	public Map<String, Object> mapOKList(List<OutSourcingDocument> list, int total) {
		Map<String, Object> modelMap = new HashMap<String, Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
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
	
}

