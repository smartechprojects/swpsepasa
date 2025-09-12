package com.eurest.supplier.service;

import com.eurest.supplier.dao.CodigosPostalesDao;
import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.dto.SupplierDTO;
import com.eurest.supplier.model.AccessTokenRegister;
import com.eurest.supplier.model.CodigosPostales;
import com.eurest.supplier.model.DataAudit;
import com.eurest.supplier.model.NonComplianceSupplier;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.SupplierDocument;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.ApplicationContextProvider;
import com.eurest.supplier.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service("supplierService")
public class SupplierService {
  @Autowired
  JavaMailSender mailSenderObj;
  
  @Autowired
  SupplierDao supplierDao;
  
  @Autowired
  UdcService udcService;
  
  @Autowired
  UsersService usersService;
  
  @Autowired
  CodigosPostalesDao codigosPostalesDao;
  
  @Autowired
  DocumentsService documentsService;
  
  @Autowired
  EmailService emailService;
  
  @Autowired
  NextNumberService nextNumberService;
  
  @Autowired
  JDERestService jDERestService;
  
  @Autowired
  StringUtils stringUtils;
  
  @Autowired
  EDIService EDIService;
  
  @Autowired
  NonComplianceSupplierService nonComplianceSupplierService;
  
  @Autowired
  DataAuditService dataAuditService;  
  
  Logger log4j = Logger.getLogger(SupplierService.class);
  
  private final String SECRET = "aink_45$11SecKey";
  
  public Supplier getSupplierById(int id) {
    Supplier s = this.supplierDao.getSupplierById(id);
   /* if (!"APROBADO".equals(s.getApprovalStatus()) 
    	&& ( "0".equals(s.getAddresNumber()) || s.getAddresNumber() == null || s.getAddresNumber() == ""))
    	s.addresNumber(""); */
    
    if(s != null) {
    	if("".equals(s.getAddresNumber()) ||  "0".equals(s.getAddresNumber()) || s.getAddresNumber() == null) {
    	}
    	else{
    		List<SupplierDocument> docs = documentsService.searchSupplierDocument(s.getAddresNumber());
        	if(docs != null) {
        		s.setFileList("");
        		String newFileList = "";
        		for(SupplierDocument doc : docs) {
        			//if(!"Factura".equals(doc.getFiscalType())) {
        			if(!"OP".equals(doc.getDocumentType())) {
        				newFileList = newFileList + "_FILE:" + doc.getId() + "_:_" + doc.getName() + "_:_" + doc.getUploadDate();
        			}
        		}
        		s.setFileList(newFileList);
        	}
    	}
    }
    return s;
  }
  
  public List<SupplierDTO> getList(int start, int limit) {
    return this.supplierDao.getList(start, limit);
  }
  
  
  @SuppressWarnings("unused")
public void sendAddressBookTest() {
	  try {
	    Supplier s =  this.supplierDao.getSupplierById(32);
	    Supplier jdeS = EDIService.registerNewAddressBook(s);
	    int i = 0;
	  }catch(Exception e) {
		  log4j.error("Exception" , e);
		  e.printStackTrace();
	  }
	    
  }
  
  public List<SupplierDTO> searchByCriteria(String query, int start, int limit) {
    return this.supplierDao.searchByCriteria(query, start, limit);
  }
  
  public List<SupplierDTO> getSuppliersByFilter(String query) {
	    return this.supplierDao.getSuppliersByFilter(query);
	  }
  
  public Supplier searchByAddressNumber(String addressNumber) {
    return this.supplierDao.searchByAddressNumber(addressNumber);
  }
  
  public Supplier searchByApprover(String currentApprover) {
    return this.supplierDao.searchByApprover(currentApprover);
  }
  
  public long updateSupplier(Supplier o) {
		
    NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(o.getRfc(), 0, 0);
    DataAudit dataAudit = new DataAudit();
	Date currentDate = new Date();
	
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	
	String usr = auth.getName(); 	
		Users usuarioLogueado=usersService.getByUserName(usr);
	try {
			if(AppConstants.ANONYMOUS_USER.equals(usr)) {
				log4j.info("Usuario: " + AppConstants.ANONYMOUS_USER);
				String ustAtrb = (String) request.getSession().getAttribute("userAuth");
				if(ustAtrb==null) {
					log4j.info("Atributo userAuth no encontrado en session");
					usr = auth.getName();
				}else {
					log4j.info("Atributo userAuth encontrado en session: " + ustAtrb);
					usr = ustAtrb;
				}		
			}
		}catch(Exception ex) {
			log4j.error("Excepción al obtener usuario: " + ex);
			usr = auth.getName();
		}
	
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
      o.setTicketId(Long.valueOf(-1L));
      return -1;
    } 
    //GL Class - Validación de refuerzo
	/*if(o.getCountry() != null && "MX".equals(o.getCountry().toUpperCase().trim())) {
		o.setGlClass("100");
	}else {
		o.setGlClass("200");
	}*/
    if(o.getTicketId() != null) {
	    long currentTicket = o.getTicketId();
    	if (currentTicket == 0L) {
    		long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
    		long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
    		long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));

