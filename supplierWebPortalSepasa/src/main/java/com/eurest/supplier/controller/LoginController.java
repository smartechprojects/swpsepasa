package com.eurest.supplier.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.eurest.supplier.dao.NoticeDao;
import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.model.AccessTokenRegister;
import com.eurest.supplier.model.NoticeDetail;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.service.OutSourcingService;
import com.eurest.supplier.service.PurchaseOrderService;
import com.eurest.supplier.service.SupplierService;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.service.UsersService;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.PropertiesLoader;


@Controller
public class LoginController {
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	UdcService udcService;
	
	@Autowired
	PurchaseOrderService purchaseOrderService;
	
	@Autowired
	private SupplierService supplierService;
	
	@Autowired
	OutSourcingService outSourcingService;
	
	@Autowired
	NoticeDao noticeDao;
	
	@Autowired
	SupplierDao supplierDao;
	
	Logger log4j = Logger.getLogger(LoginController.class);
	
	@RequestMapping(value="/home.action",method = RequestMethod.GET)
	public String home(Model model,HttpServletRequest request){
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usr = auth.getName();	
		Users e = usersService.searchCriteriaUserName(usr);
		if(e!=null) {
			
			Supplier s = supplierService.searchByAddressNumber(e.getUserName()); 
			if(s != null){
				model.addAttribute("invException", s.getInvException());
				
				boolean paymentComplException = false;
				List<UDC> pcExceptionList =  udcService.searchBySystem("NOPAYMENTCOMPL");
				if(pcExceptionList != null) {
					for(UDC udc : pcExceptionList) {
						if(s.getAddresNumber().equals(udc.getStrValue1())){
							paymentComplException = true;
							break;
						}
					}
				}
				
				if(!paymentComplException) {
					String pendingList = purchaseOrderService.getPendingReceiptsComplPago(s.getAddresNumber());
					if(!"".equals(pendingList)){
						if(s.getCountry() != null && "MX".equals(s.getCountry().trim())) {
							model.addAttribute("approveNotif", "Estimado proveedor: Usted cuenta con COMPLEMENTOS DE PAGO pendientes de carga");
						}
					}
				}
			}
			
			model.addAttribute("id", e.getId());
			model.addAttribute("name", e.getName());
			model.addAttribute("email", e.getEmail());
			model.addAttribute("userName", e.getUserName());
			model.addAttribute("role", e.getRole());
			model.addAttribute("type", e.getUserType().getUdcKey());
			model.addAttribute("userType", e.getUserType().getStrValue1());
			model.addAttribute("multipleRfc", "empty");
			
			request.getSession().setAttribute("userAuth", e.getUserName());

			if(!e.isLogged()) {
				return "changePassword";
			}
			
			if(s != null){
				List<NoticeDetail> notices = noticeDao.noticeActivesBySupp(s.getAddresNumber());
				
				if(notices != null) {
					if(notices.size()>0) return "notice";
				}
			}

			if(s != null) {
				if(s.isSupplierWithoutOC()) {
					model.addAttribute("supplierWithoutOC", "TRUE");
				} else {
					model.addAttribute("supplierWithoutOC", "FALSE");
				}
			}
			
			if(s != null) {
				if(s.isSupplierWithOC()) {
					model.addAttribute("supplierWithOC", "TRUE");
				} else {
					model.addAttribute("supplierWithOC", "FALSE");
				}
			}			
			
			String pd = "";
			if(s != null) {
				if(s.isOutSourcing()) {
					if(!s.isOutSourcingAccept()) {
						model.addAttribute("addressNumber", s.getAddresNumber());
						model.addAttribute("name", s.getRazonSocial());
						model.addAttribute("message", "_START");
						return "outsourcingFiles";
					}else {
						if(!s.isOutSourcingMonthlyAccept()) {
							model.addAttribute("addressNumber", s.getAddresNumber());
							model.addAttribute("name", s.getRazonSocial());
							model.addAttribute("message", "_STARTMONTH");	
							return "outsourcingFiles";
						}
						
						if(!s.isOutSourcingBimonthlyAccept()) {
							model.addAttribute("addressNumber", s.getAddresNumber());
							model.addAttribute("name", s.getRazonSocial());
							model.addAttribute("message", "_STARTSECOND");	
							//model.addAttribute("monthLoad", getPeriodLoad());
							return "outsourcingFiles";
						}
						
						if(!s.isOutSourcingQuarterlyAccept()) {
							model.addAttribute("addressNumber", s.getAddresNumber());
							model.addAttribute("name", s.getRazonSocial());
							model.addAttribute("message", "_STARTQUARTER");	
							//model.addAttribute("monthLoad", getPeriodLoad());
							return "outsourcingFiles";
						}
					}
					
					String pendingDocs = outSourcingService.getPendingDocuments(s.getAddresNumber());
					if(!"".equals(pendingDocs)) {
						pd = pendingDocs;
					}
					model.addAttribute("osSupplier", "TRUE");
				}else {
					model.addAttribute("osSupplier", "FALSE");
				}
		     }
			
			model.addAttribute("pendingDocs", pd);
		}
		
		UDC udc = udcService.searchBySystemAndKey(AppConstants.WELCOME_FIELD, AppConstants.MESSAGE_FIELD);
		model.addAttribute("welcomeMessage", udc.getStrValue1());
		
		UDC udcEoYStart = udcService.searchBySystemAndKey("ENDOFYEAR", "START");
		UDC udcEoYEnd = udcService.searchBySystemAndKey("ENDOFYEAR", "END");
		if(udcEoYStart != null && udcEoYEnd != null) {
			SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			SimpleDateFormat lfmt = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es","ES"));
			try {
				Date startDate = fmt.parse(udcEoYStart.getStrValue1());
				Date endDate = fmt.parse(udcEoYEnd.getStrValue1());
				
				Date today = new Date();
				if(today.compareTo(startDate) > 0) {
					if(today.compareTo(endDate) < 0) {
						if(e.getRole().contains("SUPPLIER")) {
							model.addAttribute("startDate", lfmt.format(startDate));
							model.addAttribute("endDate",  lfmt.format(endDate));
							return "endOfYearPage";
						}
					}
				}
			} catch (ParseException e1) {
				log4j.error("Exception" , e1);
				return "home";
			}
		}else {
			return "home";
		}
		return "home";

	}
	
