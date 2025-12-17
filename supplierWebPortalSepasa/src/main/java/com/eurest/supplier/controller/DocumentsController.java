package com.eurest.supplier.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eurest.supplier.dto.UserDocumentDTO;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.FiscalDocumentsConcept;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.SupplierDocument;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.service.DocumentsService;
import com.eurest.supplier.service.FiscalDocumentService;
import com.eurest.supplier.service.JDERestService;
import com.eurest.supplier.service.PurchaseOrderService;
import com.eurest.supplier.service.UsersService;
import com.eurest.supplier.util.AppConstants;
 

@Controller
public class DocumentsController {
	
@Autowired
private DocumentsService documentsService;

@Autowired
private FiscalDocumentService fiscalDocumentService;

@Autowired
private PurchaseOrderService purchaseOrderService;

@Autowired
private UsersService usersService;

@Autowired
private JDERestService jdeRestService;

private Logger log4j = Logger.getLogger(DocumentsController.class);

@RequestMapping(value ="/documents/view.action")
public @ResponseBody Map<String, Object> view(@RequestParam int start,
											  @RequestParam int limit,
											  @RequestParam String query){	
	List<UserDocument> list=null;
	int total=0;
	try{
		if("".equals(query)){
			list = documentsService.getDocumentsList(start, limit);
			total = documentsService.getTotalRecords();
		}else{
			list = documentsService.searchCriteria(query);
			total = list.size();
		}					
	    return mapOK(list, total);
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
	
}

	@RequestMapping(value = "/documents/listDocumentsByOrder.action")
	public @ResponseBody
	Map<String, Object> viewByOrder(@RequestParam int start,
									  @RequestParam int limit,
									  @RequestParam int orderNumber, 
									  @RequestParam String orderType, 
									  @RequestParam String addressNumber) {
		List<UserDocument> list = null;
		int total = 0;
		try {
			list = documentsService.searchCriteriaByOrderNumber(orderNumber, 
					                                            orderType, 
					                                            addressNumber);
			

			if(list != null){
				total = list.size();
				for(UserDocument ud : list){
					ud.setContent(null);
				}
			}
			else
				total = 0;
			return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}

	}
	
	@RequestMapping(value = "/documents/listDocumentsByFiscalRef.action")
	public @ResponseBody
	Map<String, Object> viewByFiscalRef(@RequestParam int start,
									  @RequestParam int limit,
									  @RequestParam String addresNumber,
									  @RequestParam String uuid) {
		List<UserDocument> list = null;
		int total = 0;
		try {
			
			FiscalDocuments o = fiscalDocumentService.getFiscalDocumentsByUuid(uuid);
			if(o != null && o.isMultiOrder() && o.getMultiOrderIds() != null && !o.getMultiOrderIds().isEmpty()) {
				//Busca documentos Multi-Orden (Solo muestra los documentos de una orden para evitar repetidos)
				String[] oData = o.getMultiOrderIds().split(",");
				if(oData.length > 0) {
					PurchaseOrder po = purchaseOrderService.getOrderById(Integer.parseInt(oData[0]));
					if(po != null) {
						list = documentsService.searchCriteriaByOrderAndUuid(addresNumber, po.getOrderNumber(), po.getOrderType(), uuid);
					}
				}
			}
			
			//Flujo Normal
			if(!(list != null && !list.isEmpty())) {
				list = documentsService.searchCriteriaByRefFiscal(addresNumber, uuid);	
			}
			
			if(list != null){
				total = list.size();
				for(UserDocument ud : list){
					ud.setContent(null);
				}
			}
			
			return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}

	}
	
	@RequestMapping(value = "/public/listDocumentsByTicketId.action", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> viewDocumentsByTicketId(@RequestParam long ticketId) {
		List<UserDocument> list = null;
		int total = 0;
		try {
			list = documentsService.searchCriteriaByIdList(ticketId);
			if(list != null){
				total = list.size();
				for(UserDocument ud : list){
					ud.setContent(null);
				}
			}
			else
				total = 0;
			return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}

	}

	@RequestMapping(value = "/documents/listConceptDocumentsByFiscalRef.action")
	public @ResponseBody
	Map<String, Object> viewConceptDocumentsByFiscalRef(@RequestParam int start,
									  @RequestParam int limit,
									  @RequestParam String addresNumber,
									  @RequestParam String uuid) {
		
		List<UserDocument> list = null;
		List<UserDocument> listCompl = null;
		List<FiscalDocuments> fdList = null;
		List<UserDocument> newList = new ArrayList<UserDocument>();
		boolean isDocumentFounded = false;
		UserDocument doc = null;
		int total = 0;
		
		try {
			
			//Agregar conceptos y sus documentos si es que fueron cargados
			fdList = fiscalDocumentService.getFiscalDocuments(addresNumber, "", uuid, "", start, limit,"");
			list = documentsService.searchCriteriaByDescription(addresNumber, "MainUUID_".concat(uuid));

			if(fdList != null) {				
				for(FiscalDocuments fd : fdList) {
					if(list != null){
						for(UserDocument ud : list){									
							if(ud.getFiscalType() != null && "Factura".equals(ud.getFiscalType())) {								
								doc = new UserDocument();
								doc.setId(ud.getId());
								doc.setName(ud.getName());
								doc.setContent(null);
								doc.setUuid(ud.getUuid());
								doc.setAddressBook(ud.getAddressBook());
								doc.setDocumentType(ud.getDocumentType());
								doc.setDescription(ud.getDescription());										
								doc.setSize(ud.getSize());
								doc.setAccept(ud.isAccept());										
								doc.setDocumentNumber(ud.getDocumentNumber());										
								doc.setFiscalRef(ud.getFiscalRef());
								doc.setFiscalType(ud.getFiscalType());
								doc.setFolio(ud.getFolio());
								doc.setSerie(ud.getSerie());										
								doc.setStatus(ud.isStatus());
								doc.setType(ud.getType());
								doc.setUploadDate(ud.getUploadDate());
								newList.add(doc);
							}
						}
					}
					
					if(fd.getConcepts() != null) {
						for(FiscalDocumentsConcept concept : fd.getConcepts()) {
							isDocumentFounded = false;
							
							if(list != null){
								for(UserDocument ud : list){									
									if(ud.getDocumentType() != null && concept.getConceptName() != null
										&& ud.getDocumentType().equals(concept.getConceptName())) {
										
										doc = new UserDocument();
										doc.setId(ud.getId());
										doc.setName(ud.getName());
										doc.setContent(null);
										doc.setUuid(ud.getUuid());
										doc.setAddressBook(ud.getAddressBook());
										doc.setDocumentType(ud.getDocumentType());
										doc.setDescription(String.valueOf(concept.getAmount()));//Monto Concepto										
										doc.setSize(ud.getSize());
										doc.setAccept(ud.isAccept());										
										doc.setDocumentNumber(ud.getDocumentNumber());										
										doc.setFiscalRef(ud.getFiscalRef());
										doc.setFiscalType(ud.getFiscalType());
										doc.setFolio(ud.getFolio());
										doc.setSerie(ud.getSerie());										
										doc.setStatus(ud.isStatus());
										doc.setType(ud.getType());
										doc.setUploadDate(ud.getUploadDate());
										newList.add(doc);
										
										isDocumentFounded = true;
									}
								}
							}
							
							if(!isDocumentFounded) {
								//Se agrega el registro sin documento
								doc = new UserDocument();
								doc.setId(0);
								doc.setName("");
								doc.setContent(null);
								doc.setUuid(concept.getUuid());
								doc.setAddressBook(concept.getAddressNumber());
								doc.setDocumentType(concept.getConceptName());
								doc.setDescription(String.valueOf(concept.getAmount()));//Monto Concepto
								newList.add(doc);	
							}
						}
					}
					
					if(AppConstants.STATUS_COMPLEMENT.equals(fd.getStatus())
							&& fd.getComplPagoUuid() != null && !fd.getComplPagoUuid().isEmpty()) {
						
						listCompl = documentsService.searchCriteriaByRefFiscal(fd.getAddressNumber(), fd.getComplPagoUuid());
						if(listCompl != null && !listCompl.isEmpty()) {
							
							List<String> complName = new ArrayList<String>();							
							for(UserDocument docCompl : listCompl) {
								
								//Solo se agrega una vez
								if(!complName.contains(docCompl.getName())) {
									complName.add(docCompl.getName());
									
									doc = new UserDocument();
									doc.setId(docCompl.getId());
									doc.setName(docCompl.getName());
									doc.setContent(null);
									doc.setUuid(docCompl.getUuid());
									doc.setAddressBook(docCompl.getAddressBook());
									doc.setDocumentType(docCompl.getDocumentType());
									doc.setDescription(docCompl.getDescription());										
									doc.setSize(docCompl.getSize());
									doc.setAccept(docCompl.isAccept());										
									doc.setDocumentNumber(docCompl.getDocumentNumber());										
									doc.setFiscalRef(docCompl.getFiscalRef());
									doc.setFiscalType(docCompl.getFiscalType());
									doc.setFolio(docCompl.getFolio());
									doc.setSerie(docCompl.getSerie());										
									doc.setStatus(docCompl.isStatus());
									doc.setType(docCompl.getType());
									doc.setUploadDate(docCompl.getUploadDate());
									newList.add(doc);
								}
							}
						}
					}
				}
				
				total = newList.size();
			} else {
				total = 0;
			}


			/*
			if(list != null){
				total = list.size();
				for(UserDocument ud : list){
					ud.setContent(null);
					ud.setDescription("");
					if(fdList != null) {
						for(FiscalDocuments fd : fdList) {
							if(fd.getConcepts() != null) {
								for(FiscalDocumentsConcept concept : fd.getConcepts()) {
									if(ud.getDocumentType() != null && concept.getConceptName() != null
											&& ud.getDocumentType().equals(concept.getConceptName())) {										
										ud.setDescription(String.valueOf(concept.getAmount()));
										break;
									}
								}
							}
						}
					}
				}
			}
			else {
				total = 0;
			}
			*/
			return mapOK(newList, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value ="/documents/openDocument.action", method = RequestMethod.GET)
    public void downloadFile(HttpServletResponse response, 
    			             @RequestParam int id) throws IOException {
     
		UserDocument doc = documentsService.getDocumentById(id);
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
	
	@RequestMapping(value ="/documents/openDocumentSupplier.action", method = RequestMethod.GET)
    public void downloadFileSupplier(HttpServletResponse response, 
    			             @RequestParam int id) throws IOException {
     
		SupplierDocument doc = documentsService.getDocumentSuppById(id);
		String fileName = doc.getName();
		String contentType = doc.getType();

        if(doc.getName().toUpperCase().endsWith(".PDF")
        		|| doc.getName().toUpperCase().endsWith(".JPG")
        		|| doc.getName().toUpperCase().endsWith(".JPEG")
        		|| doc.getName().toUpperCase().endsWith(".PNG")) {
    		response.setHeader("Content-Type", contentType);
            response.setHeader("Content-Length", String.valueOf(doc.getContent().length));
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        } else {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        }
        InputStream is = new ByteArrayInputStream(doc.getContent());
        byte[] bytes = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(bytes)) != -1) {
            response.getOutputStream().write(bytes, 0, bytesRead);
        }
        is.close();
    }
	
	@RequestMapping(value ="/public/openSecuredDocument.action", method = RequestMethod.GET)
    public void openSecuredDocument(HttpServletResponse response, @RequestParam String token) throws IOException {
     
		UserDocument doc = documentsService.searchSecuredDocument(token);
		if(doc != null) {
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
		else {
	        String inputString = "File Not Found";
	        byte[] byteArrray = inputString.getBytes();
			response.setHeader("Content-Type", "text/plain");
	        response.setHeader("Content-Length", String.valueOf(byteArrray.length));
	        response.setHeader("Content-Disposition", "inline; filename=\"" + "FILE NOT FOUND.TXT" + "\"");
	        InputStream is = new ByteArrayInputStream(byteArrray);
	        byte[] bytes = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = is.read(bytes)) != -1) {
	        	response.getOutputStream().write(bytes, 0, bytesRead);
	        }
	        is.close();
        }
    }
	
	@SuppressWarnings("unused")
	@RequestMapping(value ="/documents/listDocumentByType.action", method = RequestMethod.GET)
    public Map<String, Object> listFileByType(@RequestParam int start,
			  								  @RequestParam int limit, 
			  								  @RequestParam String type) throws IOException {
     
		List<UserDocument> list = documentsService.searchCriteriaByType(type);
		int total = 0;
		try {
			if(list != null){
				total = list.size();
				for(UserDocument ud : list){
					ud.setContent(null);
				}
			}
			else
				total = 0;
			
			return null;
			//return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
    }

	@RequestMapping(value = "/documents/save.action", method = RequestMethod.POST)
	public @ResponseBody
	Map<String, Object> save(@RequestBody UserDocument obj) {

		try{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usr = auth.getName();
		documentsService.save(obj,new Date(),usr);
		return mapOK(new UserDocument());
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
}

@RequestMapping(value ="/documents/update.action")
public @ResponseBody Map<String, Object> update(@RequestBody UserDocument obj){
	
	try{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usr = auth.getName();
		documentsService.update(obj,new Date(),usr);
		return mapOK(new UserDocument());
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
}

@RequestMapping(value ="/documents/delete.action")
public @ResponseBody Map<String, Object>  delete(@RequestParam int id){

	try{ 
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usr = auth.getName();
		documentsService.delete(id, usr);
		return mapOK(new UserDocument());
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
}


@SuppressWarnings("unused")
@RequestMapping(value ="/documents/getListFacByFech.action", method = RequestMethod.GET)
public @ResponseBody Map<String, Object> getListFacByDate(
		@RequestParam String poFromDate,
		@RequestParam String poToDate,
		@RequestParam String userName){	
	List<UserDocumentDTO> list = null;
	List<UserDocumentDTO> listAduana = null;
	
	try{
		 SimpleDateFormat objSDF = new SimpleDateFormat("dd/MM/yyyy");
		
		String usr = userName;
		String role = usersService.searchCriteriaUserName(usr).getRole();
		list = documentsService.getListFacByDate(objSDF.parse(poFromDate), objSDF.parse(poToDate),userName,role,""); 
		
		
		String uuidListIn="";
		for (UserDocumentDTO userDocumentDTO : list) {
			if (uuidListIn.length()>0) {
				uuidListIn=uuidListIn+",";
			}
			uuidListIn=uuidListIn+userDocumentDTO.getUuid();
		}
		
		List<String> listajde=uuidListIn.equals("")?new ArrayList<String>():jdeRestService.getListFactJde(uuidListIn,"normal");
		
		
		listAduana = documentsService.getListFacByDate(objSDF.parse(poFromDate), objSDF.parse(poToDate),userName,role,"aduana"); 
		String uuidListInAduana="";
		for (UserDocumentDTO userDocumentDTO : listAduana) {
			if (uuidListInAduana.length()>0) {
				uuidListInAduana=uuidListInAduana+",";
			}
			uuidListInAduana=uuidListInAduana+userDocumentDTO.getUuid();
		}
		List<String> listajdeAduana=uuidListInAduana.equals("")?new ArrayList<String>():jdeRestService.getListFactJde(uuidListInAduana,"aduana");
		
		HashMap<String, String> listFilesPdf=jdeRestService.getListFilesMiddleware();
		
		
		String doc=documentsService.createReportExcel( usersService.searchCriteriaUserName(usr),list, listajde, listAduana, listajdeAduana,listFilesPdf);
		
		
	        
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
	return mapOK(new UserDocument());
}

@SuppressWarnings("unused")
@RequestMapping(value ="/documents/sendAlertsComFac.action", method = RequestMethod.GET)
public @ResponseBody Map<String, Object> sendAlertsComFac(
		){	

	
	try{
		
		
		jdeRestService.sendPendingPaymentComplement();
		jdeRestService.sendPendingReceipInvoice();
		
		
	        
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return mapError(e.getMessage());
	}
	return mapOK(new UserDocument());
}


public Map<String,Object> mapOK(List<UserDocument> list, int total){
	Map<String,Object> modelMap = new HashMap<String,Object>(3);
	modelMap.put("total", total);
	modelMap.put("data", list);
	modelMap.put("success", true);
	return modelMap;
}

public Map<String,Object> mapOK(UserDocument obj){
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



}
