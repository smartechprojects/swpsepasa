package com.eurest.supplier.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.dto.SupplierDTO;
import com.eurest.supplier.model.AccessTokenRegister;
import com.eurest.supplier.model.CodigosPostales;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.NonComplianceSupplier;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.service.DataAuditService;
import com.eurest.supplier.service.JDERestService;
import com.eurest.supplier.service.SupplierService;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.service.UsersService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.StringUtils;


@Controller
public class SupplierController {
  @Autowired
  private SupplierService supplierService;
  
  @Autowired
  JDERestService jDERestService;
  
  @Autowired
  SupplierDao supplierDao;
  
  @Autowired
  UsersService userService;
  
  @Autowired
  UdcService udcService;
  
  @Autowired
  DataAuditService dataAuditService;
  
  private Logger log4j = Logger.getLogger(SupplierController.class);
  
  @RequestMapping({"/supplier/view.action"})
  @ResponseBody
  public Map<String, Object> view(@RequestParam int start, @RequestParam int limit, HttpServletRequest request) {
    List<SupplierDTO> list = null;
    int total = 0;
    try {
      list = this.supplierService.getList(start, limit);
      total = list.size();
      return mapOK(list, total);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/getById.action"})
  @ResponseBody
  public Map<String, Object> getById(@RequestParam int start, @RequestParam int limit, @RequestParam int id, HttpServletRequest request) {
    Supplier sup = null;
    try {
      sup = this.supplierService.getSupplierById(id);
      return mapOK(sup);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/getByAddressNumber.action"})
  @ResponseBody
  public Map<String, Object> getByAddressNumber(@RequestParam String addressNumber) {
    Supplier sup = null;
    try {
      sup = this.supplierService.searchByAddressNumber(addressNumber);
      return mapOK(sup);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value ="/supplier/getSuppliersByFilter.action")
	public @ResponseBody Map<String, Object> getSuppliersByFilter(@RequestParam int start,
												  @RequestParam int limit,
												  @RequestParam String query){	
		List<SupplierDTO> list=null;
		int total=0;
		try{
			list = this.supplierService.getSuppliersByFilter(query);
			total = list.size();
			return mapOK(list, total);
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
  /*
  @RequestMapping({"/supplier/correctionEmail.action"})
  @ResponseBody
  public void correctionEmail() {
    List<Supplier> sup = null;
    try {
      sup = supplierDao.correctionEmail();
      for(Supplier o : sup) {
    	  Users u = userService.getByUserName(o.getAddresNumber());
    	  u.setEmail(o.getEmailSupplier());
    	  userService.update(u, null, null);
      }
      
      System.out.println("Actualizacion Email usuarios terminada");
      
      //return mapOK(sup);
    } catch (Exception e) {
      e.printStackTrace();
      //return mapError(e.getMessage());
    } 
  }
  */
  @RequestMapping({"/public/getCountRFC.action"})
  @ResponseBody
  public Map<String, Object> getCountRFC(@RequestParam String rfcSupplier, @RequestParam String typeSearch ) {
	  List<Supplier> sups = null;
    try {
    	sups  = this.supplierService.searchByRfc(rfcSupplier,typeSearch);
    	String msg = Integer.toString(sups.size());
      return mapStrOk(msg);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/updateEmailSupplier.action"})
  @ResponseBody
  public Map<String, Object> updateEmailSupplier(@RequestParam int idSupplier, @RequestParam String emailSupplier) {
    try {
      String sup = this.supplierService.updateEmailSupplier(idSupplier, emailSupplier);
      return mapStrOk(sup);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/public/getByTicketId.action"})
  @ResponseBody
  public Map<String, Object> getById(@RequestParam long ticketId) {
    Supplier sup = null;
    try {
      sup = this.supplierService.searchByTicket(ticketId);
      //sup.setFileList("");
      return mapOK(sup);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }

  
  @RequestMapping(value ="/supplier/disableSupplier.action", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> disableSupplier(
													@RequestParam String idSupplier,
													@RequestParam String userDisable){
		try{
			String msg = supplierService.disableSupplier(idSupplier,userDisable);
			return mapStrOk(msg);
		} catch (Exception e) {
			System.out.println("Exception e: " + e);
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
  
  @RequestMapping({"/supplier/searchByCriteria.action"})
  @ResponseBody
  public Map<String, Object> getByName(@RequestParam int start, @RequestParam int limit, @RequestParam String query, HttpServletRequest request) {
    List<SupplierDTO> list = null;
    int total = 0;
    try {
      list = this.supplierService.searchByCriteria(query, start, limit);
      total = list.size();
      return mapOK(list, total);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/searchSupplier.action"})
  @ResponseBody
  public Map<String, Object> getSuppliers(@RequestParam int start, @RequestParam int limit, @RequestParam String supAddNbr, @RequestParam String supAddName) {
    List<SupplierDTO> list = null;
    int total = 0;
    try {
      list = this.supplierService.listSuppliers(supAddNbr, supAddName, start, limit);
      total = this.supplierService.listSuppliersTotalRecords(supAddNbr, supAddName);
      return mapOK(list, total);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/newRegister/save.action"}, method = {RequestMethod.POST})
  @ResponseBody
  public Map<String, Object> save(@RequestBody Supplier obj) {
    try {
      long ticket = this.supplierService.saveSupplier(obj);
      return mapStrOk(String.valueOf(ticket));
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/public/update.action"}, method = {RequestMethod.POST})
  @ResponseBody
  public Map<String, Object> update(@RequestBody Supplier obj) {
    try {
      long ticketId = this.supplierService.updateSupplier(obj);
      if (ticketId == -1L) return mapStrOk(String.valueOf("ERROR_COMPL")); 
      if (ticketId == -2L) return mapStrOk(String.valueOf("Error JDE")); 
      return mapStrOk(String.valueOf(ticketId));
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/codigoPostal/view.action"}, method = {RequestMethod.GET})
  @ResponseBody
  public Map<String, Object> update(@RequestParam int start, @RequestParam int limit, @RequestParam String query) {
    try {
      List<CodigosPostales> list = this.supplierService.getByCode(query, start, limit);
      if (list != null)
        return mapOKCP(list, list.size()); 
      return mapOKCP(new ArrayList<>(), 0);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/public/emailValidation.action"}, method = {RequestMethod.GET})
  @ResponseBody
  public Map<String, Object> emailValidation(@RequestParam String emailComprador, HttpServletRequest request, 
          HttpSession session) {
    try {
      Users u = this.supplierService.getPurchaseRoleByEmail(emailComprador.toLowerCase());
      if (u != null)
        return mapOK(new Boolean(true));  
      return mapError("El correo electrónico del comprador no existe en la base de datos de SEPASA. Revise nuevamente los datos capturados.");
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/public/supplierReplication.action"}, method = {RequestMethod.GET})
  @ResponseBody
  public Map<String, Object> supplierReplication(HttpServletRequest request) {
    this.jDERestService.getNewAddressBook();
    return mapStrOk("OK");
  }
  
  @RequestMapping({"/supplier/getCustomBroker.action"})
  @ResponseBody
  public Map<String, Object> getCustomBroker(@RequestParam String addressNumber) {
    List<UDC> udcList = null;
    boolean isCustomBroker = false;
    try {
    	udcList = udcService.searchBySystem("CUSTOMBROKER");
    	
    	if(udcList != null && !udcList.isEmpty()) {
    		for(UDC udc : udcList) {
    			if(udc.getUdcKey().trim().equals(addressNumber.trim())) {
    				isCustomBroker = true;
    				break;
    			}
    		}
    	}
    	
      return mapBooleanOk(isCustomBroker);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/getTransportCustomBroker.action"})
  @ResponseBody
  public Map<String, Object> getTransportCustomBroker(@RequestParam String addressNumber) {
    List<UDC> udcList = null;
    boolean isTransportCustomBroker = false;
    try {
    	udcList = udcService.searchBySystem("CUSTOMBROKER");
    	
    	if(udcList != null && !udcList.isEmpty()) {
    		for(UDC udc : udcList) {
    			if(udc.getUdcKey().trim().equals(addressNumber.trim()) && udc.getStrValue1() != null && "Y".equals(udc.getStrValue1().trim())) {
    				isTransportCustomBroker = true;
    				break;
    			}
    		}
    	}
    	
      return mapBooleanOk(isTransportCustomBroker);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping({"/supplier/getIsHotelSupplier.action"})
  @ResponseBody
  public Map<String, Object> getIsHotelSupplier(@RequestParam String addressNumber) {
    List<UDC> udcList = null;
    boolean isHotelSupplier = false;
    try {
    	udcList = udcService.searchBySystem("HOTELSUPPLIER");
    	
    	if(udcList != null && !udcList.isEmpty()) {
    		for(UDC udc : udcList) {
    			if(udc.getUdcKey().trim().equals(addressNumber.trim())) {
    				isHotelSupplier = true;
    				break;
    			}
    		}
    	}
    	
      return mapBooleanOk(isHotelSupplier);
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  @RequestMapping(value = {"/supplier/update.action"}, method = {RequestMethod.POST})
  @ResponseBody
  public Map<String, Object> updateSupplier(@RequestBody SupplierDTO obj) {
    try {
    	
    	/*Supplier sup=this.supplierService.getSupplierById(obj.getId());
    	String before=new Gson().toJson(sup);
    	
    	
    	boolean bandera_outsourc=obj.isRepse()==sup.isOutSourcing();
    	sup.setOutSourcing(obj.isRepse());
    	sup.setOutSourcingAccept(obj.isOutSourcingAccept());
    	sup.setOutSourcingMonthlyAccept(obj.isOutSourcingMonthlyAccept());
    	sup.setOutSourcingBimonthlyAccept(obj.isOutSourcingBimonthlyAccept());
    	sup.setOutSourcingQuarterlyAccept(obj.isOutSourcingQuarterlyAccept());
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	  HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String usr = auth.getName();	
		
		
    	DataAudit audit=new DataAudit();
    	audit.setUser(usr);
    	audit.setCreationDate(new Date());
    	audit.setAddressNumber(sup.getAddresNumber());
    	audit.setMethod("om.eurest.supplier.controller.SupplierController.updateSupplier");
    	audit.setModule("REPSE");
    	audit.setIp(request.getRemoteAddr());
    	
    	if (bandera_outsourc) {
			audit.setNotes(before+ "==>"+ new Gson().toJson(sup));
			
			
		}else {
			audit.setAction(sup.isOutSourcing()?"REPSE":"NO REPSE");
		}
    	
    	
    	
      long ticketId = this.supplierService.update(sup);
    	
      if (ticketId != 1) {return mapStrOk(String.valueOf("ERROR_COMPL"));} 
      dataAuditService.save(audit);
      return mapStrOk(String.valueOf("El cambio se guardó exitosamente."));*/
    	
    	Supplier sup=this.supplierService.getSupplierById(obj.getId());
    	differencesSupplier(sup, obj);
    	sup.setOutSourcing(obj.isRepse());
    	sup.setOutSourcingAccept(obj.isOutSourcingAccept());
    	sup.setOutSourcingMonthlyAccept(obj.isOutSourcingMonthlyAccept());
    	sup.setOutSourcingBimonthlyAccept(obj.isOutSourcingBimonthlyAccept());
    	sup.setOutSourcingQuarterlyAccept(obj.isOutSourcingQuarterlyAccept());
    	
      long ticketId = this.supplierService.update(sup);
      if (ticketId != 1) return mapStrOk(String.valueOf("ERROR_COMPL")); 
      return mapStrOk(String.valueOf("El cambio se guardó exitosamente."));
      
    } catch (Exception e) {
      e.printStackTrace();
      return mapError(e.getMessage());
    } 
  }
  
  //OUTSOURCING
  @RequestMapping(value ="/public/approveOutsourcingRequest", method = RequestMethod.GET)
  public String approveOutsourcingRequest(Model model, @RequestParam String ab, @RequestParam String token) {

      Supplier s = supplierService.searchByAddressNumber(ab);
      if(s != null) {
    	  s.setOutSourcingAccept(true);
    	  s.setOutSourcingRecordDate(new Date());
    	  supplierService.updateSupplierOutSourcing(s);
    	  supplierService.sendOutSourcingEmail(s);
    	  
          model.addAttribute("supplierName", s.getRazonSocial());
  		  model.addAttribute("message", "_APPROVED");
      }
		  return "outsourcingFiles";
  }
  
  @RequestMapping(value ="/public/deleteSupplier.action", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> deleteSupplier(@RequestParam int id) {

  	try{ 
  		supplierService.delete(id);
  		return mapStrOk("delete_supp_succ");
  	} catch (Exception e) {
  		log4j.error("Exception" , e);
  		e.printStackTrace();
  		return mapError(e.getMessage());
  	}
  }
  
  public Map<String, Object> mapOK(List<SupplierDTO> list, int total) {
    Map<String, Object> modelMap = new HashMap<>(3);
    modelMap.put("total", Integer.valueOf(total));
    modelMap.put("data", list);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapOK(SupplierDTO obj) {
    Map<String, Object> modelMap = new HashMap<>(3);
    modelMap.put("total", Integer.valueOf(1));
    modelMap.put("data", obj);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapOK(Users obj) {
    Map<String, Object> modelMap = new HashMap<>(3);
    modelMap.put("total", Integer.valueOf(1));
    modelMap.put("data", obj);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapOK(Boolean obj) {
	    Map<String, Object> modelMap = new HashMap<>(3);
	    modelMap.put("total", Integer.valueOf(1));
	    modelMap.put("data", obj);
	    modelMap.put("success", Boolean.valueOf(true));
	    return modelMap;
	  }
  
  public Map<String, Object> mapOK(Supplier obj) {
    Map<String, Object> modelMap = new HashMap<>(3);
    modelMap.put("total", Integer.valueOf(1));
    modelMap.put("data", obj);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapError(String msg) {
    Map<String, Object> modelMap = new HashMap<>(2);
    modelMap.put("message", msg);
    modelMap.put("success", Boolean.valueOf(false));
    return modelMap;
  }
  
  public Map<String, Object> mapStrOk(String msg) {
    Map<String, Object> modelMap = new HashMap<>(2);
    modelMap.put("message", msg);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapOKCP(List<CodigosPostales> list, int total) {
    Map<String, Object> modelMap = new HashMap<>(3);
    modelMap.put("total", Integer.valueOf(total));
    modelMap.put("data", list);
    modelMap.put("success", Boolean.valueOf(true));
    return modelMap;
  }
  
  public Map<String, Object> mapBooleanOk(boolean obj) {
	  Map<String, Object> modelMap = new HashMap<>(2);
	  modelMap.put("data", Boolean.valueOf(obj));
	  modelMap.put("success", Boolean.valueOf(true));
	  return modelMap;	   
  }
  

  @SuppressWarnings("unused")
public String getEncodedString(String ticketId, String rfc, String recordId) {
	  String prefix = StringUtils.randomString(8);
	  String sufix = "/" + StringUtils.randomString(6) + "==";
	  String midStr = StringUtils.randomString(3) + "a/";
	  return "";
}

  public void differencesSupplier(Supplier currentS, SupplierDTO obj) {
	  
	  DataAudit dataAudit = new DataAudit();
  	  Date currentDate = new Date();
  	
  	  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	  HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	  String usr = auth.getName(); 	
  	
	  String msg ="";
	  
	  if(!Objects.equals(currentS.isOutSourcing(),obj.isRepse())) {
		  if(obj.isRepse()) {  
			  msg = "Enabled Supplier REPSE";
		  }else {
			  msg = "Disenabled Supplier REPSE";
		  }
		  
	  }
	  
	  if(!Objects.equals(currentS.isOutSourcingAccept(),obj.isOutSourcingAccept())) {
		  if(obj.isOutSourcingAccept()) {  
			  msg = "Enabled Base Line Upload REPSE";
		  }else {
			  msg = "Disenabled Base Line Upload REPSE";
		  }
	  }
	  
	  if(!Objects.equals(currentS.isOutSourcingMonthlyAccept(),obj.isOutSourcingMonthlyAccept())) {
		  if(obj.isOutSourcingMonthlyAccept()) {  
			  msg = "Enabled Monthly Upload REPSE";
		  }else {
			  msg = "Disenabled Monthly Upload REPSE";
		  }
	  }
	  
	  if(!Objects.equals(currentS.isOutSourcingBimonthlyAccept(), obj.isOutSourcingBimonthlyAccept())) {
		  if(obj.isOutSourcingBimonthlyAccept()) {  
			  msg = "Enabled Bimonthly Upload REPSE";
		  }else {
			  msg = "Disenabled Bimonthly Upload REPSE";
		  }
	  }
	  
      if(!Objects.equals(currentS.isOutSourcingQuarterlyAccept(),obj.isOutSourcingQuarterlyAccept())) {
    	  if(obj.isOutSourcingQuarterlyAccept()) {  
			  msg = "Enabled Quarterly Upload REPSE";
		  }else {
			  msg = "Disenabled Quarterly Upload REPSE";
		  }
	  }
	  
    dataAudit.setAction("UpdateSupplierREPSE");
  	dataAudit.setAddressNumber(currentS.getAddresNumber());
  	dataAudit.setCreationDate(currentDate);
  	dataAudit.setDocumentNumber(null);
  	dataAudit.setIp(request.getRemoteAddr());
  	dataAudit.setMethod("updateSupplier");
  	dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
  	dataAudit.setOrderNumber(null);
  	dataAudit.setUuid(null);
  	dataAudit.setStep(null);
  	dataAudit.setMessage(msg);
  	dataAudit.setNotes(null);
  	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
  	dataAudit.setUser(usr);
  	dataAuditService.save(dataAudit);
	  
	  

  }
  
  @RequestMapping({ "/supplier/token/listAccessTokenRegister.action" })
	@ResponseBody
	public Map<String, Object> listAccessTokenRegister(@RequestParam int start, @RequestParam int limit,
			@RequestParam String query) {
		List<AccessTokenRegister> list = null;
		int total = 0;
		try {
			list = supplierService.listAccessTokenRegister(query, start, limit);
			total = supplierService.listAccessTokenRegisterCount(query);
			return mapOKAccessTokenRegister(list, total);
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}


	@RequestMapping({ "/supplier/token/saveAccessTokenRegister.action" })
	@ResponseBody
	public Map<String, Object> listAccessTokenRegister(@RequestBody AccessTokenRegister o) {

		try {
			
			List<Supplier> rfc = supplierService.validateTaxId(o.getRfc());
			
			if(rfc != null && rfc.size()>0) {
				return mapError("RFC/Tax Id ya registrado en la base de datos<br><br>");
			}
			
			NonComplianceSupplier blackList = supplierService.validateTaxIdInBlackList(o.getRfc());
			if(blackList != null) return mapError("RFC/Tax Id encontrado en las listas negras<br><br>");
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usr = auth.getName();
			supplierService.saveAccessToken(o, usr, "");
			return mapStrOk("OK");
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping({ "/supplier/token/updateAccessTokenRegister.action" })
	@ResponseBody
	public Map<String, Object> updateAccessTokenRegister(@RequestBody AccessTokenRegister o) {

		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usr = auth.getName();
			supplierService.updateAccessToken(o, usr);
			return mapStrOk("OK");
		} catch (Exception e) {
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/public/authRegister", method = RequestMethod.GET)
	public String authRegister(@RequestParam String access_token, HttpServletRequest request, Model model) {
		try {
			return supplierService.validateAcccessTokenRegister(access_token, request, false);
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	//@RequestMapping(value = "/public/validateTaxId", method = RequestMethod.POST)
	@RequestMapping(value = "/public/validateTaxId", method = {RequestMethod.GET, RequestMethod.POST})
	public String validateTaxId(@RequestParam String access_token, @RequestParam String taxId,  Model model) {
		try {
			
			if(taxId.contains("_APM_")) {
	      		  taxId= taxId.replace("_APM_", "&");
	      	  	}
			
			if(!supplierService.validateNonCompliance(taxId, access_token)) {
				return "nonCompliancePage";
			}
			
			List<Supplier> list = supplierService.searchByRfc(taxId, "rfc");
			if(list != null && list.size() > 0) {
				return "redirect:/invalidNewRegisterAuth.action?access_token=" + access_token;
			}else {
				list = supplierService.searchByRfc(taxId, "taxId");
				if(list != null && list.size() > 0) {
					return "redirect:/invalidNewRegisterAuth.action?access_token=" + access_token;
				}
			}
			//return supplierService.validateTaxId(taxId, access_token);
			
			if(taxId.contains("&")) {
      		  taxId= taxId.replace("&", "_APM_");
      	  	}
			
			AccessTokenRegister reg = supplierService.searchByToken(access_token);
			if(reg != null) {
				reg.setAssigned(true);
				this.supplierDao.updateAccessToken(reg);
			}
			
			return "redirect:/newRegister.action?access_token=" + access_token+"&rfcValid=" +taxId;
			
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	@RequestMapping(value="/newRegisterAuth.action",method = RequestMethod.GET)
	public String newRegisterAuth(Model model, @RequestParam String access_token, HttpServletRequest request){
		
		String validateToken = supplierService.validateAcccessTokenRegister(access_token, request, true);
		if(!"invalidToken".equals(validateToken)) {
			//model.addAttribute("access_token", access_token);
			//return "startNewRegister";
			AccessTokenRegister reg = supplierService.searchByToken(access_token);
			if(reg != null) {
				String taxId = reg.getRfc();
				if(taxId.contains("&")) {
		      		  taxId= taxId.replace("&", "_APM_");
		      	  	}
				
				return "redirect:/public/validateTaxId?access_token=" + access_token + "&taxId=" +taxId;
			}
			
			return validateToken;
		}else {
			return validateToken;
		}
	}
	
	@RequestMapping(value="/invalidNewRegisterAuth.action",method = RequestMethod.GET)
	public String invalidNewRegisterAuth(Model model, @RequestParam String access_token, HttpServletRequest request){
		model.addAttribute("errorRequest", "ERROR: RFC or TAX ID Already exists");
		model.addAttribute("access_token", access_token);
		return "startNewRegister";
	}
	
	@RequestMapping(value="/invalidRegister.action",method = RequestMethod.GET)
	public String newRegister(Model model, @RequestParam String addInvalid, HttpServletRequest request){
		
		model.addAttribute("addInvalid", addInvalid);
		
		return "invalidRegister";
	}
	
	public Map<String, Object> mapOKAccessTokenRegister(List<AccessTokenRegister> list, int total) {
		Map<String, Object> modelMap = new HashMap<>(3);
		modelMap.put("total", Integer.valueOf(total));
		modelMap.put("data", list);
		modelMap.put("success", Boolean.valueOf(true));
		return modelMap;
	}
	
	@RequestMapping(value="/requestTicketPage.action",method = RequestMethod.GET)
	public String newRegisterWithTicket(Model model, HttpServletRequest request){
		return "requestTicketPage";
	}
	
	@RequestMapping(value="/openTicketRequest.action",method = RequestMethod.POST)
	public String newRegisterWithTicket(Model model, @RequestParam String rfc, @RequestParam long ticket, 
			HttpServletRequest request){
		
		Supplier sup = this.supplierService.searchByTicket(ticket);
		if(sup != null) {
			
			if("MX".equals(sup.getCountry())) {
				if(!sup.getRfc().equals(rfc)) {
					model.addAttribute("errorRequest", "ERROR: El RFC no est� registrado");
					return "requestTicketPage";
				}
			}else {
				if(!sup.getTaxId().equals(rfc)) {
					model.addAttribute("errorRequest", "ERROR: TaxId is not registered");
					return "requestTicketPage";
				}
			}
			/*if(sup.getRfc() != null || !"".equals(sup.getRfc())){
				if(!sup.getRfc().equals(rfc)) {
					model.addAttribute("errorRequest", "ERROR: El RFC no est� registrado");
					return "requestTicketPage";
				}
			}else {
				if(sup.getTaxId() != null || !"".equals(sup.getTaxId())){
					if(!sup.getTaxId().equals(rfc)) {
						model.addAttribute("errorRequest", "ERROR: TaxId is not registered");
						return "requestTicketPage";
					}
				}
			}*/
			
			if(AppConstants.STATUS_INPROCESS.equals(sup.getApprovalStatus())) {
				model.addAttribute("errorRequest", "ERROR: Su solicitud ya no se encuentra disponible en formato borrador");
				return "requestTicketPage";
			}
			
			if(AppConstants.STATUS_ACCEPT.equals(sup.getApprovalStatus())) {
				model.addAttribute("errorRequest", "ERROR: Su solicitud ya no se encuentra disponible en formato borrador");
				return "requestTicketPage";
			}
			
			model.addAttribute("ticketAccepted", ticket);
			return "newRegister";
		}else {
			model.addAttribute("errorRequest", "ERROR: No record found");
			return "requestTicketPage";
		}

	}
  
}