	@RequestMapping(value="/homeUnique.action",method = RequestMethod.GET)
	public String homeUnique(Model model, @RequestParam String userName){
		String usr = userName;	
		Users e = usersService.searchCriteriaUserName(usr);
		if(e!=null) {
			
			model.addAttribute("id", e.getId());
			model.addAttribute("name", e.getName());
			model.addAttribute("email", e.getEmail());
			model.addAttribute("userName", e.getUserName());
			model.addAttribute("role", e.getRole());
			model.addAttribute("type", e.getUserType().getUdcKey());
			model.addAttribute("userType", e.getUserType().getStrValue1());
			
			Supplier s = supplierService.searchByAddressNumber(e.getUserName());
			if(s != null) {
				model.addAttribute("invException", s.getInvException());
			}else {
				model.addAttribute("invException", "N");
			}
		}
		
		model.addAttribute("multipleRfc", "empty");
		
		UDC udc = udcService.searchBySystemAndKey(AppConstants.WELCOME_FIELD, AppConstants.MESSAGE_FIELD);
		model.addAttribute("welcomeMessage", udc.getStrValue1());
		return "home";

	}
	
	@RequestMapping(value="/changePassword.action",method = RequestMethod.POST)
	public String changePassword(Model model, @RequestParam String usrname,
			                     @RequestParam String psw){

		Users e = usersService.searchCriteriaUserName(usrname);
		e.setPassword(psw);
		e.setLogged(true);
		usersService.update(e, new Date(), "");
		model.addAttribute("name", e.getName());
		model.addAttribute("email", e.getEmail());
		model.addAttribute("userName", e.getUserName());
		//model.addAttribute("message", "_SUCCESS");
		return "login";
	}
	
	/*@RequestMapping(value="/acceptAgreement.action",method = RequestMethod.POST)
	public String acceptAgreement(Model model, @RequestParam String userName){
		Users e = usersService.searchCriteriaUserName(userName);
		usersService.updateAgreement(e);
		return "redirect:home.action";
	}*/
	@RequestMapping(value="/acceptAgreement.action",method = RequestMethod.POST)
	public String acceptAgreement(Model model, @RequestParam String access_token,
												@RequestParam String rfcValid,
												@RequestParam String razonSocialValid,
												@RequestParam String searchTypeValid){
		AccessTokenRegister reg = supplierService.searchByToken(access_token);
		if(reg != null) {
			reg.setAcceptConfidential(true); 
			//supplierService.updateAccessToken(reg);
			this.supplierDao.updateAccessToken(reg);
		}
		model.addAttribute("rfcValid", rfcValid);
		model.addAttribute("razonSocialValid", razonSocialValid);
		model.addAttribute("searchTypeValid", searchTypeValid);
		model.addAttribute("approvalFlowUserValid", reg.getApprovalFlowUser());
		
		return "newRegister";
	}
	