    	      /*long leftLimit = 1L;
    	      long rightLimit = 1000000000000L;
    	      long randomTicket = leftLimit + (long)(Math.random() * rightLimit);*/
    	      o.setTicketId(Long.valueOf(randomTicket));
    	    }
    }else {
    	long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
		long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
		long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));
    	/*long leftLimit = 1L;
	    long rightLimit = 1000000000000L;
	    long randomTicket = leftLimit + (long)(Math.random() * rightLimit);*/
	    o.setTicketId(Long.valueOf(randomTicket));
    }
     
    String docReference = "";
    o.setBatchNumber("");
    
    if (o.getTaxId() != null && !"".equals(o.getTaxId())) {
    	docReference = o.getTaxId(); 
    }else {
    	docReference = o.getRfc();
    }
    
    if (o.getId() == 0 && "DRAFT".equals(o.getApprovalStatus())) {
      List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
      StringBuilder fileList = new StringBuilder();
      for (SupplierDocument d : list) {
        if(!"OP".equals(d.getDocumentType())) {
        	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
        }
        //this.documentsService.update(d, new Date(), "admin");
      } 
  	  o.setEmailContactoPedidos(o.getEmailSupplier());
  	  o.setName(o.getRazonSocial());
      o.setFileList(fileList.toString());
      o.setFechaSolicitud(new Date());
      //o.setSearchType("V");
	  //o.setCreditMessage("");
	  //o.setTaxAreaCxC("");
	  //o.setTaxExpl2CxC("");
	  //o.setPmtTrmCxC("N60");
	  //o.setPayInstCxC("T");
	  //o.setCurrCodeCxC("USD");
	  //if(o.getCatCode15() == "0" || o.getCatCode15() == null) o.setCatCode15("");
	  //if(o.getIndustryClass() == "0" || o.getIndustryClass() == null) o.setIndustryClass("");
	  if(o.getEstado() == "0" || o.getEstado()== null) o.setEstado("");
	  
      EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
      emailAsyncPur.setProperties("SEPASA - Registro como borrador en Alta de Proveedor. Ticket " + o.getTicketId(), this.stringUtils.prepareEmailContent("Estimado proveedor <br /><br />Hemos recibido una solicitud con carde borrador en nuestros sistemas. Si solicitud serÃ  procesada una vez que someta el formato de forma definitiva. <br /> <br /> Puede continuar actualizando sus datos utilizando el nde ticket que le enviamos a continuaciÃ²n " + o.getTicketId() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK), o.getEmailSupplier());
      emailAsyncPur.setMailSender(this.mailSenderObj);
      Thread emailThreadPur = new Thread(emailAsyncPur);
      emailThreadPur.start();
      
    } else if (o.getId() != 0 && "NEW".equals(o.getApprovalStatus()) || 
    	o.getId() != 0 && "RENEW".equals(o.getApprovalStatus())) {
    	
      String emailRecipient = o.getEmailSupplier();
      String reference = o.getEmailComprador().toLowerCase();
      
      UDC udc = this.udcService.searchBySystemAndKey("APPROVER", "FIRST_APPROVER");
      if (udc != null) {
          o.setCurrentApprover(udc.getStrValue1());
          o.setNextApprover(udc.getStrValue2());
        } else {
      	  UDC udc_default = this.udcService.searchBySystemAndKey("1ST_APPROVER_DEFAULT", "1ST_DEFAULT");
            
            if(udc_default!= null) {
          	    o.setCurrentApprover(udc_default.getStrValue1());
                o.setNextApprover(udc_default.getStrValue2());
            }else {
          	  o.setCurrentApprover("FIRST");
                o.setNextApprover("SECOND");
            }
        }
        
      Users fstApprover = usersService.getByUserName(o.getCurrentApprover());
      
      if(fstApprover != null) {
      	String emailContent = AppConstants.EMAIL_FIRST_APP_CONTENT;
	  	    emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
	  	    emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
	  	  
	        EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
	        emailAsyncPur.setProperties(AppConstants.EMAIL_FIRST_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), fstApprover.getEmail());
	        emailAsyncPur.setMailSender(this.mailSenderObj);
	        Thread emailThreadPur = new Thread(emailAsyncPur);
	        emailThreadPur.start();
      }
      
      StringBuilder fileList = new StringBuilder();
      
      if("".equals(o.getAddresNumber()) ||  "0".equals(o.getAddresNumber()) || o.getAddresNumber() == null) {
    	  List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
	      for (SupplierDocument d : list) {
	    	  if(!"OP".equals(d.getDocumentType())) {
	          	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
	          }
	        //this.documentsService.update(d, new Date(), "admin");
	      } 
      }else {
    	  List<SupplierDocument> listOld = this.documentsService.searchSupplierDocument(o.getAddresNumber());
	        for (SupplierDocument d : listOld) {
	        	if(!"OP".equals(d.getDocumentType())) {
	            	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
	            }
	          //this.documentsService.update(d, new Date(), "admin");
	        } 
      }
      
      o.setApprovalStep("FIRST");
      o.setApprovalStatus("PENDIENTE");
      o.setApprovalNotes("");
      o.setRejectNotes("");
      o.setEmailContactoPedidos(o.getEmailSupplier());
  	  o.setName(o.getRazonSocial());
      //o.setFileList(fileList.toString());
      o.setFechaSolicitud(new Date());
      /*o.setSearchType("V");
	  o.setCreditMessage("");
	  o.setTaxAreaCxC("");
	  o.setTaxExpl2CxC("");
	  //o.setPmtTrmCxC("N60");
	  o.setPayInstCxC("T");
	  o.setCurrCodeCxC("USD");
	  if(o.getCatCode15() == "0" || o.getCatCode15() == null) o.setCatCode15("");
	  if(o.getIndustryClass() == "0" || o.getIndustryClass() == null) o.setIndustryClass("");*/
	  if(o.getEstado() == "0" ||o.getEstado()== null) o.setEstado("");
	  
      o.setFileList(fileList.toString());
     
      EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
      String emailContent = AppConstants.EMAIL_REQUEST_RECEIVED_CONTENT;
      emailContent  = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
      
      emailAsyncSup.setProperties(AppConstants.EMAIL_REQUEST_RECEIVED_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), emailRecipient);
      emailAsyncSup.setMailSender(this.mailSenderObj);
      Thread emailThreadSup = new Thread(emailAsyncSup);
      emailThreadSup.start();
    
      /*emailContent = AppConstants.EMAIL_FIRST_APP_CONTENT;
	  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
	  emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
	  
      EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
      emailAsyncPur.setProperties(AppConstants.EMAIL_FIRST_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), o.getEmailComprador());
      emailAsyncPur.setMailSender(this.mailSenderObj);
      Thread emailThreadPur = new Thread(emailAsyncPur);
      emailThreadPur.start();*/
    
    
    
    }else if (o.getId() != 0 && "PENDIENTE".equals(o.getApprovalStatus())) {
    	
    	StringBuilder fileList = new StringBuilder();
    	
    	if("".equals(o.getAddresNumber()) ||  "0".equals(o.getAddresNumber()) || o.getAddresNumber() == null) {
      	  List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
  	      for (SupplierDocument d : list) {
  	    	if(!"OP".equals(d.getDocumentType())) {
  	        	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
  	        }	        
  	      } 
        }else {
      	  List<SupplierDocument> listOld = this.documentsService.searchSupplierDocument(o.getAddresNumber());
  	        for (SupplierDocument d : listOld) {
  	        	if(!"OP".equals(d.getDocumentType())) {
  	          	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
  	          }
  	        } 
        }
      o.setFileList(fileList.toString());
    }else if(o.getId() != 0 && "APROBADO".equals(o.getApprovalStatus())) {
    	
    	//boolean banderaDamin=usuarioLogueado.getRole().equals("ROLE_ADMIN");
    	String rol = usuarioLogueado.getRole().toUpperCase();

    	boolean banderaAdmin = rol.equals("ROLE_ADMIN") || rol.equals("ROLE_TAX");

    	
    	if (!banderaAdmin) {
		
    		Users u = usersService.getByUserName(o.getAddresNumber());
        	
        	if (u!=null) {
    			u.setEnabled(false);
    	    	u.setEmail(o.getEmailSupplier());
    	    	usersService.update(u, null, null);
    		}
        	
        	String reference = o.getEmailComprador().toLowerCase();
		
        	UDC udc = this.udcService.searchBySystemAndKey("APPROVER", "FIRST_APPROVER");
  	        if (udc != null) {
  	          o.setCurrentApprover(udc.getStrValue1());
  	          o.setNextApprover(udc.getStrValue2());
  	        } else {
  	      	  UDC udc_default = this.udcService.searchBySystemAndKey("1ST_APPROVER_DEFAULT", "1ST_DEFAULT");
  	            
  	            if(udc_default!= null) {
  	          	  o.setCurrentApprover(udc_default.getStrValue1());
  	                o.setNextApprover(udc_default.getStrValue2());
  	            }else {
  	          	  o.setCurrentApprover("FIRST");
  	                o.setNextApprover("SECOND");
  	            }
  	        } 
	        
	        o.setApprovalStep("FIRST");
	        o.setApprovalStatus("PENDIENTE");
	        o.setApprovalNotes("");
	        o.setRejectNotes("");
	        o.setFechaAprobacion(new Date());
	        o.setEmailContactoPedidos(o.getEmailSupplier());
	        
	        Users fstApprover = usersService.getByUserName(o.getCurrentApprover());
	        
	        if(fstApprover != null) {
	        	String emailContent = AppConstants.EMAIL_FIRST_APP_CONTENT;
		  	    emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
		  	    emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
		  	  
		        EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
		        emailAsyncPur.setProperties(AppConstants.EMAIL_FIRST_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), fstApprover.getEmail());
		        emailAsyncPur.setMailSender(this.mailSenderObj);
		        Thread emailThreadPur = new Thread(emailAsyncPur);
		        emailThreadPur.start();
	        }
    	}else {
    		if(usuarioLogueado.getRole().equals("ROLE_TAX")) {
        		long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
    	    	  long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
    	    	  long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));
    	    	
    	    	  o.setTipoMovimiento("C");
    	    	  o.setChangeTicket(String.valueOf(randomTicket));
    	    	  
    	    	  Supplier jdeS = EDIService.registerNewAddressBook(o);
    	          if(jdeS==null) {
    	        	  return -2L;
    	          }else {
    	        	  List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
    	        	  if(emailnotif != null) {
    	        		  for(UDC x : emailnotif) {
    	        			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
    	        			  emailAsyncPur.setProperties("SEPASA - Proveedor actualizado", this.stringUtils.prepareEmailContent("Estimado usuario <br /><br />Los codigos de categoria del proveedor " + o.getAddresNumber() + " | " + o.getRazonSocial() + " fue actualizado por el usuario " + usuarioLogueado.getUserName() +"<br><br>"), x.getStrValue1());
    	        		      emailAsyncPur.setMailSender(this.mailSenderObj);
    	        		      Thread emailThreadPur = new Thread(emailAsyncPur);
    	        		      emailThreadPur.start();
    	        		  }
    	        	  }
    	        	  
    	        	  DataAudit dataAuditUpdate = new DataAudit();
    	        	  
    	        	  	dataAuditUpdate.setAction("UpdateTaxDara");
    	            	dataAuditUpdate.setAddressNumber(o.getAddresNumber());
    	            	dataAuditUpdate.setCreationDate(currentDate);
    	            	dataAuditUpdate.setDocumentNumber(null);
    	            	dataAuditUpdate.setIp(request.getRemoteAddr());
    	            	dataAuditUpdate.setMessage("Update Tax Data Successful");
    	            	dataAuditUpdate.setMethod("updateSupplier");
    	            	dataAuditUpdate.setModule(AppConstants.SUPPLIER_MODULE);
    	            	dataAuditUpdate.setNotes(null);
    	            	dataAuditUpdate.setOrderNumber(null);
    	            	dataAuditUpdate.setStatus(AppConstants.STATUS_COMPLETE);
    	            	dataAuditUpdate.setStep(null);
    	            	dataAuditUpdate.setUser(usuarioLogueado.getUserName());
    	            	dataAuditUpdate.setUuid(null);
    	            	
    	            	dataAuditService.save(dataAuditUpdate);
    	      		  
    	          }
        	}else {
        		Users u = usersService.getByUserName(o.getAddresNumber());
        		
       		 if(u != null) {
      			  u.setEmail(o.getEmailSupplier());
      			  u.setName(o.getRazonSocial());
      			  usersService.update(u, null, null);
      		  }
        	}
    		
    	}
    	
        
        o.setName(o.getRazonSocial());
        
        List<SupplierDocument> listOld = this.documentsService.searchSupplierDocument(o.getAddresNumber());
        StringBuilder fileList = new StringBuilder();
        for (SupplierDocument d : listOld) {
        	if(!"OP".equals(d.getDocumentType())) {
            	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
            }
          //this.documentsService.update(d, new Date(), "admin");
        } 
        o.setFileList(fileList.toString());
        /*
        List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
        for (SupplierDocument d : list) {
          if (!"".equals(o.getAddresNumber())) d.setAddressBook(o.getAddresNumber()); 
          fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getSize());
          //d.setAddressBook(o.getAddresNumber());
          this.documentsService.update(d, new Date(), "admin");
        } 
        o.setFileList(fileList.toString());*/
    } 
    this.supplierDao.updateSupplier(o);
       
        dataAudit.setAction("UpdateSupplier");
		dataAudit.setAddressNumber(o.getAddresNumber());
		dataAudit.setCreationDate(currentDate);
		dataAudit.setDocumentNumber(null);
		dataAudit.setIp(request.getRemoteAddr());
		dataAudit.setMethod("updateSupplier");
		dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
		dataAudit.setOrderNumber(null);
		dataAudit.setUuid(null);
		dataAudit.setStep(null);
		dataAudit.setMessage("Supplier Updated Successful - Ticket: " + o.getTicketId());
		dataAudit.setNotes(o.getApprovalNotes());
		dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
		dataAudit.setUser(usr);
		
		dataAuditService.save(dataAudit);
    
    return o.getTicketId();
  }
  
  public long saveSupplier(Supplier o) {
    NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(o.getRfc(), 0, 0);
    DataAudit dataAudit = new DataAudit();
    Date currentDate = new Date();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
			
	String usr = auth.getName();
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
      o.setTicketId(Long.valueOf(-1L));
      return -1L;
    }
    //GL Class - Validación de refuerzo
	/*if(o.getCountry() != null && "MX".equals(o.getCountry().toUpperCase().trim())) {
		o.setGlClass("100");
	}else {
		o.setGlClass("200");
	}*/
    o.setFechaSolicitud(new Date());
    o.setCurrentApprover("");
    //o.setAddresNumber("");
    String docReference = "";
    if (o.getTaxId() != null && !"".equals(o.getTaxId())) {
    	docReference = o.getTaxId(); 
    }else {
    	docReference = o.getRfc();
    } 
    long currentTicket = o.getTicketId().longValue();
    if (currentTicket == 0L) {
    	long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
		long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
		long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));
      /*long leftLimit = 1L;
      long rightLimit = 1000000000000L;
      long randomTicket = leftLimit + (long)(Math.random() * rightLimit);*/
      o.setTicketId(Long.valueOf(randomTicket));
    } 
    
    if ("DRAFT".equals(o.getApprovalStatus())) {
      o.setBatchNumber("");
      List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
      StringBuilder fileList = new StringBuilder();
      for (SupplierDocument d : list) {
    	  if(!"OP".equals(d.getDocumentType())) {
          	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
          }
        //this.documentsService.update(d, new Date(), "admin");
      } 
      o.setEmailContactoPedidos(o.getEmailSupplier());
  	  o.setName(o.getRazonSocial());
      o.setFileList(fileList.toString());
      o.setFechaSolicitud(new Date());
      /*o.setSearchType("V");
	  o.setCreditMessage("");
	  o.setTaxAreaCxC("");
	  o.setTaxExpl2CxC("");
	  //o.setPmtTrmCxC("N60");
	  o.setPayInstCxC("T");
	  o.setCurrCodeCxC("USD");
	  if(o.getCatCode15() == "0" || o.getCatCode15() == null) o.setCatCode15("");
	  if(o.getIndustryClass() == "0" || o.getIndustryClass() == null) o.setIndustryClass("");*/
	  if(o.getEstado() == "0" ||o.getEstado()== null) o.setEstado("");
      EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
      emailAsyncPur.setProperties("SEPASA - Registro como borrador en Alta de Proveedor. Ticket " + o.getTicketId(), this.stringUtils.prepareEmailContent("Estimado proveedor <br /><br />Hemos recibido una solicitud con carde borrador en nuestros sistemas. Si solicitud serprocesada una vez que someta el formato de forma definitiva. <br /> <br /> Puede continuar actualizando sus datos utilizando el nde ticket que le enviamos a continuaci" + o.getTicketId() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK), o.getEmailSupplier());
      emailAsyncPur.setMailSender(this.mailSenderObj);
      Thread emailThreadPur = new Thread(emailAsyncPur);
      emailThreadPur.start();
    } else {
      String emailRecipient = o.getEmailSupplier();
      String reference = o.getEmailComprador().toLowerCase();
      
      UDC udc = this.udcService.searchBySystemAndKey("APPROVER", "FIRST_APPROVER");
        if (udc != null) {
          o.setCurrentApprover(udc.getStrValue1());
          o.setNextApprover(udc.getStrValue2());
        } else {
      	  UDC udc_default = this.udcService.searchBySystemAndKey("1ST_APPROVER_DEFAULT", "1ST_DEFAULT");
            
            if(udc_default!= null) {
          	  o.setCurrentApprover(udc_default.getStrValue1());
                o.setNextApprover(udc_default.getStrValue2());
            }else {
          	  o.setCurrentApprover("FIRST");
                o.setNextApprover("SECOND");
            }
        } 
        
        Users fstApprover = usersService.getByUserName(o.getCurrentApprover());
        
        if(fstApprover != null) {
        	String emailContent = AppConstants.EMAIL_FIRST_APP_CONTENT;
	  	    emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
	  	    emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
	  	  
	        EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
	        emailAsyncPur.setProperties(AppConstants.EMAIL_FIRST_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), fstApprover.getEmail());
	        emailAsyncPur.setMailSender(this.mailSenderObj);
	        Thread emailThreadPur = new Thread(emailAsyncPur);
	        emailThreadPur.start();
        }
      
      o.setApprovalStep("FIRST");
      o.setApprovalStatus("PENDIENTE");
      o.setApprovalNotes("");
      o.setRejectNotes("");
      o.setEmailContactoPedidos(o.getEmailSupplier());
  	  o.setName(o.getRazonSocial());
      o.setFechaSolicitud(new Date());
      /*o.setSearchType("V");
	  o.setCreditMessage("");
	  o.setTaxAreaCxC("");
	  o.setTaxExpl2CxC("");
	  //o.setPmtTrmCxC("N60");
	  o.setPayInstCxC("T");
	  o.setCurrCodeCxC("USD");
	  if(o.getCatCode15() == "0" || o.getCatCode15() == null) o.setCatCode15("");
	  if(o.getIndustryClass() == "0" || o.getIndustryClass() == null) o.setIndustryClass("");*/
      //int addressNumber = this.jDERestService.getAddressBookNextNumber();
      //o.setAddresNumber(String.valueOf(addressNumber));
     
      EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
      String emailContent = AppConstants.EMAIL_REQUEST_RECEIVED_CONTENT;
	  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
      
      emailAsyncSup.setProperties(AppConstants.EMAIL_REQUEST_RECEIVED_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), emailRecipient);
      emailAsyncSup.setMailSender(this.mailSenderObj);
      Thread emailThreadSup = new Thread(emailAsyncSup);
      emailThreadSup.start();
      
      /*emailContent = AppConstants.EMAIL_FIRST_APP_CONTENT;
	  emailContent = emailContent.replace("_NUMTICKET_", String.valueOf(o.getTicketId()));
	  emailContent = emailContent.replace("_URL_", AppConstants.EMAIL_PORTAL_LINK);
	  
      EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
      emailAsyncPur.setProperties(AppConstants.EMAIL_FIRST_APP_SUBJECT, this.stringUtils.prepareEmailContent(emailContent), o.getEmailComprador());
      emailAsyncPur.setMailSender(this.mailSenderObj);
      Thread emailThreadPur = new Thread(emailAsyncPur);
      emailThreadPur.start();*/

      StringBuilder fileList = new StringBuilder();
      
      if("".equals(o.getAddresNumber()) ||  "0".equals(o.getAddresNumber()) || o.getAddresNumber() == null) {
    	  List<SupplierDocument> list = this.documentsService.searchSupplierDocument("NEW_" + docReference);
	      for (SupplierDocument d : list) {
	    	  if(!"OP".equals(d.getDocumentType())) {
	          	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
	          }
	        //this.documentsService.update(d, new Date(), "admin");
	      } 
      }else {
    	  List<SupplierDocument> listOld = this.documentsService.searchSupplierDocument(o.getAddresNumber());
	        for (SupplierDocument d : listOld) {
	        	if(!"OP".equals(d.getDocumentType())) {
	            	fileList.append("_FILE:" + d.getId() + "_:_" + d.getName() + "_:_" + d.getUploadDate());
	            }
	          //this.documentsService.update(d, new Date(), "admin");
	        } 
      }
      
      o.setFileList(fileList.toString());
    } 
    //o.setDiasCredito(null);
    this.supplierDao.saveSupplier(o);
    
    dataAudit.setAction("SaveSupplier");
	dataAudit.setAddressNumber(o.getAddresNumber());
	dataAudit.setCreationDate(currentDate);
	dataAudit.setDocumentNumber(null);
	dataAudit.setIp(request.getRemoteAddr());
	dataAudit.setMethod("saveSupplier");
	dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
	dataAudit.setOrderNumber(null);
	dataAudit.setUuid(null);
	dataAudit.setStep(null);
	dataAudit.setMessage("Saved Supplier Successful - Ticket: " + o.getTicketId());
	dataAudit.setNotes(null);
	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
	dataAudit.setUser(usr);
	
	dataAuditService.save(dataAudit);
    return o.getTicketId().longValue();
  }
  
  
