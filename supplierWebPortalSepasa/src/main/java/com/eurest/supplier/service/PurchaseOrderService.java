package com.eurest.supplier.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eurest.supplier.dao.FiscalDocumentDao;
import com.eurest.supplier.dao.PurchaseOrderDao;
import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.dto.ForeingInvoice;
import com.eurest.supplier.dto.InvoiceCodesDTO;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.dto.PurchaseOrderDTO;
import com.eurest.supplier.dto.PurchaseOrderGridDTO;
import com.eurest.supplier.invoiceXml.Concepto;
import com.eurest.supplier.model.CodigosSAT;
import com.eurest.supplier.model.ExchangeRate;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.ForeignInvoiceTable;
import com.eurest.supplier.model.LogData;
import com.eurest.supplier.model.PaymentCalendar;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.PurchaseOrderDetail;
import com.eurest.supplier.model.PurchaseOrderPayment;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.ReceiptInvoice;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.Tolerances;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.FileConceptUploadBean;
import com.eurest.supplier.util.JdeJavaJulianDateTools;
import com.eurest.supplier.util.Logger;
import com.eurest.supplier.util.StringUtils;

@Service("purchaseOrderService")
public class PurchaseOrderService {

	@Autowired
	private PurchaseOrderDao purchaseOrderDao;

	@Autowired
	EDIService eDIService;

	@Autowired
	private JavaMailSender mailSenderObj;

	@Autowired
	EmailService emailService;

	@Autowired
	SupplierService supplierService;

	@Autowired
	TolerancesService tolerancesService;

	@Autowired
	DocumentsService documentsService;

	@Autowired
	JDERestService jDERestService;

	@Autowired
	StringUtils stringUtils;

	@Autowired
	UdcService udcService;
	
	@Autowired
	UDCDao udcDao;

	@Autowired
	Logger logger;
	
	@Autowired
	XmlToPojoService xmlToPojoService;
	
	@Autowired
	CodigosSATService codigosSATService;
	
	@Autowired
	PaymentCalendarService paymentCalendarService;
	
	@Autowired
	FiscalDocumentService fiscalDocumentService;
	
	@Autowired
	FiscalDocumentDao fiscalDocumentDao;	
	
	@Autowired
	ExchangeRateService exchangeRateService;
	
	@Autowired
	DataAuditService dataAuditService;
	
	@Autowired
	JDERestService jdeRestService;
	  
	static String TIMESTAMP_DATE_PATTERN = "yyyy-MM-dd";
	static String TIMESTAMP_DATE_PATTERN_NEW = "yyyy-MM-dd HH:mm:ss";
	static String DATE_PATTERN = "dd/MM/yyyy";	
	
	private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(PurchaseOrderService.class);

	public List<PurchaseOrder> getOrders(int start, int limit) {
		return purchaseOrderDao.getOrders(start, limit);
	}

	
	
	
	public synchronized String approveOC(String addressBook, String documentNumber, String documentType, String notes, String user) {
	  	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 	String userAuth = auth.getName();
	 	
		PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBook(Integer.valueOf(documentNumber), addressBook, documentType);
		if(po != null) {
			List<Receipt> receiptArray= purchaseOrderDao.getOrderReceipts(po.getOrderNumber(), po.getAddressNumber(), po.getOrderType(), po.getOrderCompany());
			
			if(receiptArray != null && !receiptArray.isEmpty()) {
				po.setOrderStauts(AppConstants.STATUS_OC_APPROVED);
			} else {
				po.setOrderStauts(AppConstants.STATUS_OC_RECEIVED);
			}
			po.setHeaderNotes(notes);
			updateOrders(po);
			
			//Cambio de Estatus en JDE (Aprobación)
			PurchaseOrderDTO dto = new PurchaseOrderDTO();
			dto.setPHAN8(po.getAddressNumber());
			dto.setPHDOCO(po.getOrderNumber()+"");
			dto.setPHDCTO(po.getOrderType());
			dto.setPHKCOO(po.getOrderCompany());
			dto.setPDLTTR("380");
			dto.setPDNXTR("400");
			jDERestService.sendOrderConfirmation(dto);
			
          	dataAuditService.saveDataAudit("UpdatePOStatusApproved",po.getAddressNumber(), new Date(), request.getRemoteAddr(),
    	            userAuth, "PDLTTR = 380 - PDNXTR = 400", "approveOC", 
    	            "Purchase Order Status Updated Successfully",documentNumber+"", po.getOrderNumber()+"", null, 
    	            "", AppConstants.STATUS_OC_RECEIVED, AppConstants.SALESORDER_MODULE);    
			
			String emailApprover = "";				
			List<UDC> approverUDCList = udcDao.searchBySystem("APPROVEROC");
			if(approverUDCList != null) {
				for(UDC approver : approverUDCList) {
					if(AppConstants.INV_FIRST_APPROVER.equals(approver.getUdcKey())){				
						emailApprover = approver.getStrValue2();
					}				
				}
			}
			
			Supplier s = supplierService.searchByAddressNumber(po.getAddressNumber());
			if(s!=null) {
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(
					AppConstants.EMAIL_NEW_ORDER_NOTIF + po.getOrderNumber() + "-" + po.getOrderType(),
					stringUtils.prepareEmailContent(AppConstants.EMAIL_PURCHASE_NEW + po.getOrderNumber() + "-"
					+ po.getOrderType() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />"
					+ "Cuenta: " + po.getAddressNumber() + "<br /> Razon Social: " + s.getRazonSocial()),
					emailApprover);
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();
			}
		}
		
    	return "";
    }
	
	
	
	public synchronized String rejectOC(String addressBook, String documentNumber, String documentType, String notes, String user) {
	  	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 	String userAuth = auth.getName();
	 	
		PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBook(Integer.valueOf(documentNumber), addressBook, documentType);
		if(po != null) {
				po.setOrderStauts(AppConstants.STATUS_OC_REJECTED);
				po.setRejectNotes(notes);
				updateOrders(po);
				
				//Cambio de Estatus en JDE (Rechazo)
				PurchaseOrderDTO dto = new PurchaseOrderDTO();
				dto.setPHAN8(po.getAddressNumber());
				dto.setPHDOCO(po.getOrderNumber()+"");
				dto.setPHDCTO(po.getOrderType());
				dto.setPHKCOO(po.getOrderCompany());
				dto.setPDLTTR("380");
				dto.setPDNXTR("225");
				jDERestService.sendOrderConfirmation(dto);
				
	          	dataAuditService.saveDataAudit("UpdatePOStatusRejected",po.getAddressNumber(), new Date(), request.getRemoteAddr(),
	    	            userAuth, "PDLTTR = 380 - PDNXTR = 225", "rejectOC", 
	    	            "Purchase Order Status Updated Successfully",documentNumber+"", po.getOrderNumber()+"", null, 
	    	            "", AppConstants.STATUS_OC_REJECTED, AppConstants.SALESORDER_MODULE);
				
		String emailApprover = "";
		List<UDC> approverUDCList = udcDao.searchBySystem("APPROVEROC");
		if(approverUDCList != null) {
			for(UDC approver : approverUDCList) {
				if(AppConstants.INV_FIRST_APPROVER.equals(approver.getUdcKey())){				
					emailApprover = approver.getStrValue2();
				}				
			}
		}
		
		Supplier s = supplierService.searchByAddressNumber(po.getAddressNumber());
			if(s!=null) {
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(
					AppConstants.EMAIL_NEW_ORDER_NOTIF + po.getOrderNumber() + "-" + po.getOrderType(),
					stringUtils.prepareEmailContent(AppConstants.EMAIL_PURCHASE_NEW3 + po.getOrderNumber() + "-"
					+ po.getOrderType() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />"
					+ "Cuenta: " + po.getAddressNumber() 
					+ "<br />Razon Social: " + s.getRazonSocial()
					+ "<br /> Motivo de Rechazo: " + notes),
					emailApprover);
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();
			}
						        						
		}
		
    	return "";
    }	
	
	
	
	
	
	public boolean confirmEmailOrders(PurchaseOrder[] selected) {

		try {
			
			String altEmail = "";
			List<UDC> udcList =  udcService.searchBySystem("ALTEMAIL");
			if(udcList != null) {
				altEmail = udcList.get(0).getStrValue1();
			}

			List<PurchaseOrder> objList = Arrays.asList(selected);
			Supplier s = null;
			for (PurchaseOrder o : objList) {
				
				s = supplierService.searchByAddressNumber(o.getAddressNumber());
				o.setOrderStauts(AppConstants.STATUS_OC_SENT);
				o.setSupplierEmail(s.getEmailSupplier());
				purchaseOrderDao.updateOrders(o);
				String emailRecipient = (s.getEmailContactoPedidos());
				if(AppConstants.DOCTYPE_PUB1.equals(o.getOrderType()) || AppConstants.DOCTYPE_PUB2.equals(o.getOrderType())) {
					if(!"".equals(altEmail)) {
						emailRecipient = emailRecipient + "," + altEmail;
					}
				}
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(
						AppConstants.EMAIL_NEW_ORDER_NOTIF + o.getOrderNumber() + "-" + o.getOrderType(),
						stringUtils.prepareEmailContent(AppConstants.EMAIL_PURCHASE_NEW + o.getOrderNumber() + "-"
								+ o.getOrderType() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />"
								+ "Cuenta: " + o.getAddressNumber() + "<br /> Razon Social: " + o.getDescription()),
						emailRecipient + "," + o.getEmail());
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();
			}
			return true;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return false;
		}
	}

	public boolean reasignOrders(PurchaseOrder[] selected) {

		try {

			List<PurchaseOrder> objList = Arrays.asList(selected);
			List<Integer> idList = new ArrayList<Integer>();
			String email = "";

			for (PurchaseOrder o : objList) {
				idList.add(o.getId());
				email = o.getEmail();
				logger.log(AppConstants.LOG_REASIGN_TITLE, AppConstants.LOG_REASIGN_MSG.replace("ORDER_NUMBER", String.valueOf(o.getOrderNumber())).replace("NEW_ORDER_EMAIL", email));
			}

			if (idList.size() > 0) {
				return purchaseOrderDao.updateEmail(email, idList);
			}

			return false;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return false;
		}
	}

