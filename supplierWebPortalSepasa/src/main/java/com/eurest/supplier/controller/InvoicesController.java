package com.eurest.supplier.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
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

import com.eurest.supplier.dao.DocumentsDao;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.dto.ReportInvoicesDTO;
import com.eurest.supplier.model.OutSourcingDocument;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.EmailService;
import com.eurest.supplier.service.ExcelService;
import com.eurest.supplier.service.InvoiceService;
import com.eurest.supplier.service.OutSourcingService;
import com.eurest.supplier.service.SupplierService;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.service.XmlToPojoService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.MultiFileUploadBean;

import net.sf.json.JSONObject;


@Controller
public class InvoicesController {
	
	@Autowired
	DocumentsDao documentsDao;
	
   	@Autowired
   	InvoiceService invoiceService;
   	
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
	
	@Autowired
	ExcelService excelService;
	
	private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(InvoicesController.class);
	
    
    @RequestMapping(value = "/invoice/searchReportDocument.action", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> searchOSDocuments(@RequestParam int start,
															   @RequestParam int limit,
															   @RequestParam String supplierNumber,
															   @RequestParam String uuid,
															   @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date poFromDate,
															   @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date poToDate,															   
															   @RequestParam String pFolio,
															   @RequestParam String module,
															   HttpServletRequest request){	
    	
    	
    	try{
			List<ReportInvoicesDTO> list=null;
			int total=0;						
			list = invoiceService.searchDocsByQuery(uuid, supplierNumber, start, limit, poFromDate, poToDate, pFolio,module); // status documentType monthLoad, yearLoad
			if(list != null) {
				total = invoiceService.searchDocsByQueryCount(uuid, supplierNumber, poFromDate, poToDate, pFolio,module); // status documentType monthLoad, yearLoad
			}
			return mapOKList(list, total);
		    
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
   	
    }
    
    @RequestMapping(value ="/downloadAllsInvoicesZip.action", method = RequestMethod.POST,produces="text/plain;charset=UTF-8")
	public @ResponseBody String downloadAllsInvoicesZip(HttpServletResponse response,
														  @RequestParam String supplierNumber,
														  @RequestParam String uuid,
														  @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date poFromDate,
														  @RequestParam (required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Date poToDate,														   														   
														  @RequestParam String pFolio,
														  @RequestParam String module
														  ){

    	try {
		List<UserDocument> filesIDs = new ArrayList<UserDocument>();   		
		UserDocument file = null;
				
		List<ReportInvoicesDTO> list=null;
		list = invoiceService.searchDocsByQuery(uuid, supplierNumber, 0, 1000, poFromDate, poToDate, pFolio, module);
				
		if(list != null) {
			for(ReportInvoicesDTO ud : list){
				file = documentsDao.searchInvXMLByUuidOnly(ud.getUuid());	
				if(file != null) {
						filesIDs.add(file);
				}
								
			}
		}
		
		//filesIDs = documentsService.searchInvByQueryForDownload(supplierNumber, uuid, poFromDate, poToDate, pFolio);		
				
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zipOut = new ZipOutputStream(baos);
	
			long totalLenght = 0;
	
			int count = 1;
			
			for (UserDocument fdoc : filesIDs) {					
					String fileName = fdoc.getName();					
					fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "");
					
					ZipEntry entry = new ZipEntry(count + "_" + fileName);
					zipOut.putNextEntry(entry);
					
					InputStream is = new ByteArrayInputStream(fdoc.getContent());
					byte[] bytes = new byte[1024];
					int bytesRead;
					while ((bytesRead = is.read(bytes)) != -1) {
						zipOut.write(bytes, 0, bytesRead);
					}
					is.close();
					zipOut.flush();
	
					count++;
					totalLenght = totalLenght + fdoc.getSize();
				
	
				
					
			}
			zipOut.close();
	
			byte[] bytesRead = baos.toByteArray();
			String encodedBase64 = new String(Base64.getEncoder().encodeToString(bytesRead));
	
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			return encodedBase64;
	        }catch(Exception ex) {
	    	   log4j.error("Exception" , ex);
	    	   ex.printStackTrace();
		       return ex.toString();
		    }
  }

    

	
	public Map<String, Object> mapOKList(List<ReportInvoicesDTO> list, int total) {
		Map<String, Object> modelMap = new HashMap<String, Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
		modelMap.put("success", true);
		return modelMap;
	}
	
	public Map<String, Object> mapOKListDTO(List<ReportInvoicesDTO> list, int total) {
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
	
	private String getFirstTimeDate(String date) {
		return date.substring(0, 10).concat(" 00:00:00");
	}
	
	private String getLastTimeDate(String date) {
		return date.substring(0, 10).concat(" 23:59:59");
	}
	
}