public String disableSupplier(String id,String userDisable) {
	  try {
		  
		  final String OLD_FORMAT = "dd-MM-yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern(OLD_FORMAT);
			
		  Supplier s = supplierDao.getSupplierById(Integer.valueOf(id));
		  Users u = usersService.getByUserName(s.getAddresNumber());
		  
		    DataAudit dataAudit = new DataAudit();
	    	Date currentDate = new Date();
	    	
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 			
			String usr = auth.getName();
		  
		  if(s != null) {
			  s.setObservaciones("INHABILITADO");
	          s.setSearchType("VI");
	          //s.setHold("C9");
	          s.setTipoMovimiento("C");
	          s.setRejectNotes(userDisable + " " + sdf.format(new Date()));
			  //supplierDao.updateSupplier(s);
			  //EDIService.registerNewAddressBook(s);
	          
	          long leftLimit = 1_000_000_000L;      // 10 d�gitos m�nimo
	    	  long rightLimit = 9_999_999_999L;     // 10 d�gitos m�ximo
	    	  long randomTicket = leftLimit + (long)(Math.random() * (rightLimit - leftLimit + 1));
	    	
	    	  s.setChangeTicket(String.valueOf(randomTicket));
		  }
		  
		  Supplier jdeS = EDIService.disable(s);
          if(jdeS==null) {
              s.setAddresNumber(null);
              return "Fail Disable";
          }else {
        	  supplierDao.updateSupplier(s);
        	  
        	  if(u != null) {
    			  u.setEnabled(false);
    			  usersService.update(u, null, null);
    		  }
    		  
        	  List<UDC> emailnotif = this.udcService.searchBySystem("EMAILNOTIFAPPROV");
        	  if(emailnotif != null) {
        		  for(UDC o : emailnotif) {
        			  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
        			  emailAsyncPur.setProperties("SEPASA - Proveedor inhabilitado", this.stringUtils.prepareEmailContent("Estimado usuario <br /><br />El proveedor " + s.getAddresNumber() + " | " + s.getRazonSocial() + " fue inhabilitado del portal de proveedores por el usuario " + userDisable +"<br><br>"), o.getStrValue1());
        		      emailAsyncPur.setMailSender(this.mailSenderObj);
        		      Thread emailThreadPur = new Thread(emailAsyncPur);
        		      emailThreadPur.start();
        		  }
        	  }
        	  
        	  	dataAudit.setAction("DisableSupplier");
            	dataAudit.setAddressNumber(s.getAddresNumber());
            	dataAudit.setCreationDate(currentDate);
            	dataAudit.setDocumentNumber(null);
            	dataAudit.setIp(request.getRemoteAddr());
            	dataAudit.setMessage("Disable Provider Successful");
            	dataAudit.setMethod("disableSupplier");
            	dataAudit.setModule(AppConstants.SUPPLIER_MODULE);
            	dataAudit.setNotes(null);
            	dataAudit.setOrderNumber(null);
            	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
            	dataAudit.setStep(null);
            	dataAudit.setUser(userDisable);
            	dataAudit.setUuid(null);
            	
            	dataAuditService.save(dataAudit);
      		  
      		  return "Succ Disable";
          }
          
        
	  }catch(Exception e) {
		  log4j.error("Exception" , e);
		  return "Fail Disable";
	  }
  }