	public boolean confirmOrderInvoices(PurchaseOrder[] selected) {

		try {

			List<PurchaseOrderDTO> oDto = new ArrayList<PurchaseOrderDTO>();
			List<PurchaseOrder> objList = Arrays.asList(selected);
			for (PurchaseOrder o : objList) {

				o.setOrderStauts(AppConstants.STATUS_OC_PROCESSED);
				purchaseOrderDao.updateOrders(o);
				Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
				String emailRecipient = (s.getEmailSupplier());

				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup
						.setProperties(
								AppConstants.EMAIL_INV_APPROVED_SUP + o.getOrderNumber() + "-" + o.getOrderType(),
								stringUtils.prepareEmailContent(AppConstants.EMAIL_INVOICE_NOPURCHASE_ACCEPTED
										+ o.getOrderNumber() + "-" + o.getOrderType()),
								emailRecipient + "," + o.getEmail());
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();

				PurchaseOrderDTO td = new PurchaseOrderDTO();
				td.setPHAN8(o.getAddressNumber());
				td.setPHSFXO(o.getOrderCompany());
				td.setPHDOCO(String.valueOf(o.getOrderNumber()));
				td.setPHDCTO(o.getOrderType());
				td.setPHDESC(o.getInvoiceUuid());
				td.setPHORDERSTS(o.getInvoiceNumber());
				oDto.add(td);
				eDIService.createNewReceipt(o);
			}

			if (oDto.size() > 0) {
				jDERestService.sendOrderInvoiceConfirmation(oDto);
			}

			return true;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return false;
		}
	}

	public boolean rejectOrderInvoices(PurchaseOrder[] selected, boolean notifySupplier, String rejectType) {

		try {
			List<PurchaseOrder> objList = Arrays.asList(selected);
			for (PurchaseOrder o : objList) {

				    String uuid = "";
					if("S".equals(o.getSentToWns())) {
						o.setSentToWns("N");
					}else {
						o.setSentToWns(null);
					}
					
					if("WNS".equals(rejectType)) {
						o.setStatus(AppConstants.STATUS_OC_INVOICED);
						o.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
						purchaseOrderDao.updateOrders(o);
					}else {
						uuid = o.getInvoiceUuid();
						o.setOrderStauts(AppConstants.STATUS_OC_SENT);
						o.setInvoiceUuid("");
						o.setInvoiceAmount(0);
						o.setOrderAmount(o.getOriginalOrderAmount());
						o.setStatus("");
						o.setInvoiceNumber("");
						purchaseOrderDao.updateOrders(o);

						List<UserDocument> docList = documentsService.searchCriteriaByOrderNumber(o.getOrderNumber(),
								o.getOrderType(), o.getAddressNumber());
						if (docList != null) {
							if (!docList.isEmpty()) {
								for (UserDocument d : docList) {
									documentsService.delete(d.getId(),"INV_REJECT");
								}
							}
						}
					}
				if(notifySupplier) {
					Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
	
					// ****** ELIMINAR COMENTARIOS EN PRODUCCION
					String emailRecipient = (s.getEmailSupplier());
	
					/// ************ DESHABILITAR PARA PRODUCCIÓN
					//String emailRecipient = "javila@smartech.com.mx";

					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(
							AppConstants.EMAIL_INV_REJECT_SUP + o.getOrderNumber() + "-" + o.getOrderType(),
							stringUtils.prepareEmailContent(AppConstants.EMAIL_INVOICE_REJECTED + o.getOrderNumber() + "-"
									+ o.getOrderType() + "<br /><br />" + "Motivo: " + o.getRejectNotes() + "<br /><br />"
									+ AppConstants.EMAIL_PORTAL_LINK + AppConstants.EMAIL_CONTACT_SUPPORT + o.getEmail()),
							emailRecipient + "," + o.getEmail() + ",PortalProveedores@umusic.com");
					emailAsyncSup.setMailSender(mailSenderObj);
					emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
					Thread emailThreadSup = new Thread(emailAsyncSup);
					emailThreadSup.start();
				}else {
					String emailRecipient = (o.getEmail());
					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(
							AppConstants.EMAIL_INV_REJECT_SUP + o.getOrderNumber() + "-" + o.getOrderType(),
							stringUtils.prepareEmailContent(AppConstants.EMAIL_INVOICE_REJECTED + o.getOrderNumber() + "-"
									+ o.getOrderType() + "<br /><br />" + "Motivo: " + o.getRejectNotes() + "<br /><br />"
									+ AppConstants.EMAIL_PORTAL_LINK + AppConstants.EMAIL_CONTACT_SUPPORT + o.getEmail()),
							emailRecipient + "," + "PortalProveedores@umusic.com");
					emailAsyncSup.setMailSender(mailSenderObj);
					emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
					Thread emailThreadSup = new Thread(emailAsyncSup);
					emailThreadSup.start();
				}

				logger.log(AppConstants.LOG_INVREJECTED_TITLE, AppConstants.LOG_INVREJECTED_MDG + o.getOrderNumber()
						+ " / UUID:" + uuid + " -> Texto del rechazo: " + o.getRejectNotes());
			}
			return true;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return false;
		}
	}

	public void paymentImportBulk() {
		try {
			jDERestService.getOrderPayments();
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();

		}
	}

	public List<PurchaseOrder> getOrderForPayment() {
		return purchaseOrderDao.getOrderForPayment();
	}

	public List<PurchaseOrder> searchbyOrderNumber(int orderNumber, String addressBook, Date poFromDate, Date poToDate,
			String status, int start, int limit, String role, String email, String foreing, String pfolio) {

		Map<String, Tolerances> tolerances = tolerancesService.getTolerancesMap();
		List<PurchaseOrder> list = purchaseOrderDao.searchbyOrderNumberHQL(orderNumber, addressBook, poFromDate, poToDate,
				status, start, limit, role, email, foreing,pfolio);

		Map<String, String> udcCompanyMap = new HashMap<String, String>();
		List<UDC> udcList = udcService.searchBySystem("CIACOR");
		if (udcList != null) {
			for (UDC o : udcList) {
				udcCompanyMap.put(o.getUdcKey(), o.getStrValue1());
			}
		}

		Map<String, String> udcCompanyMapLar = new HashMap<String, String>();
		List<UDC> udcListLar = udcService.searchBySystem("CIALAR");
		if (udcListLar != null) {
			for (UDC o : udcListLar) {
				udcCompanyMapLar.put(o.getUdcKey(), o.getStrValue1() + " / " + o.getStrValue2());
			}
		}

		if (!list.isEmpty()) {
			for (PurchaseOrder po : list) {
				Set<PurchaseOrderDetail> dtl = po.getPurchaseOrderDetail();
				String company = po.getOrderCompany();
				String obj = udcCompanyMap.get(company);
				if (obj != null) {
					po.setShortCompanyName(company + "-" + obj);
				}

				obj = udcCompanyMapLar.get(company);
				if (obj != null) {
					po.setLongCompanyName(company + "-" + obj);
				}

				if (!dtl.isEmpty()) {
					for (PurchaseOrderDetail d : dtl) {
						// String key = d.getOrderCompany().trim() + "_" + d.getItemNumber().trim();
						String key = d.getItemNumber().trim();
						Tolerances t = (Tolerances) tolerances.get(key);
						if (t != null) {
							d.setTolerances(t);
						}
					}
				}

			}

		}
		return list;
	}

	public int getTotalRecords(String addressBook, int orderNumber, Date poFromDate, Date poToDate, String status,
			String role, String email, String foreign, String pfolio) {
		return purchaseOrderDao.searchbyOrderNumberHQLCount(orderNumber, addressBook, poFromDate, poToDate, status, role, email, foreign, pfolio);
	}

	public List<PurchaseOrder> searchCriteria(String addressBook, String status, String orderStatus) {
		return purchaseOrderDao.searchCriteria(addressBook, status, orderStatus);
	}

	public PurchaseOrder getOrderByOrderAndAddresBook(int orderNumber, String addressBook, String orderType) {
		return purchaseOrderDao.searchbyOrderAndAddressBook(orderNumber, addressBook, orderType);
	}

	public List<ReceiptInvoice> getPaymentPendingReceipts() {
		return purchaseOrderDao.getPaymentPendingReceipts();
	}
		
	public List<Receipt> getPaymentPendingReceipts(int start, int limit) {
		return purchaseOrderDao.getPaymentPendingReceipts(start, limit);
	}
	

	public List<Receipt> getOpenOrderReceipts(int start, int limit) {
		return purchaseOrderDao.getOpenOrderReceipts(start, limit);
	}

	public PurchaseOrder searchbyOrder(int orderNumber, String orderType) {
		return purchaseOrderDao.searchbyOrder(orderNumber, orderType);
	}

	public List<PurchaseOrder> getPurchaseOrderByOrderEvidence(boolean isWithEvidence, int maxAttempts) {
		return purchaseOrderDao.getPurchaseOrderByOrderEvidence(isWithEvidence, maxAttempts);
	}
	
	public List<PurchaseOrder> searchCriteriaByEmail(String email) {
		return purchaseOrderDao.searchCriteriaByEmail(email.trim());
	}

	public PurchaseOrder getOrderById(int id) {
		return purchaseOrderDao.getOrderById(id);

	}

	public Receipt getReceiptById(int id) {
		return purchaseOrderDao.getReceiptById(id);
	}
	
	public ReceiptInvoice getReceiptInvoiceId(int id) {
		return purchaseOrderDao.getReceiptInvoiceById(id);
	}
	
	public List<Receipt> getOrderReceiptsPendFact(String addressBook) {
		UDC Fecha_limite = udcService.searchBySystemAndKey("RECEIPT", "LIMITDATEPENDIENT");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date  fechaLimite = null;
        try {
             fechaLimite = dateFormat.parse(Fecha_limite.getStrValue1());
           
        } catch (ParseException e) {
           e.printStackTrace();
        }
		
		return purchaseOrderDao.getOrderReceiptsPendFact(addressBook,fechaLimite);
				
	}
	public List<PurchaseOrder> getPendingPaymentOrders(int orderNumber, String addressBook, String orderType) {

		
		//JAVILA: Se revisó el fromat de fechas que proucía un a excepción incorrecta
		List<PurchaseOrder> invalidList = new ArrayList<PurchaseOrder>();
		List<PurchaseOrder> list = purchaseOrderDao.getPendingPaymentOrders(orderNumber, addressBook, orderType);
		/*for (PurchaseOrder p : list) {
			/*
			//Las facturas con método de pago PUE quedan excentas de esta validación de pagos pendientes.
			if(p.getUuid() != null && !p.getUuid().isEmpty()){
				List<UserDocument> docs = documentsService.searchCriteriaByRefFiscal(addressBook, p.getUuid());
				boolean isExemptMethod = false;
				if(docs != null && !docs.isEmpty()) {					
					for(UserDocument doc : docs) {
						try {
							if(AppConstants.INVOICE_FIELD.equals(doc.getFiscalType()) && "text/xml".equals(doc.getType())) {
								String xmlContent = new String(doc.getContent(), StandardCharsets.UTF_8);
					        	InvoiceDTO inv = documentsService.getInvoiceXmlFromString(xmlContent);
					        	if(AppConstants.FACTURA_PUE.equals(inv.getMetodoPago())) {
					        		isExemptMethod = true;
					        		break;
					        	}
							}	
						} catch (Exception e) {
							e.printStackTrace();
						}
					}	
				}
				if(isExemptMethod) {
					continue;
				}
			}
			*/
		
			/*if (p.getEstimatedPaymentDate() != null) {
				if (true) {
					try {
						Date pDate =p.getEstimatedPaymentDate();
						Date currentDate = new Date();

						Calendar paymentCalendar = Calendar.getInstance();
						Calendar currentCalendar = Calendar.getInstance();
						paymentCalendar.setTime(pDate);
						currentCalendar.setTime(currentDate);

						int paymentMonth = paymentCalendar.get(Calendar.MONTH) + 1;
						int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;

						if (paymentMonth < 12) {
							int paymentTargetMonth = paymentMonth + 1;
							int paymentTargetDay = 5;

							// Same Year
							if (paymentCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
								if (currentMonth == paymentTargetMonth) {
									int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
									if (currentDay > paymentTargetDay) {
										invalidList.add(p);
										continue;
									}
								}
								if (currentMonth > paymentTargetMonth) {
									invalidList.add(p);
									continue;
								}

							}
							
							// Previous Year
							if (paymentCalendar.get(Calendar.YEAR) < currentCalendar.get(Calendar.YEAR)) {
								if (currentMonth == paymentTargetMonth) {
									int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
									if (currentDay > paymentTargetDay) {
										invalidList.add(p);
										continue;
									}
								}
								
								if (currentMonth < paymentTargetMonth) {
									invalidList.add(p);
									continue;
								}

							}
							

						} else {
							// Next year
							int paymentTargetMonth = 1;
							int paymentTargetDay = 5;
							if (currentCalendar.get(Calendar.YEAR) > paymentCalendar.get(Calendar.YEAR)) {
								if (currentMonth == paymentTargetMonth) {
									int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
									if (currentDay > paymentTargetDay) {
										invalidList.add(p);
										continue;
									}
								}
							}
						}

					} catch (Exception e) {
						log4j.error("Exception" , e);
						continue;
					}
				}
			}
		}*/

		return list;
	}

