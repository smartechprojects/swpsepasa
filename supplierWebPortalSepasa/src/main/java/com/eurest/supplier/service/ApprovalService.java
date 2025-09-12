package com.eurest.supplier.service;

import com.eurest.supplier.dao.ApprovalDao;
import com.eurest.supplier.dto.SupplierDTO;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.SupplierDocument;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.StringUtils;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service("approvalService")
public class ApprovalService {
  @Autowired
  ApprovalDao approvalDao;
  
  @Autowired
  UdcService udcService;
  
  @Autowired
  EDIService EDIService;
  
  @Autowired
  EmailService emailService;
  
  @Autowired
  UsersService userService;
  
  @Autowired
  DocumentsService documentsService;
  
  @Autowired
  private JavaMailSender mailSenderObj;
  
  @Autowired
  StringUtils stringUtils;
  
  @Autowired
  DataAuditService dataAuditService;
  
  private Logger log4j = Logger.getLogger(ApprovalService.class);
  
  public List<SupplierDTO> getPendingApproval(String currentApprover, int start, int limit) {
    return this.approvalDao.getPendingApproval(currentApprover, start, limit);
  }
  
  
  public String updateSupplier(int id, String status, String step, String notes) {
    Supplier s = this.approvalDao.getSupplierById(id);
    String emailRecipient = s.getEmailSupplier();
    DataAudit dataAudit = new DataAudit();
	Date currentDate = new Date();
	
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	
	String usr = auth.getName(); 		
    if ("APROBADO".equals(status) && "FIRST".equals(step)) {
    	
      //String nextApprover = s.getNextApprover().trim();
      UDC udc = this.udcService.searchBySystemAndKey("APPROVER", "SECOND_APPROVER");
      //String nextApproverEmail = "noReply@universal.com";
      if (udc != null) {
          s.setCurrentApprover(udc.getStrValue1());
          s.setNextApprover(udc.getStrValue2());
        } else {
      	  UDC udc_default = this.udcService.searchBySystemAndKey("2ND_APPROVER_DEFAULT", "2ND_DEFAULT");
            
            if(udc_default!= null) {
          	    s.setCurrentApprover(udc_default.getStrValue1());
                s.setNextApprover(udc_default.getStrValue2());
            }else {
          	  s.setCurrentApprover("SECOND");
                s.setNextApprover("FINAL");
            }
        }
      
      s.setApprovalStep("SECOND");
      s.setApprovalStatus("PENDIENTE");
      s.setApprovalNotes(notes);
      this.approvalDao.updateSupplier(s);
      
      
        dataAudit.setAction("SupplierApproval");
  		dataAudit.setAddressNumber(s.getAddresNumber());
  		dataAudit.setCreationDate(currentDate);
  		dataAudit.setDocumentNumber(null);
  		dataAudit.setIp(request.getRemoteAddr());
  		dataAudit.setMethod("updateSupplier");
  		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
  		dataAudit.setOrderNumber(null);
  		dataAudit.setUuid(null);
  		dataAudit.setStep(AppConstants.FIRST_STEP);
  		dataAudit.setMessage("Supplier Approval Successful - Ticket: " + s.getTicketId());
  		dataAudit.setNotes(notes);
  		dataAudit.setStatus(AppConstants.STATUS_INPROCESS);
  		dataAudit.setUser(usr);
  		
  		dataAuditService.save(dataAudit);
  		
  		Users fstApprover = userService.getByUserName(s.getCurrentApprover());
        
        if(fstApprover != null) {
        	String emailContent = AppConstants.EMAIL_SECOND_APP_CONTENT;
      	    emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
      	    emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);

            EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
            emailAsyncSup.setProperties(AppConstants.EMAIL_SECOND_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), fstApprover.getEmail());
            emailAsyncSup.setMailSender(this.mailSenderObj);
            Thread emailThreadSup = new Thread(emailAsyncSup);
            emailThreadSup.start();
        }else {
        	String emailContent = AppConstants.EMAIL_NOFOUND_APP_CONTENT;
    		emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
    		emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
    		  
        	List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
	    	  if(emailnotif != null) {
	    		  for(UDC udcNotif : emailnotif) {
	    			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
	    	  	      emailAsyncPur.setProperties(AppConstants.EMAIL_NOFOUND_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), udcNotif.getStrValue1().trim());
	    	  	      emailAsyncPur.setMailSender(this.mailSenderObj);
	    	  	      Thread emailThreadPur = new Thread(emailAsyncPur);
	    	  	      emailThreadPur.start();
	    		  }
	    	  }
        	
        }
      
	  /*String emailContent = AppConstants.EMAIL_SECOND_APP_CONTENT;
	  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
	  emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);

      EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
      emailAsyncSup.setProperties(AppConstants.EMAIL_SECOND_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), nextApproverEmail);
      emailAsyncSup.setMailSender(this.mailSenderObj);
      Thread emailThreadSup = new Thread(emailAsyncSup);
      emailThreadSup.start();*/
     
      return "Success";
    } 
    if ("APROBADO".equals(status) && "SECOND".equals(step)) {
        //String nextApprover = s.getNextApprover().trim();
    	 UDC udc = this.udcService.searchBySystemAndKey("APPROVER", "THIRD_APPROVER");
         //String nextApproverEmail = "noReply@universal.com";
         if (udc != null) {
             s.setCurrentApprover(udc.getStrValue1());
             s.setNextApprover(udc.getStrValue2());
           } else {
         	  UDC udc_default = this.udcService.searchBySystemAndKey("3RD_APPROVER_DEFAULT", "3RD_DEFAULT");
               
               if(udc_default!= null) {
             	    s.setCurrentApprover(udc_default.getStrValue1());
                   s.setNextApprover(udc_default.getStrValue2());
               }else {
             	  s.setCurrentApprover("THIRD");
                   s.setNextApprover("FINAL");
               }
           }
        s.setApprovalStep("THIRD");
        s.setApprovalStatus("PENDIENTE");
        s.setApprovalNotes(notes);
        this.approvalDao.updateSupplier(s);
        
        dataAudit.setAction("SupplierApproval");
  		dataAudit.setAddressNumber(s.getAddresNumber());
  		dataAudit.setCreationDate(currentDate);
  		dataAudit.setDocumentNumber(null);
  		dataAudit.setIp(request.getRemoteAddr());
  		dataAudit.setMethod("updateSupplier");
  		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
  		dataAudit.setOrderNumber(null);
  		dataAudit.setUuid(null);
  		dataAudit.setStep(AppConstants.SECOND_STEP);
  		dataAudit.setMessage("Supplier Approval Successful - Ticket: " + s.getTicketId());
  		dataAudit.setNotes(notes);
  		dataAudit.setStatus(AppConstants.STATUS_INPROCESS);
  		dataAudit.setUser(usr);
  		
  		dataAuditService.save(dataAudit);
        
  		Users fstApprover = userService.getByUserName(s.getCurrentApprover());
        
        if(fstApprover != null) {
        	String emailContent = AppConstants.EMAIL_SECOND_APP_CONTENT;
      	    emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
      	    emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);

            EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
            emailAsyncSup.setProperties(AppConstants.EMAIL_THIRD_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), fstApprover.getEmail());
            emailAsyncSup.setMailSender(this.mailSenderObj);
            Thread emailThreadSup = new Thread(emailAsyncSup);
            emailThreadSup.start();
        }else {
        	String emailContent = AppConstants.EMAIL_NOFOUND_APP_CONTENT;
    		emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
    		emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
    		  
        	List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
	    	  if(emailnotif != null) {
	    		  for(UDC udcNotif : emailnotif) {
	    			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
	    	  	      emailAsyncPur.setProperties(AppConstants.EMAIL_NOFOUND_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), udcNotif.getStrValue1().trim());
	    	  	      emailAsyncPur.setMailSender(this.mailSenderObj);
	    	  	      Thread emailThreadPur = new Thread(emailAsyncPur);
	    	  	      emailThreadPur.start();
	    		  }
	    	  }
        	
        }
       
        return "Success";
      } 
    if ("APROBADO".equals(status) && "THIRD".equals(step)) {	// && "THIRD".equals(step)) {
      s.setCurrentApprover("FINAL");
      s.setNextApprover("FINAL");
      s.setApprovalStep("THIRD");
      s.setApprovalStatus("APROBADO");
      s.setApprovalNotes(notes);
      s.setFechaAprobacion(new Date());
      
      
      //if(s.getAddresNumber() == "" ||  "0".equals(s.getAddresNumber()) || s.getAddresNumber() == null) {
      if("".equals(s.getTipoMovimiento()) ) {		//if(s.getTipoMovimiento() == "") {
          s.setTipoMovimiento("A");
          //s.setCreditMessage("");
          //s.setSearchType("V");
          //s.setHold("");
          
          Supplier jdeS = EDIService.registerNewAddressBook(s);
          if(jdeS==null) {
              s.setAddresNumber(null);
              return "Error JDE";
          }else {
        	  s.setAddresNumber(jdeS.getAddresNumber());
        	  
        	  List<SupplierDocument> list = null;
        	  /*if(s.getTaxId() != null && !"".equals(s.getTaxId())) {
        		list =  this.documentsService.searchByAddressNumber("NEW_" + s.getTaxId());
        	  }else {
        		list =  this.documentsService.searchByAddressNumber("NEW_" + s.getRfc());
        	  }*/
        	  
        	  list =  this.documentsService.searchSupplierDocument(s.getAddresNumber());
        	  
              for (SupplierDocument d : list) {
                d.setAddressBook(jdeS.getAddresNumber());
                this.documentsService.updateSupplierDocument(d);
              } 
              
              log4j.info("***** ADDRESSNUMBER:" + s.getAddresNumber());
        	  UDC role = udcService.searchBySystemAndKey("ROLES", "SUPPLIER");
              UDC userType = udcService.searchBySystemAndKey("USERTYPE", "SUPPLIER");

              String yearTwoDigits = String.valueOf(LocalDate.now().getYear()).substring(2); // "25"
              String tempPass = "Sepasa." + yearTwoDigits;
        	  String encodePass = Base64.getEncoder().encodeToString(tempPass.trim().getBytes());
        	  encodePass = "==a20$" + encodePass; 
              Users u = new Users();
              u.setEmail(s.getEmailSupplier());
              u.setName(s.getRazonSocial());
              u.setPassword(encodePass);
              u.setUserName(s.getAddresNumber());
              u.setUserRole(role);
              u.setUserType(userType);
              u.setEnabled(true);
              userService.save(u, null, null);
              this.approvalDao.updateSupplier(s);
              
                dataAudit.setAction("SupplierApproval");
        		dataAudit.setAddressNumber(s.getAddresNumber());
        		dataAudit.setCreationDate(currentDate);
        		dataAudit.setDocumentNumber(null);
        		dataAudit.setIp(request.getRemoteAddr());
        		dataAudit.setMethod("updateSupplier");
        		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
        		dataAudit.setOrderNumber(null);
        		dataAudit.setUuid(null);
        		dataAudit.setStep(AppConstants.FINAL_STEP);
        		dataAudit.setMessage("Supplier Approval Successful - Ticket: " + s.getTicketId());
        		dataAudit.setNotes(notes);
        		dataAudit.setStatus(AppConstants.STATUS_ACCEPT);
        		dataAudit.setUser(usr);
        		
        		dataAuditService.save(dataAudit);
              
			  EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			
			  String emailContent = AppConstants.EMAIL_ACCEPT_SUPPLIER_NOTIFICATION;
			  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
			  emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
			  emailContent = emailContent.replace("_USER_", s.getAddresNumber());
			  emailContent = emailContent.replace("_PASS_", tempPass.trim());

			  emailAsyncSup.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_ACCEPT, stringUtils.prepareEmailContent(emailContent), s.getEmailSupplier());
			  emailAsyncSup.setMailSender(mailSenderObj);
			  Thread emailThreadSup = new Thread(emailAsyncSup);
			  emailThreadSup.start();	
			  
			  List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
        	  if(emailnotif != null) {
        		  for(UDC o : emailnotif) {
        			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
        		      emailAsyncPur.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_ACCEPT, this.stringUtils.prepareEmailContent("Estimado usuario <br /><br />El proveedor " + s.getAddresNumber() + " | " + s.getRazonSocial() + " fue aprobado en el portal de proveedores.<br><br>"), o.getStrValue1());
        		      emailAsyncPur.setMailSender(this.mailSenderObj);
        		      Thread emailThreadPur = new Thread(emailAsyncPur);
        		      emailThreadPur.start();
        		  }
        	  }
			  
              return "Success";
          }
  
      }else {
    	  
    	  s.setTipoMovimiento("C");
    	  /*s.setCreditMessage("");
          s.setSearchType("V");
          s.setHold("");*/
    	  long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
    	  long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
    	  long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));
    	
    	  s.setChangeTicket(String.valueOf(randomTicket));
    	  
    	  //EDIService.registerNewAddressBook(s);
    	  Supplier jdeS = EDIService.registerNewAddressBook(s);
          if(jdeS==null) {
              s.setAddresNumber(null);
              return "Error JDE";
          }else {
        	  Users u = userService.getByUserName(s.getAddresNumber());
        	  u.setEnabled(true);
        	  userService.update(u, null, null);
        	  
        	  this.approvalDao.updateSupplier(s);
        	  
        	dataAudit.setAction("SupplierApproval");
      		dataAudit.setAddressNumber(s.getAddresNumber());
      		dataAudit.setCreationDate(currentDate);
      		dataAudit.setDocumentNumber(null);
      		dataAudit.setIp(request.getRemoteAddr());
      		dataAudit.setMethod("updateSupplier");
      		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
      		dataAudit.setOrderNumber(null);
      		dataAudit.setUuid(null);
      		dataAudit.setStep(AppConstants.FINAL_STEP);
      		dataAudit.setMessage("Supplier Approval Successful - Ticket: " + s.getTicketId());
      		dataAudit.setNotes(notes);
      		dataAudit.setStatus(AppConstants.STATUS_ACCEPT);
      		dataAudit.setUser(usr);
      		
      		dataAuditService.save(dataAudit);
        	  
        	  String credentials = "Usuario: " + s.getAddresNumber() + "<br />&nbsp; url: " + AppConstants.EMAIL_PORTAL_LINK ;
    		  EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
    		  emailAsyncSup.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_ACCEPT, stringUtils.prepareEmailContent(AppConstants.EMAIL_MASS_SUPPLIER_CHANGE_NOTIFICATION + credentials), s.getEmailSupplier());
    		  emailAsyncSup.setMailSender(mailSenderObj);
    		  Thread emailThreadSup = new Thread(emailAsyncSup);
    		  emailThreadSup.start();
          }
          
          List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
    	  if(emailnotif != null) {
    		  for(UDC o : emailnotif) {
    			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
    		      emailAsyncPur.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_ACCEPT, this.stringUtils.prepareEmailContent("Estimado usuario <br /><br />El proveedor " + s.getAddresNumber() + " | " + s.getRazonSocial()  + " fue actualizado y aprobado en el portal de proveedores.<br><br>"), o.getStrValue1());
    		      emailAsyncPur.setMailSender(this.mailSenderObj);
    		      Thread emailThreadPur = new Thread(emailAsyncPur);
    		      emailThreadPur.start();
    		  }
    	  }
    	  
    	  return "Succ Update";
      }
      
    } 
    

    if ("RECHAZADO".equals(status)) {
      s.setCurrentApprover("REJECT");
      s.setNextApprover("REJECT");
      s.setApprovalStep("");
      s.setApprovalStatus("RENEW");
      s.setRejectNotes(notes);
      this.approvalDao.updateSupplier(s);
      
        dataAudit.setAction("SupplierApproval");
		dataAudit.setAddressNumber(s.getAddresNumber());
		dataAudit.setCreationDate(currentDate);
		dataAudit.setDocumentNumber(null);
		dataAudit.setIp(request.getRemoteAddr());
		dataAudit.setMethod("updateSupplier");
		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
		dataAudit.setOrderNumber(null);
		dataAudit.setUuid(null);
		dataAudit.setStep(AppConstants.FIRST_STEP);
		dataAudit.setMessage("Supplier Reject Successful - Ticket: " + s.getTicketId());
		dataAudit.setNotes(notes);
		dataAudit.setStatus(AppConstants.STATUS_REJECT);
		dataAudit.setUser(usr);
		
		dataAuditService.save(dataAudit);

      
      String emailContent = AppConstants.EMAIL_REJECT_SUPPLIER_NOTIFICATION;
		emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
		emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
		emailContent = emailContent.replace("_REASON_", notes);
      
      EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
      emailAsyncSup.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), emailRecipient);
      emailAsyncSup.setMailSender(this.mailSenderObj);
      Thread emailThreadSup = new Thread(emailAsyncSup);
      emailThreadSup.start();
      
      List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
	  if(emailnotif != null) {
		  for(UDC o : emailnotif) {
			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
		      emailAsyncPur.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), o.getStrValue1());
		      emailAsyncPur.setMailSender(this.mailSenderObj);
		      Thread emailThreadPur = new Thread(emailAsyncPur);
		      emailThreadPur.start();
		  }
	  }
      
      /*EmailServiceAsync emailAsyncSup2 = new EmailServiceAsync();
      emailAsyncSup2.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), s.getEmailComprador());
      emailAsyncSup2.setMailSender(this.mailSenderObj);
      Thread emailThreadSup2 = new Thread(emailAsyncSup2);
      emailThreadSup2.start();*/
     
      return "Rejected";
    } 
    /*if ("RECHAZADO".equals(status) && "SECOND".equals(step)) {
      s.setCurrentApprover("");
      s.setNextApprover("");
      s.setApprovalStep("SECOND");
      s.setApprovalStatus("RENEW");
      s.setRejectNotes(notes);
      this.approvalDao.updateSupplier(s);
      
        dataAudit.setAction("SupplierApproval");
		dataAudit.setAddressNumber(s.getAddresNumber());
		dataAudit.setCreationDate(currentDate);
		dataAudit.setDocumentNumber(null);
		dataAudit.setIp(request.getRemoteAddr());
		dataAudit.setMethod("updateSupplier");
		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
		dataAudit.setOrderNumber(null);
		dataAudit.setUuid(null);
		dataAudit.setStep(AppConstants.SECOND_STEP);
		dataAudit.setMessage("Supplier Reject Successful - Ticket: " + s.getTicketId());
		dataAudit.setNotes(notes);
		dataAudit.setStatus(AppConstants.STATUS_REJECT);
		dataAudit.setUser(usr);
		
		dataAuditService.save(dataAudit);

      String emailContent = AppConstants.EMAIL_REJECT_SUPPLIER_NOTIFICATION;
		emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
		emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
		emailContent = emailContent.replace("_REASON_", notes);
      
      EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
      emailAsyncSup.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), emailRecipient);
      emailAsyncSup.setMailSender(this.mailSenderObj);
      Thread emailThreadSup = new Thread(emailAsyncSup);
      emailThreadSup.start();
      
      EmailServiceAsync emailAsyncSup2 = new EmailServiceAsync();
      emailAsyncSup2.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), s.getEmailComprador());
      emailAsyncSup2.setMailSender(this.mailSenderObj);
      Thread emailThreadSup2 = new Thread(emailAsyncSup2);
      emailThreadSup2.start();
      
      return "Rejected";
    } 
    if ("RECHAZADO".equals(status) && "THIRD".equals(step)) {
        s.setCurrentApprover("");
        s.setNextApprover("");
        s.setApprovalStep("THIRD");
        s.setApprovalStatus("RENEW");
        s.setRejectNotes(notes);
        this.approvalDao.updateSupplier(s);
        
        dataAudit.setAction("SupplierApproval");
		dataAudit.setAddressNumber(s.getAddresNumber());
		dataAudit.setCreationDate(currentDate);
		dataAudit.setDocumentNumber(null);
		dataAudit.setIp(request.getRemoteAddr());
		dataAudit.setMethod("updateSupplier");
		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
		dataAudit.setOrderNumber(null);
		dataAudit.setUuid(null);
		dataAudit.setStep(AppConstants.FINAL_STEP);
		dataAudit.setMessage("Supplier Reject Successful - Ticket: " + s.getTicketId());
		dataAudit.setNotes(notes);
		dataAudit.setStatus(AppConstants.STATUS_REJECT);
		dataAudit.setUser(usr);
		
		dataAuditService.save(dataAudit);

        String emailContent = AppConstants.EMAIL_REJECT_SUPPLIER_NOTIFICATION;
  		emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
  		emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
  		emailContent = emailContent.replace("_REASON_", notes);
        
        EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
        emailAsyncSup.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), emailRecipient);
        emailAsyncSup.setMailSender(this.mailSenderObj);
        Thread emailThreadSup = new Thread(emailAsyncSup);
        emailThreadSup.start();
        
        EmailServiceAsync emailAsyncSup2 = new EmailServiceAsync();
        emailAsyncSup2.setProperties(AppConstants.EMAIL_NEW_SUPPLIER_REJECT, this.stringUtils.prepareEmailContent(emailContent), s.getEmailComprador());
        emailAsyncSup2.setMailSender(this.mailSenderObj);
        Thread emailThreadSup2 = new Thread(emailAsyncSup2);
        emailThreadSup2.start();
        
        return "Rejected";
      } */
    return "";
  }
  
	public List<SupplierDTO> searchApproval(String ticketId,
			String approvalStep,
			String approvalStatus,
			Date fechaAprobacion,
			String currentApprover,
			String name,
            int start,
            int limit) {
		return approvalDao.searchApproval(ticketId, approvalStep, approvalStatus, fechaAprobacion, currentApprover, name, start, limit);
	}
	
	  public String reasignApprover(int id, String newApprover) {		  
	      Users u = userService.getByUserName(newApprover);
	      String pastApprover = "";
	      DataAudit dataAudit = new DataAudit();
	      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	      HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	      String usr = auth.getName();
	      
	      if(u != null) {
	    	  
		      Supplier s = this.approvalDao.getSupplierById(id);
		      pastApprover = s.getCurrentApprover();
		      s.setCurrentApprover(newApprover);
		      approvalDao.updateSupplier(s);
		      
		      String emailContent = AppConstants.EMAIL_REASIGN_CONTENT;
	    	  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(s.getTicketId()));
	    	  emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);

	          EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	          emailAsyncSup.setProperties(AppConstants.EMAIL_REASIGN_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), u.getEmail());
	          emailAsyncSup.setMailSender(this.mailSenderObj);
	          Thread emailThreadSup = new Thread(emailAsyncSup);
	          emailThreadSup.start();
	          
	            dataAudit.setAction("SupplierReasignApprover");
	    		dataAudit.setAddressNumber(s.getAddresNumber());
	    		dataAudit.setCreationDate(new Date());
	    		dataAudit.setDocumentNumber(null);
	    		dataAudit.setIp(request.getRemoteAddr());
	    		dataAudit.setMethod("reasignApprover");
	    		dataAudit.setModule(AppConstants.APPROVALSEARCH_MODULE);    	
	    		dataAudit.setOrderNumber(null);
	    		dataAudit.setUuid(null);
	    		dataAudit.setStep(s.getApprovalStep());
	    		dataAudit.setMessage("Supplier Reasign Approver Successful - Ticket: " + s.getTicketId());
	    		dataAudit.setNotes("Past Approver: " + pastApprover + " - New Approver: " + newApprover);
	    		dataAudit.setStatus(s.getApprovalStatus());
	    		dataAudit.setUser(usr);
	    		
	    		dataAuditService.save(dataAudit);
	      }
	    
		    return "success";
	  }
}