public String updateEmailSupplier(int id, String emailSupplier) {
	  try {
		  Supplier s = supplierDao.getSupplierById(id);
		  Users u = usersService.getByUserName(s.getAddresNumber());
		  
		  DataAudit dataAudit = new DataAudit();
	  	  Date currentDate = new Date();
	  	
	  	  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		  HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		  String usr = auth.getName(); 	
		  
		  if(s != null) {
			  s.setEmailSupplier(emailSupplier);
			  supplierDao.updateSupplier(s);
		  }
		  if(u != null) {
			  u.setEmail(emailSupplier);
			  usersService.update(u, null, null);
		  }
		  
		    dataAudit.setAction("UpdateEmailSupplier");
		  	dataAudit.setAddressNumber(s.getAddresNumber());
		  	dataAudit.setCreationDate(currentDate);
		  	dataAudit.setDocumentNumber(null);
		  	dataAudit.setIp(request.getRemoteAddr());
		  	dataAudit.setMethod("updateEmailSupplier");
		  	dataAudit.setModule(AppConstants.SUPPLIER_MODULE);    	
		  	dataAudit.setOrderNumber(null);
		  	dataAudit.setUuid(null);
		  	dataAudit.setStep(null);
		  	dataAudit.setMessage("TicketID: " + s.getTicketId() + " Change Email to: " + emailSupplier);
		  	dataAudit.setNotes(null);
		  	dataAudit.setStatus(AppConstants.STATUS_COMPLETE);
		  	dataAudit.setUser(usr);
		  	dataAuditService.save(dataAudit);
		  
		  return "Succ Update Email";
	  }catch(Exception e) {
		  log4j.error("Exception" , e);
		  e.printStackTrace();
		  return "Fail Update Email";
	  }
}

