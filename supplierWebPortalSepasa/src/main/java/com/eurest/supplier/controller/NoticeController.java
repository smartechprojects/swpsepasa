package com.eurest.supplier.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eurest.supplier.dto.NoticeDTO;
import com.eurest.supplier.dto.NoticeDetailDTO;
import com.eurest.supplier.model.Notice;
import com.eurest.supplier.model.NoticeDetail;
import com.eurest.supplier.model.NoticeDocument;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.NoticeService;
import com.eurest.supplier.util.FileUploadBean;

import net.sf.json.JSONObject;



@Controller
public class NoticeController {
	
  @Autowired
  NoticeService noticeService;
  
  @Autowired
  DocumentsService documentsService;
  
  private static final long MAX_SIZE = 26214400; //MAX SIZE 25MB
  private static final List<String> ALLOWED_FORMAT_NOTICE = Arrays.asList(new String[]{"pdf"});
  private static final List<String> ALLOWED_FORMAT_NOTICE_LAYOUT = Arrays.asList(new String[]{"xlsx"});
  
  @RequestMapping(value ="/notice/view.action")
	public @ResponseBody Map<String, Object> view(@RequestParam int start,
												  @RequestParam int limit,
												  @RequestParam String query,
												  HttpServletRequest request){	
		List<Notice> list=null;
		int total=0;
		try{
			if("".equals(query)){
				list = noticeService.getNoticesList(start, limit);
				total = noticeService.getTotalRecords();
			}else{
				list = noticeService.searchCriteria(query);
				total = list.size();
			}
			/*
			for(Users u : list) {
				String pass = u.getPassword().substring(6);
				byte [] barr = Base64.getDecoder().decode(pass); 
				u.setPassword(new String(barr));
			}*/
			
		    return mapOK(list, total);
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
		
	}

	@RequestMapping(value = "/notice/save.action", method = RequestMethod.POST)
	  @ResponseBody
	  public Map<String, Object> save( String idNotice, String createdBy,String noticeTitle,
			  							String frequency, String[] filters, String[] suppliersNotice,
			  							Date noticeFromDate, Date noticeToDate, Boolean required,
			  							Boolean suppLayout, Boolean emailNotif, Boolean docSupplier, 
			  							Boolean enabled,String noticeFile, FileUploadBean uploadItem){
		try{
			String msg = "";
			
			if(suppLayout != null) {
				if(suppLayout==true) {
					List<String> suppExcel = null;
					if(uploadItem.getFile().getSize()>0 ) {
						/*if (result.hasErrors()){
							for(ObjectError error : result.getAllErrors()){
							System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
							}
							
							return mapStrOk("Error al cargar el archivo");
						}*/
							
						if(isValidExtensionLayout(uploadItem.getFile().getOriginalFilename().trim())){
							suppExcel = noticeService.exportExcelSupplier(uploadItem);
							
							org.json.JSONObject suppError = noticeService.validateExcelSupplier(suppExcel);
							
							//String[] suppError =  noticeService.validateExcelSupplier(suppExcel);
							
							if(suppError.getBoolean("success")) {
								return mapStrOk("Error - Los siguientes proveedores no se encuentran en el portal: " + suppError.getString("suppliers"));
							}
							
							suppliersNotice = new String[suppExcel.size()];
							
							for (int i=0;i<suppExcel.size();i++) {
								suppliersNotice[i] =suppExcel.get(i);
							}
							
							//if(msg )return mapStrOk(msg);
						
						}else{
							return mapStrOk("Error - Se permiten solamente archivos de tipo: " + String.join(", ", ALLOWED_FORMAT_NOTICE_LAYOUT));
						}
					}
				}
			}
			
			Notice obj = new Notice();
			
			obj.setIdNotice(idNotice);
			obj.setCreatedBy(createdBy);
			obj.setNoticeTitle(noticeTitle);
			obj.setFrequency(frequency);
			obj.setFilters(filters);
			obj.setSuppliersNotice(suppliersNotice);
			obj.setNoticeFromDate(noticeFromDate);
			obj.setNoticeToDate(noticeToDate);
			obj.setEnabled(enabled);
			obj.setNoticeFile(noticeFile);
			
			if(required == null) {
				obj.setRequired(false);
			}else obj.setRequired(required);
			
			if(docSupplier == null) {
				obj.setDocSupplier(false);
			}else obj.setDocSupplier(docSupplier);
			
			if(emailNotif == null) {
				obj.setEmailNotif(false);
			}else obj.setEmailNotif(emailNotif);
			
			msg = noticeService.save(obj);
			return mapStrOk(msg);
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	/*@RequestMapping(value = "/uploadFileNoticeSupplier.action", method = RequestMethod.POST)
    @ResponseBody public String uploadFileNoticeSupplier(FileUploadBean uploadItem, 
			   BindingResult result, 
			   String idNoticeDoc,
			   String createdBy,
			   String statusNotice,
            HttpServletResponse response,
            HttpSession session){

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		JSONObject json = new JSONObject();
		
		try{
			
			if(uploadItem.getFile().getSize()>0 && "ACEPTADO".equals(statusNotice)) {
				if (result.hasErrors()){
					for(ObjectError error : result.getAllErrors()){
					System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
					}
					
					json.put("success", false);
					json.put("message", "Error al cargar el archivo");
					}
					
					
					// SAAVI SECURITY - Validate file extension:
					if(isValidExtensionNotice(uploadItem.getFile().getOriginalFilename().trim())){
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
					documentsService.saveNotice(uploadItem, "loadNoticeFileSupplier", createdBy,idNoticeDoc);
					
					}else{
					json.put("success", false);
					json.put("message", "Error - Se permiten solamente archivos de tipo: " + String.join(", ", ALLOWED_FORMAT_NOTICE));
					}
			}
		
			NoticeDetail noticeDetail = noticeService.noticeDetailByIdNoticeAndAddresNumber(idNoticeDoc, createdBy);
			
			noticeDetail.setStatus(statusNotice);
			noticeDetail.setFecha(new Date());
			
			List<NoticeDetail> details = new ArrayList<NoticeDetail>();
			details.add(noticeDetail);
			noticeService.updateNoticeDetail(details);
			
			json.put("success", true);
			json.put("message", "Succ");
		
		return json.toString();
		
		}catch(Exception e){
		e.printStackTrace();
		
		}
		return json.toString();

}*/
	
	@RequestMapping(value = "/uploadFileNoticeSupplier.action", method = RequestMethod.POST)
    public String uploadFileNoticeSupplier( Model model,
    		 @RequestParam String idNoticeDoc,@RequestParam String createdBy, 
    		 @RequestParam String statusNotice, @RequestParam MultipartFile docSupp, HttpServletRequest request){

		try{
			NoticeDetail noticeDetail = noticeService.noticeDetailByIdNoticeAndAddresNumber(idNoticeDoc, createdBy);
			
			if(noticeDetail.getRequired() && "RECHAZADO".equals(statusNotice)) {
				return "login";
			}
			
			if(noticeDetail.getAttachment() && "RECHAZADO".equals(statusNotice)) {
				return "login";
			}
			
			if(docSupp.getSize()>0 && "ACEPTADO".equals(statusNotice)) {
				
				/*if (result.hasErrors()){
					for(ObjectError error : result.getAllErrors()){
					System.err.println("Error: " + error.getCode() +  " - " + error.getDefaultMessage());
					}
					
					json.put("success", false);
					json.put("message", "Error al cargar el archivo");
					}*/
					
					
					// SAAVI SECURITY - Validate file extension:
					if(isValidExtensionNotice(docSupp.getOriginalFilename().trim())){
						
					//String ct = uploadItem.getFile().getContentType();
					//if("application/pdf".equals(ct.trim()) || "image/jpg".equals(ct.trim())|| "image/jpeg".equals(ct.trim())){
					
					// SAAVI SECURITY - Validate file size:
					if(docSupp != null){
					if(docSupp.getSize() >  MAX_SIZE) {
						model.addAttribute("userName", createdBy);
						model.addAttribute("errorRequest", "El archivo " + docSupp.getOriginalFilename() + " excede del limite permitido de 25MB.");
						return "notice";
					}
					}
					
					/*if(uploadItem.getFileTwo() != null){
					if(uploadItem.getFileTwo().getSize() >  MAX_SIZE) {
					json.put("success", false);
					json.put("message", "El archivo " + docSupp.getOriginalFilename() + " excede del limite permitido de 25MB.");
					return json.toString();
					}
					}*/
					
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
					documentsService.saveDocNotice(docSupp, "loadNoticeFileSupplier", createdBy,idNoticeDoc);
					}else{
						model.addAttribute("userName", createdBy);
						model.addAttribute("errorRequest", "Error - Se permiten solamente archivos de tipo: " + String.join(", ", ALLOWED_FORMAT_NOTICE));
						return "notice";
					}
			}
			
			noticeDetail.setStatus(statusNotice);
			noticeDetail.setFecha(new Date());
			
			List<NoticeDetail> details = new ArrayList<NoticeDetail>();
			details.add(noticeDetail);
			noticeService.updateNoticeDetail(details);
		
			return "login";
		
		}catch(Exception e){
		e.printStackTrace();
		
		}
		return "login";
}
	
	@RequestMapping({"/notice/noticeActivesBySupp.action"})
	  @ResponseBody
	  public Map<String, Object> noticeActivesBySupp(String supp) {
	    
	    try {
	    	NoticeDTO notice = this.noticeService.noticeActivesBySupp(supp);
	    	return mapOK(notice);
	    } catch (Exception e) {
	      e.printStackTrace();
	      return mapError(e.getMessage());
	    } 
	  }
	
	@RequestMapping({"/notice/statusSuppsNotice.action"})
	  @ResponseBody
	  public Map<String, Object> statusSuppsNotice(String idNotice) {
	    
	    try {
	    	List<NoticeDetailDTO> notice = this.noticeService.statusSuppsNotice(idNotice);
	    	return mapOKSupp(notice,notice.size());
	    } catch (Exception e) {
	      e.printStackTrace();
	      return mapError(e.getMessage());
	    } 
	  }
	
	@RequestMapping(value ="/notice/openDocument.action", method = RequestMethod.GET)
    public void downloadFile(HttpServletResponse response, 
    			             @RequestParam int id) throws IOException {
     
		NoticeDocument doc = noticeService.noticeDocByIdNotice(id);
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
	
    @RequestMapping(value = "/uploadFileNotice.action", method = RequestMethod.POST)
    @ResponseBody public String uploadFileNotice(FileUploadBean uploadItem, 
    								   BindingResult result, 
    								   String idNoticeDoc,
    								   String createdBy,
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
       if(isValidExtensionNotice(uploadItem.getFile().getOriginalFilename().trim())){
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
            documentsService.saveNotice(uploadItem, documentType, createdBy,idNoticeDoc);

            json.put("success", true);
            json.put("message", "El archivo " + uploadItem.getFile().getOriginalFilename() + " ha sido cargado exitosamente.");
            json.put("fileName", uploadItem.getFile().getOriginalFilename());

        }else{
        	json.put("success", false);
        	json.put("message", "Error - Se permiten solamente archivos de tipo: " + String.join(", ", ALLOWED_FORMAT_NOTICE));
        }
        

        return json.toString();
        
        }catch(Exception e){
        	e.printStackTrace();

        }
        return json.toString();

    }
    
    public boolean isValidExtensionNotice(String filename) {
		boolean isValid = false;
		int ext = filename.lastIndexOf(".");
		if (ext >= 0) {
			for (String compare : ALLOWED_FORMAT_NOTICE) {
				String currentExtension = filename.substring(ext + 1);
				if (currentExtension.equalsIgnoreCase(compare)) {
					isValid = true;
				}
			}
		}
		return isValid;
	}
    
    public boolean isValidExtensionLayout(String filename) {
		boolean isValid = false;
		int ext = filename.lastIndexOf(".");
		if (ext >= 0) {
			for (String compare : ALLOWED_FORMAT_NOTICE_LAYOUT) {
				String currentExtension = filename.substring(ext + 1);
				if (currentExtension.equalsIgnoreCase(compare)) {
					isValid = true;
				}
			}
		}
		return isValid;
	}
    
	public Map<String,Object> mapOKSupp(List<NoticeDetailDTO> list, int total){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
		modelMap.put("success", true);
		return modelMap;
	}
	
	public Map<String,Object> mapOK(List<Notice> list, int total){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
		modelMap.put("success", true);
		return modelMap;
	}
	
	public Map<String, Object> mapOK(NoticeDTO obj) {
	    Map<String, Object> modelMap = new HashMap<>(3);
	    modelMap.put("total", Integer.valueOf(1));
	    modelMap.put("data", obj);
	    modelMap.put("success", Boolean.valueOf(true));
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
}