	@RequestMapping(value="/requestResetPassword.action",method = RequestMethod.GET)
	public String requestEmail(Model model){
		return "requestEmail";
	}
	
	@RequestMapping(value="/resetPassword.action",method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public String resetPassword(Model model, @RequestParam String usrname, RedirectAttributes redirectAttributes ){
		String response = usersService.resetPassword(usrname);
		String msg ="";
		if("".equals(usrname)) {
			msg = "Debe registrar una cuenta de usuario para el envío de su contraseña";
		}
		
		if("".equals(response)) {
			Users u = usersService.searchCriteriaUserName(usrname);
			String hiddenEmail = "";
			
			if(!StringUtils.isBlank(u.getEmail())) {
				String firstChar = u.getEmail().substring(0, 1);
				String hiddenSeq = u.getEmail().substring(1, u.getEmail().indexOf("@")).replaceAll(".", "*");
				String domain = u.getEmail().substring(u.getEmail().indexOf("@"));
				hiddenEmail = firstChar.concat(hiddenSeq).concat(domain);
			} else {
				msg = "No tiene un correo registrado.";
			}
			
			msg = "La solicitud ha sido enviada exitosamente a su cuenta registrada " + hiddenEmail + ". Cierre esta ventana.";
		}else {
			msg = response;
		}
		
		redirectAttributes.addFlashAttribute("msg", msg);
		return "redirect:/requestResetPassword.action";
	}
	
	@RequestMapping(value="/login",method = RequestMethod.GET)
	public String login(Model model){
		model.addAttribute("loginType", "_ALL");
		return "login";
	}
	
	@RequestMapping(value="/loginSupplier",method = RequestMethod.GET)
	public String loginSupplier(Model model){
		model.addAttribute("loginType", "_SUPPLIER");
		return "login";
	}
	
	@RequestMapping(value="/loginEmployee",method = RequestMethod.GET)
	public String loginEmployee(Model model){
		model.addAttribute("loginType", "_EMPLOYEE");
		return "login";
	}
	
	
	
	@RequestMapping(value="/unauthorized",method = RequestMethod.GET)
	public String HelloWorld(Model model){
		model.addAttribute("message", "ACCESO RESTRINGIDO");
		return "login";
	}

	@RequestMapping(value="/j_spring_security_logout", method = RequestMethod.GET)
	public String logoutPage (Model model, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
		model.addAttribute("loginType", "_ALL");
        return "login";
    }
	
	@RequestMapping(value="/invalidSessionPage", method = RequestMethod.GET)
	public String logout (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "invalidSession";
    }
	
	/*@RequestMapping(value="/newRegister",method = RequestMethod.GET)
	public String newRegister(Model model){
		return "newRegister";
	}*/
	
	@RequestMapping(value="/newRegister.action",method = RequestMethod.GET)
	public String newRegister(Model model, @RequestParam String access_token,
											@RequestParam String rfcValid,
											//@RequestParam String razonSocial,
											//@RequestParam String searchType,
											HttpServletRequest request){
		
		@SuppressWarnings("unused")
		Object session = request.getSession();
		AccessTokenRegister reg = supplierService.searchByToken(access_token);
		
		if(reg == null) {
			return "invalidToken";
		}else {
			if(reg.isAssigned() && !reg.isEnabled()) {
				model.addAttribute("access_token", access_token);
				model.addAttribute("rfcValid", rfcValid);
				model.addAttribute("razonSocialValid", reg.getRegisterName());
				model.addAttribute("searchTypeValid", reg.getSearchType());
				model.addAttribute("approvalFlowUserValid", reg.getApprovalFlowUser());
				return "newRegister";
				/*if(reg.isAcceptConfidential()) {
					return "newRegister";
				}else {
					return "acceptAgreement";
				}*/
				
			}else {
				return "invalidToken";
			}
		}
	}
	
	@RequestMapping(value ="/isAlive/ping.action")
	public @ResponseBody Map<String, Object> view(@RequestParam int start,
												  @RequestParam int limit,
												  @RequestParam String query){	

		return mapOK("OK");
		
	}
	
	@RequestMapping(value="/getLocalization.action",method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getLocalization(@RequestParam String lang){
		PropertiesLoader propertiesLoader = new PropertiesLoader();
		return mapOKLoc(propertiesLoader.getPropMap(lang));
	}
	
	
	public Map<String,Object> mapOK(String obj){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", 1);
		modelMap.put("data", obj);
		modelMap.put("success", true);
		return modelMap;
	}
	
	public Map<String,Object> mapOKLoc(Map<String, String> obj){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", 1);
		modelMap.put("data", obj);
		modelMap.put("success", true);
		return modelMap;
	}
	
}