public int update(Supplier sup) {
	try {
		this.supplierDao.updateSupplier(sup);
		return 1;
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
		return 0;
	}
	
}
  
  public int getTotalRecords() {
    return this.supplierDao.getTotalRecords();
  }
  
  public List<CodigosPostales> getByCode(String code, int start, int limit) {
    return this.codigosPostalesDao.getByCode(code, start, limit);
  }
  
  public List<SupplierDTO> listSuppliers(String supAddNbr, String supAddName, int start, int limit) {
    return this.supplierDao.listSuppliers(supAddNbr, supAddName, start, limit);
  }
  
  public int listSuppliersTotalRecords(String supAddNbr, String supAddName) {
    return this.supplierDao.listSuppliersTotalRecords(supAddNbr, supAddName);
  }
  
  public void saveSuppliers(List<Supplier> list) {
    this.supplierDao.saveSuppliers(list);
  }
  
  public void delete(int id){
	  supplierDao.deleteSupplier(id);
  }
  
  public Users getUserByEmail(String email) {
    return this.usersService.getUserByEmail(email);
  }
  
  public Users getPurchaseRoleByEmail(String email) {
    return this.usersService.getPurchaseRoleByEmail(email);
  }
  
  public int getRfcRecords(String rfc) {
    return this.supplierDao.getRfcRecords(rfc);
  }
  
  public List<Supplier> searchByRfc(String rfc, String typeSearch) {
	if("rfc".equals(typeSearch)) {
		return this.supplierDao.searchByRfc(rfc);
	}else {
		return this.supplierDao.searchByTaxId(rfc);
	}
    
  }
  
  public Supplier searchByTicket(long ticketId) {
    return this.supplierDao.searchByTicket(ticketId);
  }
  
  public void sendOutSourcingEmail(Supplier s) {
	  
	  EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
	  String subject = AppConstants.OUTSOURCING_APPROVAL_SUBJECT;
	  subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
	  String msg = this.stringUtils.prepareEmailContent(AppConstants.OUTSOURCING_APPROVED_MESSAGE);
	  
      emailAsyncPur.setProperties(subject, msg, s.getEmailSupplier());
      emailAsyncPur.setMailSender(this.mailSenderObj);
      Thread emailThreadPur = new Thread(emailAsyncPur);
      emailThreadPur.start();
  }
  
  public void updateSupplierOutSourcing(Supplier o) {
	  this.supplierDao.updateSupplier(o);
  }

  public void updateSupplierCore(Supplier o) {
	  this.supplierDao.updateSupplier(o);
  }

  public List<Supplier> searchByOutSourcingStatus() {
	  return this.supplierDao.searchByOutSourcingStatus();
  }
  
  public List<SupplierDTO> getActiveList() {
	  return this.supplierDao.getActiveList();
  }
  
  public AccessTokenRegister searchByAccessCode(String code, String password) { 
		String encodePass = Base64.getEncoder().encodeToString(password.trim().getBytes());
		encodePass = "==a20$" + encodePass;
		return this.supplierDao.searchByAccessCode(code, encodePass);
	}

	public void saveAccessToken(AccessTokenRegister o, String userName, String company) throws IOException {

		o.setCompany(company);
		o.setCreatedBy(userName);
		Date currentDate = new Date();
		o.setCreationDate(currentDate);
		o.setUpdatedDate(currentDate);

		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, 3); // El token vence en 3 d�as posterior a su creaci�n
		o.setExpirationDate(c.getTime());
		o.setCode(StringUtils.randomString(6));
		String tempPass = StringUtils.randomString(8);
		String encodePassCode = Base64.getEncoder().encodeToString(tempPass.trim().getBytes());
		encodePassCode = "==a20$" + encodePassCode;
		o.setPassword(encodePassCode);

		String token = getJWTToken(o.getCode(), c.getTime());
		o.setToken(token);
		
		o.setApprovalFlowUser(userName);

		this.supplierDao.saveAccessToken(o);
		
		String secureUrl = AppConstants.EMAIL_PORTAL_LINK + "public/authRegister?access_token=" + token;
		
		System.out.println(secureUrl);
		
		// Crear instancia del nuevo servicio
				EmailServiceAsyncWithAttachment emailAsyncSup = new EmailServiceAsyncWithAttachment();

				// Configurar asunto, mensaje y destinatario
				emailAsyncSup.setProperties(
				    AppConstants.NEWREGISTER_SUBJECT,
				    stringUtils.prepareEmailContent(AppConstants.NEWREGISTER_MESSAGE + secureUrl),
				    o.getEmail()
				);

				// Asignar el mail sender (lo tienes ya configurado)
				emailAsyncSup.setMailSender(mailSenderObj);

				// Obtener el recurso .zip desde el classpath
				ApplicationContext context = ApplicationContextProvider.getApplicationContext();
				Resource zipResource = context.getResource("classpath:com/eurest/supplier/util/Alta_proveedores.zip");

				// Copiar a archivo temporal si el recurso existe
				if (zipResource.exists()) {
				    File tempZip = File.createTempFile("Alta_proveedores-", ".zip");

				    try (InputStream in = zipResource.getInputStream()) {
				        Files.copy(in, tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
				    }

				    // Establecer la ruta del archivo adjunto ZIP
				    emailAsyncSup.setZipAttachmentPath(tempZip.getAbsolutePath());
				}

				// Ejecutar en hilo separado
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();
		
		/*EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		emailAsyncSup.setProperties(AppConstants.NEWREGISTER_SUBJECT, stringUtils.prepareEmailContent(AppConstants.NEWREGISTER_MESSAGE + secureUrl), o.getEmail());
		emailAsyncSup.setMailSender(mailSenderObj);
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();*/

	}
	
	public void updateAccessToken(AccessTokenRegister obj, String userName) throws IOException {

		AccessTokenRegister o = supplierDao.getAccessTokenRegisterById(obj.getId());

		Date currentDate = new Date();
		o.setUpdatedDate(currentDate);
		o.setCode(StringUtils.randomString(6));

		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, 3); // El token vence en 3 d�as posterior a su creaci�n
		o.setExpirationDate(c.getTime());

		String token = getJWTToken(o.getCode(), c.getTime());
		o.setToken(token);
		o.setEnabled(true);
		
		o.setEmail(obj.getEmail());
		
		o.setUpdatedBy(userName);
		
		o.setApprovalFlowUser(userName);
		
		o.setRfc(obj.getRfc());
		
		o.setSearchType(obj.getSearchType());

		this.supplierDao.updateAccessToken(o);
		
		String secureUrl = AppConstants.EMAIL_PORTAL_LINK + "public/authRegister?access_token=" + token;
		
		System.out.println(secureUrl);

		// Crear instancia del nuevo servicio
		EmailServiceAsyncWithAttachment emailAsyncSup = new EmailServiceAsyncWithAttachment();

		// Configurar asunto, mensaje y destinatario
		emailAsyncSup.setProperties(
		    AppConstants.NEWREGISTER_RENEW_SUBJECT,
		    stringUtils.prepareEmailContent(AppConstants.NEWREGISTER_RENEW_MESSAGE + secureUrl),
		    o.getEmail()
		);

		// Asignar el mail sender (lo tienes ya configurado)
		emailAsyncSup.setMailSender(mailSenderObj);

		// Obtener el recurso .zip desde el classpath
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		Resource zipResource = context.getResource("classpath:com/eurest/supplier/util/Alta_proveedores.zip");

		// Copiar a archivo temporal si el recurso existe
		if (zipResource.exists()) {
		    File tempZip = File.createTempFile("Alta_proveedores-", ".zip");

		    try (InputStream in = zipResource.getInputStream()) {
		        Files.copy(in, tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
		    }

		    // Establecer la ruta del archivo adjunto ZIP
		    emailAsyncSup.setZipAttachmentPath(tempZip.getAbsolutePath());
		}

		// Ejecutar en hilo separado
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();


		
		//System.out.println(secureUrl);
		/*EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		emailAsyncSup.setProperties(AppConstants.NEWREGISTER_RENEW_SUBJECT, stringUtils.prepareEmailContent(AppConstants.NEWREGISTER_RENEW_MESSAGE + secureUrl), o.getEmail());
		emailAsyncSup.setMailSender(mailSenderObj);
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();*/

	}

	private String getJWTToken(String username, Date expirationDate) {
		String secretKey = "aink_45$11SecKey";
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

		String token = Jwts.builder().setId("smartechIdJWT").setSubject(username)
				.claim("authorities",
						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

		return token;
	}

	public String validateAcccessTokenRegister(String token, HttpServletRequest request, boolean disable) {
		try {
			Claims claims = Jwts.parser().setSigningKey(SECRET.getBytes()).parseClaimsJws(token).getBody();
			if (claims.get("authorities") != null) {
				return setUpSpringAuthentication(claims, token, disable);
			} else {
				return "invalidToken";
			}
		} catch (ExpiredJwtException e) {
		    return "invalidToken";
		} catch(Exception e){
		    return "invalidToken";
		}

	}
	
	private String setUpSpringAuthentication(Claims claims, String token, boolean disable) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<String> authorities = (List) claims.get("authorities");

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		
		AccessTokenRegister atr = supplierDao.searchActiveAccessCode(auth.getPrincipal().toString());
		if(atr != null) {
			if(disable) {
				atr.setEnabled(false);
			}
			supplierDao.updateAccessToken(atr);
			return "redirect:/newRegisterAuth.action?access_token=" + token;
		}
		
		return "invalidToken";

	}

	public List<AccessTokenRegister> listAccessTokenRegister(String query, int start, int limit) {
		return this.supplierDao.listAccessTokenRegister(query, start, limit);
	}

	public int listAccessTokenRegisterCount(String query) {
		return this.supplierDao.listAccessTokenRegisterCount(query);
	}

	public AccessTokenRegister getAccessTokenRegisterById(int id) {
		return this.supplierDao.getAccessTokenRegisterById(id);
	}
	
	public String validateTaxId(String taxId, String access_token) {
		/*try {
			String result = jDERestService.searchTaxId(taxId, access_token);
			if(!"invalidRegister".equals(result)){
				AccessTokenRegister reg = searchByToken(access_token);
				if(reg != null) {
					reg.setAssigned(true);
					this.supplierDao.updateAccessToken(reg);
					return result;
				}
			}else {
				return "invalidRegister"; 
			}
		} catch(Exception e){
		    return "invalidTaxId";
		}*/
		return "invalidRegister"; 
	}
	
	public NonComplianceSupplier validateTaxIdInBlackList(String taxId) {
		return nonComplianceSupplierService.searchByTaxId(taxId, 0, 0);
	}
	
	public List<Supplier>validateTaxId(String taxId) {
		return supplierDao.validateTaxId(taxId);
	}
	
	public AccessTokenRegister searchByToken(String access_token) {
		return this.supplierDao.searchByToken(access_token)  ;
	}
	
	public boolean validateNonCompliance(String taxId, String access_token) {
		NonComplianceSupplier ncs = this.nonComplianceSupplierService.searchByTaxId(taxId, 0, 0);
		if (ncs != null && (ncs.getRefDate1().contains("Definitivo") || ncs.getRefDate1().contains("Presunto")
				|| ncs.getRefDate1().contains("Desvirtuado") || ncs.getRefDate2().contains("Definitivo")
				|| ncs.getRefDate2().contains("Presunto") || ncs.getRefDate2().contains("Desvirtuado")
				|| ncs.getStatus().contains("Definitivo") || ncs.getStatus().contains("Presunto")
				|| ncs.getStatus().contains("SENTENCIAS") || ncs.getStatus().contains("NO LOCALIZADOS")
				|| ncs.getStatus().contains("EXIGIBLES") || ncs.getStatus().contains("FIRMES")
				|| ncs.getStatus().contains("Desvirtuado"))) {
			AccessTokenRegister reg = searchByToken(access_token);
			reg.setEnabled(false);
			reg.setAssigned(true);
			reg.setExpirationDate(new Date());
			supplierDao.updateAccessToken(reg);
			return false;
		}

		return true;
	}
  
}