	public synchronized void saveReceipts(PurchaseOrderDetail[] objArr, int id) {

		List<PurchaseOrderDetail> obj = new ArrayList<PurchaseOrderDetail>(Arrays.asList(objArr));
		List<PurchaseOrderDetail> objExcept = new ArrayList<PurchaseOrderDetail>(Arrays.asList(objArr));

		PurchaseOrder order = purchaseOrderDao.getOrderById(id);
		Supplier s = supplierService.searchByAddressNumber(order.getAddressNumber());
		String emailRecipient = (s.getEmailContactoVentas() + "," + s.getEmailSupplier());

		order.setPurchasOrderDetail(null);
		Set<PurchaseOrderDetail> set = new HashSet<PurchaseOrderDetail>();
		Set<PurchaseOrderDetail> setExcept = new HashSet<PurchaseOrderDetail>();

		for (PurchaseOrderDetail dtl : obj) {
			if (dtl.getToReject() > 0) {
				dtl.setRejected(dtl.getRejected() + dtl.getToReject());
				dtl.setStatus(AppConstants.STATUS_REJECT);
			}

			if (dtl.getToReceive() > 0) {
				dtl.setReceived(dtl.getReceived() + dtl.getToReceive());
				if (dtl.getPending() == 0) {
					dtl.setStatus(AppConstants.STATUS_RECEIVED);
				} else {
					if (dtl.getToReceive() > 0) {
						dtl.setStatus(AppConstants.STATUS_PARTIAL);
					}
				}
			}
			set.add(dtl);
		}
		order.setPurchasOrderDetail(set);

		boolean partial = false;
		double amountReceived = 0;
		Set<PurchaseOrderDetail> dtlList = order.getPurchaseOrderDetail();
		for (PurchaseOrderDetail dtl : dtlList) {
			amountReceived = amountReceived + dtl.getAmuntReceived();
			if (AppConstants.STATUS_PARTIAL.equals(dtl.getStatus())) {
				partial = true;
			}
		}

		order.setOrderAmount(amountReceived);
		if (partial) {
			order.setOrderStauts(AppConstants.STATUS_PARTIAL);
		} else {
			order.setOrderStauts(AppConstants.STATUS_RECEIVED);
		}

		purchaseOrderDao.updateOrders(order);

		if ("Y".equals(s.getInvException())) { // Proceso excepcional de recibo
			for (PurchaseOrderDetail dtl : objExcept) {
				if (dtl.getToReject() > 0) {
					dtl.setReceived(dtl.getToReject());
					dtl.setAmuntReceived(dtl.getReceived() * dtl.getUnitCost());
					dtl.setToReceive(dtl.getToReject());
					dtl.setToReject(0);
				}
				setExcept.add(dtl);
			}
			order.setPurchasOrderDetail(setExcept); // Override only for JDE
		}

		eDIService.createNewReceipt(order);

		emailService.sendEmail(AppConstants.EMAIL_INVOICE_SUBJECT,
				AppConstants.EMAIL_RECEIPT_COMPLETE + order.getOrderNumber() + "-" + order.getOrderType(),
				emailRecipient);
	}

	public void updateOrders(PurchaseOrder o) {
		purchaseOrderDao.updateOrders(o);
	}
	
	public void updateReceipts(List<Receipt> o) {
		purchaseOrderDao.updateReceipts(o);
	}

	public synchronized List<PurchaseOrder> saveMultiple(List<PurchaseOrder> list) {
		//log4j.info("*********** STEP 5: saveMultiple:listSize:" + list.size());
		return purchaseOrderDao.saveMultiple(list);
	}
	
	public List<Receipt> saveMultipleReceipt(List<Receipt> list) {
		for(Receipt r : list) {			
			if((r.getAmountReceived() < 0d || r.getForeignAmountReceived() < 0d) 
					&& AppConstants.JDE_RETENTION_CODE.equals(String.valueOf(r.getObjectAccount()).trim())) {
				
				r.setAmountReceived(Math.abs(r.getAmountReceived()));
				r.setForeignAmountReceived(Math.abs(r.getForeignAmountReceived()));
				r.setQuantityReceived(Math.abs(r.getQuantityReceived()));
				r.setReceiptType(AppConstants.RECEIPT_CODE_RETENTION);
			}
			r.setStatus(AppConstants.STATUS_OC_APPROVED);
			r.setPortalReceiptDate(new Date());
		}
		
		return purchaseOrderDao.saveMultipleReceipt(list);
	}

	public synchronized void updateMultiple(List<PurchaseOrder> list) {
		purchaseOrderDao.updateMultiple(list);
	}

	public int getTotalRecords() {
		return purchaseOrderDao.getTotalRecords();
	}

	public void getPurchaseOrderListBySelection(int orderNumber, String addressNumber, String fromDate, String toDate) {
		
		SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
		Date d;
		try {
			d = fmt.parse(fromDate);
			String fromDateJulian = JdeJavaJulianDateTools.Methods.getJulianDate(d);
			
			d = fmt.parse(toDate);
			String toDateJulian = JdeJavaJulianDateTools.Methods.getJulianDate(d);

			jDERestService.getPurchaseOrderListBySelection(orderNumber, addressNumber, fromDateJulian, toDateJulian, "", "");
		} catch (ParseException e) {
			log4j.error("ParseException" , e);
			e.printStackTrace();
		}
		 

	}

	public void getPurchaseOrderListBulk() {
		jDERestService.getPurchaseOrderList();
	}
	
	public void getPurchaseOrderDocumentDownloadBulk() {
		jDERestService.getPurchaseOrderDocumentDownload();
	}
		
	public void saveMultiplePayments(List<PurchaseOrderPayment> list) {
		purchaseOrderDao.saveMultiplePayments(list);
	}

	public PurchaseOrder searchbyOrderUuid(String uuid) {
		return purchaseOrderDao.searchbyOrderUuid(uuid);
	}
	
	public List<PurchaseOrderPayment> getAll() {
		return purchaseOrderDao.getAll();
	}

	@SuppressWarnings("unused")
	public String createForeignInvoice(ForeingInvoice inv) {

		PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBook(inv.getOrderNumber(), inv.getAddressNumber(),inv.getOrderType());
	  	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 	String userAuth = auth.getName();
	 	Date currentDate = new Date();
		String documentNumber ="";
		
		if (po != null) {
			try {
				List<Receipt> recepCon=purchaseOrderDao.getReceiptByFolio(inv.getInvoiceNumber());
				List<ForeignInvoiceTable> foreinTaCaon=purchaseOrderDao.getForeignInvoiceByNumber(inv.getInvoiceNumber());
				if(recepCon!=null||recepCon.size()>0||foreinTaCaon!=null||foreinTaCaon.size()>0) {
					return "ForeignInvoiceError_19";
				}
				
				
				boolean isPDFAttached = false;
				List<UserDocument> listDocumentAtt = documentsService.searchCriteriaByOrderNumber(inv.getOrderNumber(),inv.getOrderType(), inv.getAddressNumber());						
				if(listDocumentAtt != null && !listDocumentAtt.isEmpty()) {
					for(UserDocument document : listDocumentAtt) {
						if(org.apache.commons.lang.StringUtils.isBlank(document.getUuid())
								&& document.getContent() != null
								&& document.getName() != null 
								&& document.getName().toLowerCase().contains(".pdf") 
								) {
							isPDFAttached = true;
							break;
						}
					}
				}
				
				if(!isPDFAttached) {
					return "ForeignInvoiceError_1";
				}
				
				Supplier s = supplierService.searchByAddressNumber(inv.getAddressNumber());
				if(s == null) {
					return "ForeignInvoiceError_2";
				}
				String emailRecipient = (s.getEmailSupplier());
				
				String fechaFactura = inv.getExpeditionDate();
				fechaFactura = fechaFactura.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
				Date invDate = null;
				try {
					invDate = sdf.parse(fechaFactura);
				}catch(Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
				}
				
				
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, currentYear);
				cal.set(Calendar.DAY_OF_YEAR, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date startYear = cal.getTime();
				
				try {
					if(invDate.compareTo(startYear) < 0) {
						return "ForeignInvoiceError_3";
					}
				}catch(Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
					return "ForeignInvoiceError_4";
				}
				
				String oCurr = "";
				if("PME".equals(po.getCurrecyCode())) {
					oCurr = "MXN";
				}else {
					oCurr = po.getCurrecyCode();
				}
				
				String invCurrency = inv.getForeignCurrency();
				if(!invCurrency.equals(oCurr)) {
					return "ForeignInvoiceError_5";
				}
				
				List<Receipt> requestedReceiptList = null;
				List<Receipt> receiptArray= purchaseOrderDao.getOrderReceipts(inv.getOrderNumber(), inv.getAddressNumber(),inv.getOrderType(), "");
				if(receiptArray != null) {
					String[] idList = inv.getReceiptIdList().split(",");
					requestedReceiptList = new ArrayList<Receipt>();
					for(Receipt r : receiptArray) {
						if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
							requestedReceiptList.add(r);
						}
					}
				}else {
					return "ForeignInvoiceError_6";
			    }

				String domesticForeignCurrency = oCurr;
				boolean isDomestic = false;								
				List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
				if(comUDCList != null && !comUDCList.isEmpty()) {
					for(UDC company : comUDCList) {
						if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
							domesticForeignCurrency = company.getStrValue2().trim();
							isDomestic = true;
							break;
						}
					}
				}
				
				List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
				if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
					for(UDC supplier : supDomUDCList) {
						if(supplier.getStrValue1().trim().equals(inv.getAddressNumber().trim()) && !"".equals(supplier.getStrValue2().trim())) {
							domesticForeignCurrency = supplier.getStrValue2().trim();
							isDomestic = true;
							break;
						}
					}
				}

				UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
				UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");
				
				UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");
				
				UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
				UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
				
				double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
				double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;
				
				double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
				double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
				double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
				
				double orderAmount = 0;
				double invoiceAmount = 0;
				
				double discount = 0;
				
				for(Receipt r :requestedReceiptList) {
					if(isDomestic && domesticForeignCurrency.equals(invCurrency)) {
						orderAmount = orderAmount + r.getAmountReceived();	
					} else {
						orderAmount = orderAmount + r.getForeignAmountReceived();
					}
				}

				String tipoValidacion ="";
				
				double totalImporteMayor = orderAmount;
				double totalImporteMenor = orderAmount;
				
				if(montoLimite != 0) {
					if(inv.getForeignSubtotal() >= montoLimite) {
						totalImporteMayor = totalImporteMayor + montoLimiteMax;
						totalImporteMenor = totalImporteMenor - montoLimiteMin;
						tipoValidacion = "Por Monto";
					}else {
						totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
						totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
						tipoValidacion = "Por porcentaje";
					}
				}
				
				totalImporteMayor = totalImporteMayor * 100;
				totalImporteMayor = Math.round(totalImporteMayor);
				totalImporteMayor = totalImporteMayor /100;	
				
				totalImporteMenor = totalImporteMenor * 100;
				totalImporteMenor = Math.round(totalImporteMenor);
				totalImporteMenor = totalImporteMenor /100;	
				invoiceAmount = inv.getForeignSubtotal() - discount;
				
				String uuid = getUuid(po);
				if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
					return "ForeignInvoiceError_7";
				}else {
					inv.setUuid(uuid);
					int diasCred = 30 ;
					List<UDC> pmtTermsUdc = udcService.searchBySystem("PMTTERMS");					
					for(Receipt r :requestedReceiptList) {
						if(r.getPaymentTerms() != null && !"".equals(r.getPaymentTerms())) {
							for(UDC udcpmt : pmtTermsUdc) {
								if(udcpmt.getStrValue1().equals(r.getPaymentTerms().trim())) {
									diasCred = Integer.parseInt(udcpmt.getStrValue2());
									break;
								}
							}
						}
					}
					
					Date estimatedPaymentDate = null;
					if(invDate != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(new Date());
						c.add(Calendar.DATE, diasCred);
						List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, po.getAddressNumber());
						if(pc != null) {
							if(pc.size() > 0) {
								estimatedPaymentDate = pc.get(0).getPaymentDate();
							}else {
								estimatedPaymentDate = c.getTime();
							}
						}else {
							estimatedPaymentDate = c.getTime();
						}
					}
					
			        po.setInvoiceAmount(po.getInvoiceAmount() + inv.getForeignDebit());
			        po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
			        po.setInvoiceUploadDate(invDate);
			        po.setSentToWns(null);
			        //po.setEstimatedPaymentDate(estimatedPaymentDate);			        
			        purchaseOrderDao.updateOrders(po);
			        
			        for(Receipt r :requestedReceiptList) {
			        	r.setInvDate(invDate);
			        	r.setFolio(inv.getInvoiceNumber());
			        	r.setSerie("");
			        	r.setUuid(uuid);
			        	//r.setEstPmtDate(estimatedPaymentDate);
						r.setStatus(AppConstants.STATUS_OC_INVOICED);
						r.setUploadInvDate(new Date());
						documentNumber = documentNumber + r.getDocumentNumber() + ",";
					}
					purchaseOrderDao.updateReceipts(requestedReceiptList);
				}
				
				JAXBContext contextObj = JAXBContext.newInstance(ForeingInvoice.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				StringWriter sw = new StringWriter();
				marshallerObj.marshal(inv, sw);
				String xmlString = sw.toString();

				UserDocument doc = new UserDocument();
				doc.setAddressBook(inv.getAddressNumber());
				doc.setDocumentNumber(inv.getOrderNumber());
				doc.setDocumentType(inv.getOrderType());
				doc.setContent(xmlString.getBytes("UTF-8"));
				doc.setType("TEMP");
				doc.setName("FACT_FOR_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml");
				doc.setSize(xmlString.length());
				doc.setStatus(false);
				doc.setAccept(true);
				doc.setFiscalType("Factura");
				doc.setFolio("");
				doc.setSerie("");
				doc.setUuid("");
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsService.save(doc, new Date(), "");
				
	          	dataAuditService.saveDataAudit("CreateForeignInvoice",po.getAddressNumber(), currentDate, request.getRemoteAddr(),
	            userAuth, "FACT_FOR_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml", "createForeignInvoice", 
	            "Created Foreign Invoice Successful",documentNumber+"", po.getOrderNumber()+"", null, 
	            uuid, AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);    

				ForeignInvoiceTable fit = new ForeignInvoiceTable();
				fit.setAddressNumber(inv.getAddressNumber());
				fit.setOrderNumber(inv.getOrderNumber());
				fit.setOrderType(inv.getOrderType());
				fit.setCountry(inv.getCountry());
				fit.setInvoiceNumber(inv.getInvoiceNumber());
				fit.setExpeditionDate(inv.getExpeditionDate());
				fit.setReceptCompany(inv.getReceptCompany());
				fit.setForeignCurrency(inv.getForeignCurrency());
				fit.setForeignSubtotal(inv.getForeignSubtotal());
				fit.setForeignTaxes(inv.getForeignTaxes());
				fit.setForeignRetention(inv.getForeignRetention());
				fit.setForeignDebit(inv.getForeignDebit());
				fit.setForeignDescription(inv.getForeignDescription());
				fit.setForeignNotes(inv.getForeignNotes());
				fit.setUsuarioImpuestos(inv.getUsuarioImpuestos());
				fit.setTaxId(inv.getTaxId());
				fit.setUuid(uuid);
				purchaseOrderDao.saveForeignInvoice(fit);


				try {
					String resp;
					if(isDomestic && domesticForeignCurrency.equals(invCurrency)) {
						InvoiceDTO invDTO = new InvoiceDTO();
						invDTO.setTipoComprobante(AppConstants.RECEIPT_CODE_INVOICE);
						invDTO.setFechaTimbrado(inv.getExpeditionDate());
						invDTO.setSubTotal(inv.getForeignSubtotal());
						invDTO.setFolio(inv.getInvoiceNumber());
						invDTO.setSerie("");
						invDTO.setUuid(uuid);
						resp = "DOC:" + eDIService.createNewVoucher(po, invDTO, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);	
					} else {
						resp = "DOC:" + eDIService.createNewForeignVoucher(po, inv, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
					}
					
					//Enviar primer archivo adjunto a la factura foranea
					try {
						boolean isFileSent = false;
						List<UserDocument> listDocument = documentsService.searchCriteriaByOrderNumber(inv.getOrderNumber(),inv.getOrderType(), inv.getAddressNumber());						
						if(listDocument != null && !listDocument.isEmpty()) {
							for(UserDocument document : listDocument) {
								if(document.getName() != null && document.getName().toLowerCase().contains(".pdf") 
										&& document.getContent() != null && org.apache.commons.lang.StringUtils.isBlank(document.getUuid())) {
									
				                	File file = new File(System.getProperty("java.io.tmpdir")+"/"+ fit.getUuid() + ".pdf");
				                	Path filePath = Paths.get(file.getAbsolutePath());
				                	Files.write(filePath, document.getContent());
				                	document.setUuid(uuid);
				                	documentsService.update(document, null, null);
				                	log4j.info("FACTURA FORANEA: Se actualiza archivo " + fit.getUuid() + "");
				                	documentsService.sendFileToRemote(file, fit.getUuid() + ".pdf");
				                	log4j.info("FACTURA FORANEA: Se envía archivo " + fit.getUuid() + "");
				                	file.delete();
				                	isFileSent = true;
				                	break;
								}
							}
						}
						if(!isFileSent) {
							log4j.info("FACTURA FORANEA: No se envió ningún archivo para la factura " + fit.getUuid() + "");
						}
					} catch (Exception e) {
						log4j.error("Exception" , e);
						log4j.error("FACTURA FORANEA: Error al enviar el archivo para el UUID " + fit.getUuid() + ".\n" + e.getMessage());
						e.printStackTrace();
					}
					
					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(AppConstants.EMAIL_INV_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(AppConstants.ETHIC_CONTENT_INVOICE), emailRecipient);
					emailAsyncSup.setMailSender(mailSenderObj);
					emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
					Thread emailThreadSup = new Thread(emailAsyncSup);
					emailThreadSup.start();
					
					return "";
					
					
				} catch(Exception e) {
					log4j.error("Exception" , e);
					return "";
				}

			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return e.getMessage();
			}
		} else {
			return "ForeignInvoiceError_8";
		}
	}
	
	@SuppressWarnings("unused")
	public String createForeignCreditNote(ForeingInvoice inv) {

		PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBook(inv.getOrderNumber(), inv.getAddressNumber(),inv.getOrderType());
	  	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 	String userAuth = auth.getName();
	 	Date currentDate = new Date();
		String documentNumber ="";
		
		if (po != null) {
			try {
				List<Receipt> recepCon=purchaseOrderDao.getReceiptByFolio(inv.getInvoiceNumber());
				List<ForeignInvoiceTable> foreinTaCaon=purchaseOrderDao.getForeignInvoiceByNumber(inv.getInvoiceNumber());
				if(recepCon!=null||recepCon.size()>0||foreinTaCaon!=null||foreinTaCaon.size()>0) {
					return "ForeignInvoiceError_19";
				}
				
				boolean isPDFAttached = false;
				List<UserDocument> listDocumentAtt = documentsService.searchCriteriaByOrderNumber(inv.getOrderNumber(),inv.getOrderType(), inv.getAddressNumber());						
				if(listDocumentAtt != null && !listDocumentAtt.isEmpty()) {
					for(UserDocument document : listDocumentAtt) {
						if(org.apache.commons.lang.StringUtils.isBlank(document.getUuid())
								&& document.getContent() != null
								&& document.getName() != null 
								&& document.getName().toLowerCase().contains(".pdf") 
								) {
							isPDFAttached = true;
							break;
						}
					}
				}
				
				
				
				
				
				if(!isPDFAttached) {
					return "ForeignInvoiceError_1";
				}
				
				Supplier s = supplierService.searchByAddressNumber(inv.getAddressNumber());
				if(s == null) {
					return "ForeignInvoiceError_2";
				}
				String emailRecipient = (s.getEmailSupplier());
				
				String fechaFactura = inv.getExpeditionDate();
				fechaFactura = fechaFactura.replace("T", " ");
				SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN);
				Date invDate = null;
				try {
					invDate = sdf.parse(fechaFactura);
				}catch(Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
				}
				
				
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, currentYear);
				cal.set(Calendar.DAY_OF_YEAR, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date startYear = cal.getTime();
				
				try {
					if(invDate.compareTo(startYear) < 0) {
						return "ForeignInvoiceError_13";
					}
				}catch(Exception e) {
					log4j.error("Exception" , e);
					e.printStackTrace();
					return "ForeignInvoiceError_14";
				}
				
				String oCurr = "";
				if("PME".equals(po.getCurrecyCode())) {
					oCurr = "MXN";
				}else {
					oCurr = po.getCurrecyCode();
				}
				
				String invCurrency = inv.getForeignCurrency();
				if(!invCurrency.equals(oCurr)) {
					return "ForeignInvoiceError_15";
				}
				
				List<Receipt> requestedReceiptList = null;
				List<Receipt> receiptArray= purchaseOrderDao.getNegativeOrderReceipts(inv.getOrderNumber(), inv.getAddressNumber(),inv.getOrderType(), "");
				
				if(receiptArray != null) {
					String[] idList = inv.getReceiptIdList().split(",");
					requestedReceiptList = new ArrayList<Receipt>();
					for(Receipt r : receiptArray) {
						if(Arrays.asList(idList).contains(String.valueOf(r.getId()))) {
							requestedReceiptList.add(r);
						}
					}					
					if(requestedReceiptList.isEmpty()) {
						return "ForeignInvoiceError_16";
					}
				}else {
					return "ForeignInvoiceError_16";
			    }

				String domesticForeignCurrency = oCurr;
				boolean isDomestic = false;								
				List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
				if(comUDCList != null && !comUDCList.isEmpty()) {
					for(UDC company : comUDCList) {
						if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
							domesticForeignCurrency = company.getStrValue2().trim();
							isDomestic = true;
							break;
						}
					}
				}
				
				List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
				if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
					for(UDC supplier : supDomUDCList) {
						if(supplier.getStrValue1().trim().equals(inv.getAddressNumber().trim()) && !"".equals(supplier.getStrValue2().trim())) {
							domesticForeignCurrency = supplier.getStrValue2().trim();
							isDomestic = true;
							break;
						}
					}
				}

				UDC porcentajeMaxUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MAX");
				UDC porcentajeMinUdc = udcService.searchBySystemAndKey("PORCENTAJE", "MIN");
				
				UDC montoLimiteUdc = udcService.searchBySystemAndKey("MONTO", "LIMITE");
				
				UDC montoLimiteMaxUdc = udcService.searchBySystemAndKey("MONTO", "MAX");
				UDC montoLimiteMinUdc = udcService.searchBySystemAndKey("MONTO", "MIN");
				
				double porcentajeMax = Double.valueOf(porcentajeMaxUdc.getStrValue1()) / 100;
				double porcentajeMin = Double.valueOf(porcentajeMinUdc.getStrValue1()) / 100;
				
				double montoLimite = Double.valueOf(montoLimiteUdc.getStrValue1());
				double montoLimiteMax = Double.valueOf(montoLimiteMaxUdc.getStrValue1());
				double montoLimiteMin = Double.valueOf(montoLimiteMinUdc.getStrValue1());
				
				double orderAmount = 0;
				double invoiceAmount = 0;
				
				double discount = 0;
				
				for(Receipt r :requestedReceiptList) {
					if(isDomestic && domesticForeignCurrency.equals(invCurrency)) {
						orderAmount = orderAmount + (r.getAmountReceived()*(-1));	
					} else {
						orderAmount = orderAmount + (r.getForeignAmountReceived()*(-1));
					}
				}

				String tipoValidacion ="";
				
				double totalImporteMayor = orderAmount;
				double totalImporteMenor = orderAmount;
				
				if(montoLimite != 0) {
					if(inv.getForeignSubtotal() >= montoLimite) {
						totalImporteMayor = totalImporteMayor + montoLimiteMax;
						totalImporteMenor = totalImporteMenor - montoLimiteMin;
						tipoValidacion = "Por Monto";
					}else {
						totalImporteMayor = totalImporteMayor + (totalImporteMayor * porcentajeMax);
						totalImporteMenor = totalImporteMenor - (totalImporteMenor * porcentajeMin);
						tipoValidacion = "Por porcentaje";
					}
				}
				
				totalImporteMayor = totalImporteMayor * 100;
				totalImporteMayor = Math.round(totalImporteMayor);
				totalImporteMayor = totalImporteMayor /100;	
				
				totalImporteMenor = totalImporteMenor * 100;
				totalImporteMenor = Math.round(totalImporteMenor);
				totalImporteMenor = totalImporteMenor /100;	
				invoiceAmount = inv.getForeignSubtotal() - discount;
				
				String uuid = getUuid(po);
				if(totalImporteMayor < invoiceAmount || totalImporteMenor > invoiceAmount) {
					return "ForeignInvoiceError_17";
				}else {
					inv.setUuid(uuid);
					int diasCred = 30 ;
					List<UDC> pmtTermsUdc = udcService.searchBySystem("PMTTERMS");					
					for(Receipt r :requestedReceiptList) {
						if(r.getPaymentTerms() != null && !"".equals(r.getPaymentTerms())) {
							for(UDC udcpmt : pmtTermsUdc) {
								if(udcpmt.getStrValue1().equals(r.getPaymentTerms().trim())) {
									diasCred = Integer.parseInt(udcpmt.getStrValue2());
									break;
								}
							}
						}
					}
					
					Date estimatedPaymentDate = null;
					if(invDate != null) {
						Calendar c = Calendar.getInstance();
						c.setTime(new Date());
						c.add(Calendar.DATE, diasCred);
						List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, po.getAddressNumber());
						if(pc != null) {
							if(pc.size() > 0) {
								estimatedPaymentDate = pc.get(0).getPaymentDate();
							}else {
								estimatedPaymentDate = c.getTime();
							}
						}else {
							estimatedPaymentDate = c.getTime();
						}
					}
					
			        po.setInvoiceAmount(po.getInvoiceAmount() + inv.getForeignDebit());
			        po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
			        po.setInvoiceUploadDate(invDate);
			        po.setSentToWns(null);
			        //po.setEstimatedPaymentDate(estimatedPaymentDate);			        
			        purchaseOrderDao.updateOrders(po);
			        
			        for(Receipt r :requestedReceiptList) {
			        	r.setInvDate(invDate);
			        	r.setFolio(inv.getInvoiceNumber());
			        	r.setSerie("");
			        	r.setUuid(uuid);
			        	//r.setEstPmtDate(estimatedPaymentDate);
			        	r.setUploadInvDate(new Date());
						r.setStatus(AppConstants.STATUS_OC_INVOICED);
						documentNumber = documentNumber + r.getDocumentNumber() + ",";
					}
					purchaseOrderDao.updateReceipts(requestedReceiptList);
				}
				
				JAXBContext contextObj = JAXBContext.newInstance(ForeingInvoice.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				StringWriter sw = new StringWriter();
				marshallerObj.marshal(inv, sw);
				String xmlString = sw.toString();

				UserDocument doc = new UserDocument();
				doc.setAddressBook(inv.getAddressNumber());
				doc.setDocumentNumber(inv.getOrderNumber());
				doc.setDocumentType(inv.getOrderType());
				doc.setContent(xmlString.getBytes("UTF-8"));
				doc.setType("TEMP");
				doc.setName("NC_FOR_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml");
				doc.setSize(xmlString.length());
				doc.setStatus(false);
				doc.setAccept(true);
				doc.setFiscalType("NotaCredito");
				doc.setFolio("");
				doc.setSerie("");
				doc.setUuid("");
				doc.setUploadDate(new Date());
				doc.setFiscalRef(0);
				documentsService.save(doc, new Date(), "");
				
	          	dataAuditService.saveDataAudit("CreateForeignCreditNote",po.getAddressNumber(), currentDate, request.getRemoteAddr(),
	            userAuth, "NC_FOR_OC_" + po.getOrderNumber() + "_" + po.getOrderType() + ".xml", "createForeignCreditNote", 
	            "Created Foreign Credit Note Successful",documentNumber+"", po.getOrderNumber()+"", null, 
	            uuid, AppConstants.STATUS_COMPLETE, AppConstants.SALESORDER_MODULE);    

				ForeignInvoiceTable fit = new ForeignInvoiceTable();
				fit.setAddressNumber(inv.getAddressNumber());
				fit.setOrderNumber(inv.getOrderNumber());
				fit.setOrderType(inv.getOrderType());
				fit.setCountry(inv.getCountry());
				fit.setInvoiceNumber(inv.getInvoiceNumber());
				fit.setExpeditionDate(inv.getExpeditionDate());
				fit.setReceptCompany(inv.getReceptCompany());
				fit.setForeignCurrency(inv.getForeignCurrency());
				fit.setForeignSubtotal(inv.getForeignSubtotal()*-1);
				fit.setForeignTaxes(inv.getForeignTaxes()*-1);
				fit.setForeignRetention(inv.getForeignRetention()*-1);
				fit.setForeignDebit(inv.getForeignDebit()*-1);
				fit.setForeignDescription(inv.getForeignDescription());
				fit.setForeignNotes(inv.getForeignNotes());
				fit.setUsuarioImpuestos(inv.getUsuarioImpuestos());
				fit.setTaxId(inv.getTaxId());
				fit.setUuid(uuid);
				purchaseOrderDao.saveForeignInvoice(fit);


				try {
					String resp;
					if(isDomestic && domesticForeignCurrency.equals(invCurrency)) {
						InvoiceDTO invDTO = new InvoiceDTO();
						invDTO.setTipoComprobante(AppConstants.RECEIPT_CODE_INVOICE);
						invDTO.setFechaTimbrado(inv.getExpeditionDate());
						invDTO.setSubTotal(inv.getForeignSubtotal());
						invDTO.setFolio(inv.getInvoiceNumber());
						invDTO.setSerie("");
						invDTO.setUuid(uuid);
						resp = "DOC:" + eDIService.createNewVoucher(po, invDTO, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);	
					} else {
						resp = "DOC:" + eDIService.createNewForeignVoucher(po, inv, 0, s, requestedReceiptList, AppConstants.NN_MODULE_VOUCHER);
					}
					
					//Enviar primer archivo adjunto a la factura foranea
					try {
						boolean isFileSent = false;
						List<UserDocument> listDocument = documentsService.searchCriteriaByOrderNumber(inv.getOrderNumber(),inv.getOrderType(), inv.getAddressNumber());						
						if(listDocument != null && !listDocument.isEmpty()) {
							for(UserDocument document : listDocument) {
								if(document.getName() != null && document.getName().toLowerCase().contains(".pdf") 
										&& document.getContent() != null && org.apache.commons.lang.StringUtils.isBlank(document.getUuid())) {
									
				                	File file = new File(System.getProperty("java.io.tmpdir")+"/"+ fit.getUuid() + ".pdf");
				                	Path filePath = Paths.get(file.getAbsolutePath());
				                	Files.write(filePath, document.getContent());
				                	document.setUuid(uuid);
				                	documentsService.update(document, null, null);
				                	log4j.info("NOTA CRÉDITO FORANEA: Se actualiza archivo " + fit.getUuid() + "");
				                	documentsService.sendFileToRemote(file, fit.getUuid() + ".pdf");
				                	log4j.info("NOTA CRÉDITO FORANEA: Se envía archivo " + fit.getUuid() + "");
				                	file.delete();
				                	isFileSent = true;
				                	break;
								}
							}
						}
						if(!isFileSent) {
							log4j.info("NOTA CRÉDITO FORANEA: No se envió ningún archivo para la nota de crédito " + fit.getUuid() + "");
						}
					} catch (Exception e) {
						log4j.error("Exception" , e);
						log4j.error("NOTA CRÉDITO FORANEA: Error al enviar el archivo para el UUID " + fit.getUuid() + ".\n" + e.getMessage());
						e.printStackTrace();
					}
					
					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(AppConstants.EMAIL_INV_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(AppConstants.ETHIC_CONTENT_INVOICE), emailRecipient);
					emailAsyncSup.setMailSender(mailSenderObj);
					emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
					Thread emailThreadSup = new Thread(emailAsyncSup);
					emailThreadSup.start();
					
					return "";
					
					
				} catch(Exception e) {
					log4j.error("Exception" , e);
					return "";
				}

			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return e.getMessage();
			}
		} else {
			return "ForeignInvoiceError_18";
		}
	}

	public String createForeignInvoiceWithoutOrder(FileConceptUploadBean uploadItem, ForeingInvoice inv, double dblAdvancePayment) {

		try {        	
			boolean isPDFAttached = false;
			List<UserDocument> listDocumentAtt = documentsService.searchCriteriaByRefFiscal(inv.getAddressNumber(), inv.getUuid());						
			if(listDocumentAtt != null && !listDocumentAtt.isEmpty()) {
				for(UserDocument document : listDocumentAtt) {
					if(document.getContent() != null && document.getName() != null && document.getName().toLowerCase().contains(".pdf")) {
						isPDFAttached = true;
						break;
					}
				}
			}
			
			if(!isPDFAttached) {
				return "ForeignInvoiceError_1";
			}
			
			List<ForeignInvoiceTable> foreinTaCaon=purchaseOrderDao.getForeignInvoiceByNumber(inv.getInvoiceNumber());
			if(foreinTaCaon!=null||foreinTaCaon.size()>0) {
				return "ForeignInvoiceError_19";
			}
			//New Fiscal Document
        	FiscalDocuments fiscalDoc = new FiscalDocuments();
        	fiscalDoc.setAddressNumber(inv.getAddressNumber());
        	fiscalDoc.setCompanyFD(inv.getReceptCompany());
        	
			Supplier s = supplierService.searchByAddressNumber(inv.getAddressNumber());
			if(s != null) {
				fiscalDoc.setSupplierName(s.getName());			
			} else {
				return "ForeignInvoiceError_2";
			}
        	
			String company = inv.getReceptCompany();
			String fechaFactura = inv.getExpeditionDate();
			String fechaFacturaNueva = fechaFactura;
			fechaFactura = fechaFactura.replace("T", " ");
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
			SimpleDateFormat sdfNew = new SimpleDateFormat(TIMESTAMP_DATE_PATTERN_NEW);
			Date invDate = null;
			try {
				invDate = sdf.parse(fechaFactura);
				fechaFacturaNueva = sdfNew.format(invDate);
				fechaFacturaNueva = fechaFacturaNueva.replace(" ", "T");
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}

			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, currentYear);
			cal.set(Calendar.DAY_OF_YEAR, 1);    
			Date startYear = cal.getTime();
			try {
				if(invDate.compareTo(startYear) < 0) {
					return "ForeignInvoiceError_3";
				}
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return "ForeignInvoiceError_4";
			}
        	
			String invCurrency = inv.getForeignCurrency().trim();
			UDC companyRfc = udcDao.searchBySystemAndKey("RFCCOMPANYCC", company);
			if(companyRfc != null) {
				fiscalDoc.setRfcReceptor(companyRfc.getStrValue1());
				fiscalDoc.setCentroCostos(org.apache.commons.lang.StringUtils.leftPad(companyRfc.getStrValue2(), 12, " "));				
			} else {
				return "ForeignInvoiceError_9";
			}
		
			String accountingAcc = "";
			List<UDC> accountingAccRfc = udcDao.searchBySystem("RFCACCOUNTINGACC");
			if(accountingAccRfc != null) {
				for(UDC udcAcc : accountingAccRfc) {
					if(udcAcc.getUdcKey().equals(company) && udcAcc.getStrValue1().equals(invCurrency)) {
						accountingAcc = udcAcc.getStrValue2().trim();
						break;
					}
				}
			}
			
			if(!"".equals(accountingAcc)) {
				fiscalDoc.setAccountingAccount(accountingAcc);
			} else {
				return "ForeignInvoiceError_10";
			}
			
	    	boolean isTransportCB = false;
	    	UDC udcTrans = udcService.searchBySystemAndKey("CUSTOMBROKER", fiscalDoc.getAddressNumber());    	
	    	if(udcTrans != null) {
				if(udcTrans.getStrValue1() != null && "Y".equals(udcTrans.getStrValue1().trim())) {
					isTransportCB = true;
				}
	    	}
	    	
	    	UDC accountingNumber = udcDao.searchBySystemAndKey("CPTACCNUMBER", "CONCEPT000");
	    	if(accountingNumber != null) {
				if(!isTransportCB) {
					fiscalDoc.setAccountNumber(fiscalDoc.getCentroCostos().concat(".").concat(accountingNumber.getStrValue1()));
				} else {
					fiscalDoc.setAccountNumber(fiscalDoc.getCentroCostos().concat(".").concat(accountingNumber.getStrValue2()));						
				}	
	    	} else {
	    		return "ForeignInvoiceError_11";
	    	}
			
	    	if(!AppConstants.DEFAULT_CURRENCY.equals(invCurrency)) {
	    		double exchangeRate;
	    		exchangeRate = this.getCurrentExchangeRate(new Date(), AppConstants.DEFAULT_CURRENCY_JDE, invCurrency);
				if(exchangeRate > 0D) {
					fiscalDoc.setTipoCambio(exchangeRate);					
				} else {
					return "ForeignInvoiceError_12";
				}	
	    	}
			
			int diasCredito = 0;
			UDC pmtTermsUDC = udcDao.searchBySystemAndKey("PMTTCUSTOM", "DEFAULT");
			if(pmtTermsUDC != null) {
				fiscalDoc.setPaymentTerms(pmtTermsUDC.getStrValue1());
				diasCredito = Integer.valueOf(pmtTermsUDC.getStrValue2());
			} else {
				fiscalDoc.setPaymentTerms("N30");
				diasCredito = 30;
			}
			
			String emailApprover = "";
			List<UDC> approverUDCList = udcDao.searchBySystem("APPROVERINV");
			if(approverUDCList != null) {
				for(UDC approver : approverUDCList) {
					if(AppConstants.INV_FIRST_APPROVER.equals(approver.getUdcKey())){
						fiscalDoc.setCurrentApprover(approver.getStrValue1());
						emailApprover = approver.getStrValue2();
					}

					if(AppConstants.INV_SECOND_APPROVER.equals(approver.getUdcKey())){
						fiscalDoc.setNextApprover(approver.getStrValue1());
					}				
				}
			}
	    	
			Date estimatedPaymentDate = null;
			Date currentDate = new Date();
			Calendar c = Calendar.getInstance();		
			c.setTime(currentDate);
			c.add(Calendar.DATE, diasCredito);
			List<PaymentCalendar> pc = paymentCalendarService.getPaymentCalendarFromToday(c.getTime(), 0, 500, s.getAddresNumber());
			if(pc != null) {
				if(pc.size() > 0) {
					estimatedPaymentDate = pc.get(0).getPaymentDate();
				}else {
					estimatedPaymentDate = c.getTime();
				}
			}else {
				estimatedPaymentDate = c.getTime();
			}
        	
			//fiscalDoc.setEstimatedPaymentDate(estimatedPaymentDate);
			fiscalDoc.setInvoiceDate(fechaFacturaNueva);
			fiscalDoc.setCurrencyCode(invCurrency);
			fiscalDoc.setCurrencyMode(AppConstants.CURRENCY_MODE_FOREIGN);
			fiscalDoc.setGlOffset(AppConstants.GL_OFFSET_FOREIGN);
			fiscalDoc.setTaxCode(AppConstants.INVOICE_TAX0);
			fiscalDoc.setSerie("");
			fiscalDoc.setStatus(AppConstants.STATUS_INPROCESS);
			fiscalDoc.setApprovalStatus(AppConstants.STATUS_INPROCESS);
			fiscalDoc.setApprovalStep(AppConstants.FIRST_STEP);			
			fiscalDoc.setFolio(inv.getFolio());
			fiscalDoc.setSerie(inv.getSerie());
			fiscalDoc.setUuidFactura(inv.getUuid());
			fiscalDoc.setType(AppConstants.STATUS_FACT_FOREIGN);                	
			fiscalDoc.setRfcEmisor("");
			fiscalDoc.setSubtotal(inv.getForeignSubtotal());
			fiscalDoc.setAmount(inv.getForeignDebit());
			fiscalDoc.setMoneda(inv.getForeignCurrency());
			fiscalDoc.setDescuento(0);
			fiscalDoc.setImpuestos(0);			
        	fiscalDoc.setAdvancePayment(dblAdvancePayment);
        	fiscalDoc.setInvoiceUploadDate(new Date());
        	
        	//Create concept list
        	fiscalDocumentService.createConceptList(uploadItem, fiscalDoc, false);
        	
        	//Save Fiscal Document
        	fiscalDocumentService.saveDocument(fiscalDoc);
        	
        	//Save Concept Documents
        	documentsService.save(uploadItem, inv.getAddressNumber(), inv.getUuid());
			
			JAXBContext contextObj = JAXBContext.newInstance(ForeingInvoice.class);
			Marshaller marshallerObj = contextObj.createMarshaller();
			marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter sw = new StringWriter();
			marshallerObj.marshal(inv, sw);
			String xmlString = sw.toString();

			UserDocument doc = new UserDocument();
			doc.setAddressBook(inv.getAddressNumber());
			doc.setDocumentNumber(0);
			doc.setDocumentType("Honorarios");
			doc.setContent(xmlString.getBytes("UTF-8"));
			doc.setType("TEMP");
			doc.setName(inv.getUuid() + ".xml");
			doc.setSize(xmlString.length());
			doc.setStatus(false);
			doc.setAccept(true);
			doc.setFiscalType("Factura");
			doc.setFolio(inv.getFolio());
			doc.setSerie("");
			doc.setUuid(inv.getUuid());
			doc.setUploadDate(new Date());
			doc.setFiscalRef(0);
			doc.setDescription("MainUUID_".concat(inv.getUuid()));
			documentsService.save(doc, new Date(), "");

			ForeignInvoiceTable fit = new ForeignInvoiceTable();
			fit.setAddressNumber(inv.getAddressNumber());
			fit.setOrderNumber(inv.getOrderNumber());
			fit.setOrderType(inv.getOrderType());
			fit.setCountry(inv.getCountry());
			fit.setInvoiceNumber(inv.getInvoiceNumber());
			fit.setExpeditionDate(fechaFacturaNueva);
			fit.setReceptCompany(inv.getReceptCompany());
			fit.setForeignCurrency(inv.getForeignCurrency());
			fit.setForeignSubtotal(inv.getForeignSubtotal());
			fit.setForeignTaxes(inv.getForeignTaxes());
			fit.setForeignRetention(inv.getForeignRetention());
			fit.setForeignDebit(inv.getForeignDebit());
			fit.setForeignDescription(inv.getForeignDescription());
			fit.setForeignNotes(inv.getForeignNotes());
			fit.setUsuarioImpuestos(inv.getUsuarioImpuestos());
			fit.setTaxId(inv.getTaxId());
			fit.setUuid(inv.getUuid());
			fit.setReceptCompany("");
			fit.setUsuarioImpuestos("");
			purchaseOrderDao.saveForeignInvoice(fit);
			
			try {
		    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		        emailAsyncSup.setProperties(AppConstants.EMAIL_INV_REQUEST_NO_OC, this.stringUtils.prepareEmailContent(AppConstants.EMAIL_INV_APPROVAL_MSG_1_NO_OC + inv.getUuid()  + AppConstants.EMAIL_INV_APPROVAL_MSG_2_NO_OC + s.getAddresNumber() +  "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK), emailApprover);
		        emailAsyncSup.setMailSender(mailSenderObj);
		        Thread emailThreadSup = new Thread(emailAsyncSup);
		        emailThreadSup.start();
			} catch (Exception e) {	
				log4j.error("Exception" , e);
			}
			
			return "";
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	public String acceptForeignInvoice(ForeingInvoice inv) {

		PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBook(inv.getOrderNumber(), inv.getAddressNumber(),
				inv.getOrderType());
		if (po != null) {
			try {

				UserDocument current = null;
				List<UserDocument> list = documentsService.searchCriteriaByOrderNumber(inv.getOrderNumber(),
						inv.getOrderType(), inv.getAddressNumber());
				for (UserDocument o : list) {
					if (o.getType().equals("TEMP")) {
						o.setType("FINAL");
						current = o;
						break;
					}
				}

				if (current != null) {
					
					if(inv.getUuid() == null) {
						inv.setUuid(getUuid(po));
					}
					
					JAXBContext contextObj = JAXBContext.newInstance(ForeingInvoice.class);
					Marshaller marshallerObj = contextObj.createMarshaller();
					marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

					StringWriter sw = new StringWriter();
					marshallerObj.marshal(inv, sw);
					String xmlString = sw.toString();
					current.setContent(xmlString.getBytes("UTF-8"));
					documentsService.update(current, new Date(), "");

					ForeignInvoiceTable fit = purchaseOrderDao.getForeignInvoice(inv);
					if (fit != null) {
						fit.setAddressNumber(inv.getAddressNumber());
						fit.setOrderNumber(inv.getOrderNumber());
						fit.setOrderType(inv.getOrderType());
						fit.setCountry(inv.getCountry());
						fit.setExpeditionDate(inv.getExpeditionDate());
						fit.setReceptCompany(inv.getReceptCompany());
						fit.setForeignCurrency(inv.getForeignCurrency());
						fit.setForeignSubtotal(inv.getForeignSubtotal());
						fit.setForeignTaxes(inv.getForeignTaxes());
						fit.setForeignRetention(inv.getForeignRetention());
						fit.setForeignDebit(inv.getForeignDebit());
						fit.setForeignDescription(inv.getForeignDescription());
						fit.setForeignNotes(inv.getForeignNotes());
						fit.setUsuarioImpuestos(inv.getUsuarioImpuestos());
						fit.setTaxId(inv.getTaxId());
						fit.setUuid(inv.getUuid());
						purchaseOrderDao.saveForeignInvoice(fit);
					}

				}

				po.setInvoiceAmount(inv.getForeignDebit());
				po.setStatus(AppConstants.STATUS_OC_INVOICED);
				po.setOrderStauts(AppConstants.STATUS_OC_INVOICED);
				purchaseOrderDao.updateOrders(po);

				String emailRecipient = "";
				UDC udc = udcService.getUdcById(Integer.valueOf(inv.getUsuarioImpuestos()));
				emailRecipient = udc.getStrValue1();

				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup
						.setProperties(
								AppConstants.EMAIL_INV_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(),
								stringUtils.prepareEmailContent(AppConstants.EMAIL_FOREIGINVOICE_RECEIVED
										+ po.getOrderNumber() + "-" + po.getOrderType()),
								emailRecipient + "," + po.getEmail());
				emailAsyncSup.setMailSender(mailSenderObj);
				emailAsyncSup.setAdditionalReference(udcDao, po.getOrderType());
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();

				return "";

			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return e.getMessage();
			}
		} else {
			return "No existen órdenes de compra para la factura solicitada";
		}

	}

	public ForeingInvoice searchForeignInvoice(String addressBook, int orderNumber, String orderType) {

		UserDocument doc = documentsService.searchCriteriaByOrderNumberFiscalType(orderNumber, orderType, addressBook,
				"Factura");
		if (doc != null) {
			try {
				String docDetail = new String(doc.getContent());
				JAXBContext jaxbContext = JAXBContext.newInstance(ForeingInvoice.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

				StringReader reader = new StringReader(docDetail);
				ForeingInvoice inv = (ForeingInvoice) unmarshaller.unmarshal(reader);
				return inv;

			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return null;
			}
		}

		return null;

	}

	public PurchaseOrder searchbyOrderAndAddressBook(int orderNumber, String addressNumber, String orderType) {
		return purchaseOrderDao.searchbyOrderAndAddressBook(orderNumber, addressNumber, orderType);
	}

	public List<LogData> getLogDataBayDate(Date startDate, Date endDate, String logType, int start, int limit) {
		return logger.getLogDataBayDate(startDate, endDate, logType, start, limit);
	}
	
	public List<InvoiceCodesDTO> getInvoiceCodes(int orderNumber, String addressNumber, String orderType){
		
		List<InvoiceCodesDTO> codes = new ArrayList<InvoiceCodesDTO>();
		try {
		String xmlContent = "";
		List<UserDocument> docList = documentsService.searchCriteriaByOrderNumber(orderNumber,orderType, addressNumber);
		if (docList != null) {
			if (!docList.isEmpty()) {
				for (UserDocument d : docList) {
					if(AppConstants.INVOICE_FIELD.equals(d.getFiscalType())){
						    InputStream is = new ByteArrayInputStream(d.getContent());
						    StringBuilder textBuilder = new StringBuilder();
						    try (Reader reader = new BufferedReader(new InputStreamReader
						    	      (is, Charset.forName(StandardCharsets.UTF_8.name())))) {
						    	        int c = 0;
						    	        while ((c = reader.read()) != -1) {
						    	            textBuilder.append((char) c);
						    	        }
						    	    }
					        xmlContent = textBuilder.toString();
					        if(!"".equals(xmlContent)) {
					        	InvoiceDTO inv =  null;
								if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
									inv = xmlToPojoService.convertV4(xmlContent);
								} else {
									inv = xmlToPojoService.convert(xmlContent);
								}
					        	if(inv != null) {
					        		List<Concepto> conceptosFactura = inv.getConcepto();
					        		for(Concepto c : conceptosFactura) {
					        			InvoiceCodesDTO obj = new InvoiceCodesDTO();
					        			obj.setCode(c.getClaveProdServ());
					        			obj.setUom(c.getUnidad());
					        			obj.setDescription(c.getDescripcion());
					        			obj.setAmount(Double.valueOf(c.getImporte()));
					        			codes.add(obj);
					        		}
					        	}
					        }
					        break;
					}
					documentsService.delete(d.getId(),"");
				}
			}
		}
		}catch(Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		
		if(codes.size()>0) {
			List<String> codeList = new ArrayList<String>();
			for(InvoiceCodesDTO o : codes) {
				codeList.add(o.getCode());
			}
			List<CodigosSAT> codeListSAT = codigosSATService.findCodes(codeList);
			for(CodigosSAT o : codeListSAT) {
				for(InvoiceCodesDTO p : codes) {
					if(p.getCode().equals(o.getCodigoSAT())) {
						p.setDescriptionSAT(o.getDescripcion());
					}
				}
			}
		}
		
		return codes;
	}
	
	private String getUuid(PurchaseOrder po) {
		StringBuilder str = new StringBuilder();
		String supNbr = org.apache.commons.lang.StringUtils.leftPad(po.getAddressNumber(),8,"0");
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
		str.append(org.apache.commons.lang.StringUtils.leftPad(po.getCompanyKey(),4,"0"));
		str.append(org.apache.commons.lang.StringUtils.leftPad(String.valueOf(po.getOrderNumber()),7,"0"));
		return str.toString();
	}
	
	public double getCurrentExchangeRate(Date invoiceDate, String currencyCode, String currencyTarget) {
    	double exchangeRate = 0D;
    	List<ExchangeRate> erToday = exchangeRateService.getExchangeRateFromToday(invoiceDate, currencyCode, currencyTarget, 0, 50);
		if(erToday != null && erToday.size() > 0) {
			exchangeRate = erToday.get(0).getExchangeRate();
		}
		
		if(exchangeRate == 0D) {
	    	List<ExchangeRate> erBefore = exchangeRateService.getExchangeRateBeforeToday(invoiceDate, currencyCode, currencyTarget, 0, 50);
	    	if(erBefore != null && erBefore.size() > 0) {
	    		exchangeRate = erBefore.get(0).getExchangeRate();
			}
		}

		if(exchangeRate == 0D) {
	    	List<ExchangeRate> erAfter = exchangeRateService.getExchangeRateBeforeToday(invoiceDate, currencyCode, currencyTarget, 0, 50);
	    	if(erAfter != null && erAfter.size() > 0) {
	    		exchangeRate = erAfter.get(0).getExchangeRate();
			}
		}
		
		return exchangeRate;
	}
	
	public List<Receipt> getOrderReceipts(int orderNumber,String addressBook, String orderType, String orderCompany) {
		
		List<Receipt> list = purchaseOrderDao.getOrderReceipts(orderNumber, addressBook, orderType, orderCompany);
		try {
			//Se busca la Fecha Estimada de Pago en JDE
			if(list != null && !list.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				List<PurchaseOrderGridDTO> listDTO = new ArrayList<PurchaseOrderGridDTO>();
				for(Receipt r : list) {
					if (r.getFolio() != null && !"".equals(r.getFolio().trim())) {
						
						String invNbr = "";						
						String folio = "";						
						if(r.getFolio() != null && !"null".equals(r.getFolio()) && !"NULL".equals(r.getFolio()) ) {
							folio = r.getFolio();
						}
						
						String serie = "";
						if(r.getSerie() != null && !"null".equals(r.getSerie()) && !"NULL".equals(r.getSerie()) ) {
							serie = r.getSerie();
						}
						
						invNbr = serie + folio;						
						PurchaseOrderGridDTO dto = new PurchaseOrderGridDTO();
						dto.setAddressNumber(r.getAddressNumber());
						dto.setOrderNumber(String.valueOf(r.getOrderNumber()));
						dto.setOrderType(r.getOrderType());
						dto.setOrderCompany(r.getOrderCompany());
						dto.setInvoiceNumber(invNbr);
						listDTO.add(dto);
					}
				}
				
				if(listDTO != null && !listDTO.isEmpty()) {
					List<PurchaseOrderGridDTO> respJDE = jdeRestService.getEstPmtDate(listDTO);						
					if (respJDE != null && !respJDE.isEmpty()) {
						for(Receipt r : list) {
							if (r.getUuid() != null && !"".equals(r.getUuid().trim())) {
								
								String folio = "";						
								if(r.getFolio() != null && !"null".equals(r.getFolio()) && !"NULL".equals(r.getFolio()) ) {
									folio = r.getFolio();
								}
								
								String serie = "";
								if(r.getSerie() != null && !"null".equals(r.getSerie()) && !"NULL".equals(r.getSerie()) ) {
									serie = r.getSerie();
								}

								//Si la factura no tiene folio, se asignan los últimos 12 caracteres del UUID
								if("".equals(folio) && r.getUuid() != null && r.getUuid().length() >= 12) {
									folio = r.getUuid().substring(r.getUuid().length() - 12).replaceAll("[^a-zA-Z0-9]", "");
									serie = "";
								}
								
								String vinv = "";
								vinv = serie + folio;
								
								//Si el vinv tiene mas de 25 caracteres, se asignan los últimos 12 caracteres del UUID
								if(vinv.length() > 25 && r.getUuid() != null && r.getUuid().length() >= 12) {
									vinv = r.getUuid().substring(r.getUuid().length() - 12).replaceAll("[^a-zA-Z0-9]", "");
								}
								
								if(!"".equals(vinv)) {
									for (PurchaseOrderGridDTO o : respJDE) {
										if (o.getAddressNumber().equals(r.getAddressNumber())
												&& o.getOrderNumber().equals(String.valueOf(r.getOrderNumber()))
												&& o.getOrderType().equals(r.getOrderType())
												&& o.getOrderCompany().equals(r.getOrderCompany())
												&& o.getInvoiceNumber().trim().equals(vinv)
												&& o.getEstPmtDateInt2() > 0) {
											
											//Setea la fecha a partir de la fecha Juliana (Se convierte la fecha del lado del Portal)
											r.setEstPmtDateStr(sdf.format(JdeJavaJulianDateTools.Methods.JulianDateToJavaDate(String.valueOf(o.getEstPmtDateInt2()))));
											break;
										}
									}
								}
							}
						}
					}
					
					for(Receipt r : list) {
						if (r.getUuid() != null && !"".equals(r.getUuid().trim())) {
							//Si no se encontró la factura se coloca N/A
							if(!(r.getEstPmtDateStr() != null && !"".equals(r.getEstPmtDateStr()))) {
								r.setEstPmtDateStr("N/A");
							}
						}
					}
				}
			}				
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
		return list;
	}
	
	public List<Receipt> getComplPendingReceipts(String addressBook) {
		Map<String, Receipt> rm = new HashMap<String, Receipt>();
		List<Receipt> returnList = new ArrayList<Receipt>();
		List<Receipt> receiptList = purchaseOrderDao.getComplPendingReceipts(addressBook);
		if(receiptList != null) {
			if(receiptList.size() > 0) {
				for(Receipt o : receiptList) {
					Receipt rec = rm.get(o.getUuid());
					if(rec != null) {
						continue;
					}else {
						rm.put(o.getUuid(), o);
					}
				}
			}
		}
		
		for (Map.Entry<String, Receipt> entry : rm.entrySet()) {
			returnList.add(entry.getValue());
		}

		return returnList;
	}
	
	public List<ReceiptInvoice> getComplPendingReceiptInvoice(String addressBook) {
		return purchaseOrderDao.getComplPendingReceiptInvoice(addressBook);
	}
	
	public List<Receipt> getNegativeOrderReceipts(int orderNumber,String addressBook, String orderType, String orderCompany) {
		return purchaseOrderDao.getNegativeOrderReceipts(orderNumber, addressBook, orderType, orderCompany);
	}
	
	public List<Receipt> getSuplierInvoicedReceipts(String addressNumber, String uuid) {
		return purchaseOrderDao.getSuplierInvoicedReceipts(addressNumber, uuid);
	}
	
	public void updatePaymentReceipts(List<Receipt> list) {
		purchaseOrderDao.updatePaymentReceipts(list);
	}
	
	public List<Receipt> getReceiptsByUUID(String uuid) {
		return purchaseOrderDao.getReceiptsByUUID(uuid);
	}

	public void updateReceipt(Receipt o) {
		purchaseOrderDao.updateReceipt(o);
	}
	
	public void saveReceiptInvoice(ReceiptInvoice o) {
		purchaseOrderDao.saveReceiptInvoice(o);
	}
	
	public List<ReceiptInvoice> getReceiptsInvoiceByUUID(String uuid) {
		return purchaseOrderDao.getReceiptsInvoiceByUUID(uuid);
	}
	
	public String getPendingReceiptsComplPago(String addressNumber) {
		List<String> pendingList = new ArrayList<String>();
		List<Receipt> invalidList = new ArrayList<Receipt>();
		List<Receipt> list = purchaseOrderDao.getPendingReceiptsComplPago(addressNumber);
		
		for(Receipt r : list) {
			if(r.getPaymentDate() != null) {
				try {
					Date pDate = r.getPaymentDate();
					Date currentDate = new Date();

					Calendar paymentCalendar = Calendar.getInstance();
					Calendar currentCalendar = Calendar.getInstance();
					paymentCalendar.setTime(pDate);
					currentCalendar.setTime(currentDate);

					int paymentMonth = paymentCalendar.get(Calendar.MONTH) + 1;
					int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;

					if (paymentMonth < 12) {
						int paymentTargetMonth = paymentMonth + 1;
						int paymentTargetDay = 5;

						// Same Year
						if (paymentCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
							if (currentMonth == paymentTargetMonth) {
								int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
								if (currentDay > paymentTargetDay) {
									invalidList.add(r);
									continue;
								}
							}
							if (currentMonth > paymentTargetMonth) {
								invalidList.add(r);
								continue;
							}

						}
						
						// Previous Year
						if (paymentCalendar.get(Calendar.YEAR) < currentCalendar.get(Calendar.YEAR)) {
							if (currentMonth == paymentTargetMonth) {
								int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
								if (currentDay > paymentTargetDay) {
									invalidList.add(r);
									continue;
								}
							}
							
							if (currentMonth < paymentTargetMonth) {
								invalidList.add(r);
								continue;
							}
						}

					} else {
						// Next year
						int paymentTargetMonth = 1;
						int paymentTargetDay = 5;
						if (currentCalendar.get(Calendar.YEAR) > paymentCalendar.get(Calendar.YEAR)) {
							if (currentMonth == paymentTargetMonth) {
								int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
								if (currentDay > paymentTargetDay) {
									invalidList.add(r);
									continue;
								}
							}
						}
					}

				} catch (Exception e) {
					log4j.error("Exception" , e);
					continue;
				}
			}
			
		}

		if(invalidList != null) {
			if(invalidList.size()> 0) {
				for(Receipt r : invalidList) {
					if(!pendingList.contains(r.getUuid())) {
						pendingList.add(r.getUuid());
					}
				}
				return String.join(",", pendingList);
			}
		}	
		return "";
	}
	
	public PurchaseOrder searchbyOrderAndAddressBookAndCompany(int documentNumber, String addressBook,
			String documentType, String orderCompany) {
		return purchaseOrderDao.searchbyOrderAndAddressBookAndCompany(documentNumber, addressBook, documentType, orderCompany);
	}
	
	public ForeignInvoiceTable getForeignInvoiceFromOrder(PurchaseOrder o) {
		return purchaseOrderDao.getForeignInvoiceFromOrder(o);
	}

	public ForeignInvoiceTable getForeignInvoiceFromUuid(String uuid) {
		return purchaseOrderDao.getForeignInvoiceFromUuid(uuid);
	}
	
	public void saveForeignInvoice(ForeignInvoiceTable o) {
		purchaseOrderDao.saveForeignInvoice(o);
	}
	
	public void deleteForeignInvoice(ForeignInvoiceTable o) {
		purchaseOrderDao.deleteForeignInvoice(o);
	}
	
	public void deleteReceiptInvoice(ReceiptInvoice o) {
		purchaseOrderDao.deleteReceiptInvoice(o);
	}
	
	public List<FiscalDocuments> getPaymentPendingReceiptsSOC(int start, int limit) {
		return fiscalDocumentDao.getPaymentPendingReceiptsSOC(start, limit);
	}

	public void updatePaymentReceiptsFD(List<FiscalDocuments> list) {
		fiscalDocumentDao.updateReceiptPaymentList(list);
	}
	
	public PurchaseOrder searchbyOrderAndAddressBookAndCompany(long documentNumber, String addressBook,
			String documentType, String orderCompany) {
		return purchaseOrderDao.searchbyOrderAndAddressBookAndCompany(documentNumber, addressBook, documentType, orderCompany);
	}
	
}
