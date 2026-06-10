package com.eurest.supplier.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.eurest.supplier.dao.DocumentsDao;
import com.eurest.supplier.dao.OutSourcingDao;
import com.eurest.supplier.dao.PurchaseOrderDao;
import com.eurest.supplier.dto.ForeingInvoice;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.invoiceXml.Concepto;
import com.eurest.supplier.model.CodigosSAT;
import com.eurest.supplier.model.OutSourcingDocument;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.Logger;
import com.eurest.supplier.util.MultiFileUploadBean;
import com.eurest.supplier.util.StringUtils;

@Service("outSourcingService")
public class OutSourcingService {
	
	@Autowired
	OutSourcingDao outSourcingDao;

	@Autowired
	SupplierService supplierService;
	
	 @Autowired
	 private JavaMailSender mailSenderObj;

	@Autowired
	EmailService emailService;

	@Autowired
	StringUtils stringUtils;
	
	@Autowired
	CodigosSATService codigosSATService; 
	
	@Autowired
	XmlToPojoService xmlToPojoService;
	
   	@Autowired
	PurchaseOrderService purchaseOrderService;

   	@Autowired
	DocumentsService documentsService;
   	
	@Autowired
	private DocumentsDao documentsDao;
	
	@Autowired
	PurchaseOrderDao purchaseOrderDao;
	
	@Autowired
	EDIService eDIService;
	
	@Autowired
	UdcService udcService;
	
	@Autowired
	Logger logger;
	
	@Autowired
	UsersService usersService;
	
	@Autowired
	DataAuditService dataAuditService;

	public final String OUTSOURCING_RECEIPT_SUBJECT = "SEPASA - Nueva documentación del proveedor _SUPPLIER_";
	public final String OUTSOURCING_RECEIPT_MESSAGE = "Estimado Proveedor <br /><br />Su documentación relacionada con los servicios especializados ha sido recibida y será revisada por nuestro equipo interno antes de ser aprobada. <br /><br />Por favor, esté atento a los mensajes en Portal  para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_SEND_MESSAGE = "Estimado Colaborador <br /><br />El proveedor _SUPPLIER_ ha enviado la documentación _DOCTYPE_ relacionada con su condición de Servicios Especializados. Ésta documentación la puede consultar dentro del Portal de Proveedores en la pestaña \"Servicios Especializados\" <br /><br />";
	
	public final String OUTSOURCING_SEND_MESSAGE_MONTH = "Estimado Colaborador <br /><br />El proveedor _SUPPLIER_ ha enviado la documentación _DOCTYPE_ relacionada con su condición de Servicios Especializados. Ésta documentación la puede consultar dentro del Portal de Proveedores en la pestaña \"Servicios Especializados\" <br /><br />";
	
	public final String OUTSOURCING_RECEIPT_SUBJECT_MONTH = "SEPASA - Nueva documentación mensual del proveedor _SUPPLIER_";
	public final String OUTSOURCING_RECEIPT_SUBJECT_QUARTER = "SEPASA - Nueva documentación cuatrimestral del proveedor _SUPPLIER_";
	public final String OUTSOURCING_RECEIPT_SUBJECT_SECOND = "SEPASA - Nueva documentación bimestral del proveedor _SUPPLIER_";

	public final String OUTSOURCING_NOTIF_SECOND_SUBJECT = "SmartREPSE-Recordatorio: Documentación Bimestral pendiente de carga";
    public final String OUTSOURCING_NOTIF_SECOND_MESSAGE = "Estimado Proveedor <br /><br />Recuerde cargar la documentación bimestral relacionada con los servicios de Servicios Especializados antes del día 17 del mes en curso, de lo contrario su cuenta será deshabilitada hasta no cargar los documentos.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_NOTIF_QUARTERLY_SUBJECT = "SmartREPSE-Recordatorio: Documentación Cuatrimestral pendiente de carga";
    public final String OUTSOURCING_NOTIF_QUARTERLY_MESSAGE = "Estimado Proveedor <br /><br />Recuerde cargar la documentación cuatrimestral relacionada con los servicios de Servicios Especializados antes del día 17 del mes en curso, de lo contrario su cuenta será deshabilitada hasta no cargar los documentos.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_ALERT_SUBJECT = "SEPASA - Deshabilitación del sistema: Documentación Mensual pendiente de carga";
	public final String OUTSOURCING_ALERT_MESSAGE = "Estimado Proveedor <br /><br />La documentación mensual relacionada con los servicios de Servicios Especializados no ha sido recibida aún. Recuerde que debe enviar los documentos a más tardar el día 17 de cada mes. Ingrese al portal para cargar la documentación faltante, de lo contrario no podrá enviar sus facturas o recibir nuevas órdenes de compra.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_ALERT_SECOND_SUBJECT = "SEPASA - Deshabilitación del sistema: Documentación Cuatrimestral pendiente de carga";
	public final String OUTSOURCING_ALERT_SECOND_MESSAGE= "Estimado Proveedor <br /><br />La documentación cuatrimestral relacionada con los servicios de Servicios Especializados no ha sido recibida aún. Recuerde que debe enviar los documentos a más tardar el día 17 de cada cuatrimestre. Ingrese al portal para cargar la documentación faltante, de lo contrario no podrá enviar sus facturas o recibir nuevas órdenes de compra.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_ALERT_QUARTERLY_SUBJECT = "SEPASA - Deshabilitación del sistema: Documentación Cuatrimestral pendiente de carga";
	public final String OUTSOURCING_ALERT_QUARTERLY_MESSAGE= "Estimado Proveedor <br /><br />La documentación cuatrimestral relacionada con los servicios de Servicios Especializados no ha sido recibida aún. Recuerde que debe enviar los documentos a más tardar el día 17 de cada cuatrimestre. Ingrese al portal para cargar la documentación faltante, de lo contrario no podrá enviar sus facturas o recibir nuevas órdenes de compra.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public final String OUTSOURCING_NOTIF_SUBJECT = "Portal de Proveedores-Notificación: Documentación Mensual pendiente de carga";
	public final String OUTSOURCING_NOTIF_MESSAGE = "Estimado Proveedor <br /><br />Recuerde cargar la documentación mensual relacionada con los servicios de Servicios Especializados antes de los días 15 de cada mes, de lo contrario su cuenta será deshabilitada hasta no cargar los documentos.<br /><br />Por favor, esté atento a los mensajes en Portal de Proveedores para recibir más información relacionada con la nueva reforma en materia laboral.<br /><br />";
	
	public static final String EMAIL_OSINV_ACCEPT_SUP = "SEPASA - Notificación de carga exitosa de la factura por servicios especializados en el portal de proveedores para la Orden de Compra No. ";
    public static final String EMAIL_OSINVOICE_ACCEPTED = "Estimado Proveedor: <br /><br />Su factura asociada con SERVICIOS ESPECIALIZADOS se ha recibido correctamente. Esta factura será revisada por el equipo interno junto con los recibos de pago debidamente timbrados. En caso de que no exista algún error con la documentación enviada, la factura será programada a pago a partir de la fecha de recepción de la factura.<br /><br />" +"La orden de compra asociada a su factura es: ";
    
	public static final String EMAIL_OSINV_ACCEPT_COLAB = "SEPASA - Notificación de revisión de factura por servicios especializados para la Orden de Compra No. ";
    public static final String EMAIL_OSINVOICE_MSG = "Estimado Colaborador: <br /><br />Una nueva factura asociada con SERVICIOS ESPECIALIZADOS del proveedor _SUPPLIERNAME_ ha sido cargada en el portal. Agradecemos su pronta intervención para la revisión y aprobación o rechazo de esta factura<br /><br />" +"La orden de compra asociada a la factura es: ";
    
	public static final String EMAIL_OSINV_ACCEPT = "SEPASA - Notificación Factura de Servicios Especializados ACEPTADA para la Orden de Compra No. ";
    public static final String EMAIL_OSINVOICE_ACCEPT= "Estimado Proveedor: <br /><br />Su factura asociada con SERVICIOS ESPECIALIZADOS se ha recibido ACEPTADA para pago. La factura será programada a pago a partir de la fecha de recepción de la factura.<br /><br />" +"La orden de compra asociada a su factura es: ";
    
	public static final String EMAIL_OSINV_REJECT = "SEPASA - Notificación Factura de Servicios Especializados RECHAZADA para la Orden de Compra No. ";
    public static final String EMAIL_OSINVOICE_REJECT = "Estimado Proveedor: <br /><br />Su factura asociada a SERVICIOS ESPECIALIZADOS se ha recibido RECHAZADA. A continuación encontrará las notas del rechazo.<br /> <br /> NOTAS: _NOTES_ <br /><br />" +"Revise los errores y vuelva a subir su factura incluyendo los recibos de nómina debidamente timbrados. <br /> <br />La orden de compra asociada a su factura es: ";
    
	public static final String EMAIL_OSDOC_REJECT = "SEPASA -  Notificación de documento RECHAZADO";
    public static final String EMAIL_OSDOC_REJECT_MSG = "Estimado Proveedor: <br /><br />El documento _DOCNAME_ asociado con SERVICIOS ESPECIALIZADOS ha sido RECHAZADO. A continuación encontrará las notas del rechazo.<br /> <br /> NOTAS: _NOTES_ <br /><br />" +" Revise los errores y vuelva a cargar el documento accediendo la pestaña \"Servicios Especializados\" y utilizando la columna \"Cargar reemplazo\" para enviar nuevamente el documento. ";
    
	public static final String EMAIL_OSDOC_ACCEPT = "SEPASA - Notificación de documento ACEPTADO para la Orden de Compra No. ";
    public static final String EMAIL_OSDOC_ACCEPT_MSG= "Estimado Proveedor: <br /><br />El documento _DOCNAME_ asociado con SERVICIOS ESPECIALIZADOS se ha recibido ACEPTADO. Si el documento es una factura, ésta será programada a pago a partir de la fecha de aprobación.<br /><br />";
    
    public static final String FISCAL_DOC_PENDING = "PENDIENTE";
    public static final String FISCAL_DOC_REJECTED = "RECHAZADO";
    public static final String FISCAL_DOC_APPROVED = "APROBADO";
    public static final String LOG_APPROVAL = "APROBACIONES";
    public static final String LOG_DOCUMENTS = "CARGA_DOCUMENTOS";
    
    private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(OutSourcingService.class);
    
    public synchronized String validateInvoiceFromOrder(InvoiceDTO inv, String addressBook, int documentNumber, String documentType, 
    		                               String tipoComprobante, String receiptList, String xmlContent, List<MultipartFile> uploadedFiles,
    		                               String orderCompany) {

    	try {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
     		String userAuth = auth.getName();
     		Date currentDate = new Date();
     				
			String rOs = validateInvoiceCodes(inv.getConcepto());
			if("".equals(rOs)) {				
				//return "La factura NO contiene claves por Conceptos por Servicios Especializados. Utilice el botón \"Cargar Factura\" para enviar facturas que no contengan servicios especializados";
				return "La factura NO contiene claves por Conceptos por Servicios Especializados.";
			}
			
			PurchaseOrder po = purchaseOrderService.searchbyOrderAndAddressBookAndCompany(documentNumber, addressBook, documentType, orderCompany);
			
			String res = documentsService.validateInvoiceFromOrder(inv, addressBook, documentNumber, 
					                                                documentType, tipoComprobante, po, 
					                                                false, xmlContent, 
					                                                receiptList, true, 0,
					        										false, null);
			
			if("".equals(res)) {
				Supplier s = supplierService.searchByAddressNumber(addressBook);
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(EMAIL_OSINV_ACCEPT_SUP + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(EMAIL_OSINVOICE_ACCEPTED + po.getOrderNumber() + "-" + po.getOrderType() + "<br /> <br />" + AppConstants.ETHIC_CONTENT), s.getEmailSupplier());
				emailAsyncSup.setMailSender(mailSenderObj);
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();	
				
            	UserDocument doc = new UserDocument(); 
            	doc.setAddressBook(po.getAddressNumber());
            	doc.setDocumentNumber(documentNumber);
            	doc.setDocumentType(documentType);
            	doc.setContent(uploadedFiles.get(0).getBytes());
            	doc.setType(uploadedFiles.get(0).getContentType());
            	String fileName = uploadedFiles.get(0).getOriginalFilename();
            	fileName = fileName.replace(" ", "_");
            	doc.setName(fileName);
            	doc.setSize(uploadedFiles.get(0).getSize());
            	doc.setStatus(true);
            	doc.setAccept(true);
            	doc.setFiscalType(tipoComprobante);;
            	doc.setType("text/xml");
            	doc.setFolio(inv.getFolio());
            	doc.setSerie(inv.getSerie());
            	doc.setUuid(inv.getUuid());
            	doc.setUploadDate(new Date());
            	doc.setFiscalRef(0);
            	documentsService.save(doc, new Date(), "");
            	
            	dataAuditService.saveDataAudit("SaveOutsourcingInvoice", po.getAddressNumber(), currentDate, request.getRemoteAddr(),
            	userAuth, uploadedFiles.get(0).getOriginalFilename(), "validateInvoiceFromOrder", 
    	   		"Uploaded Outsourcing Invoice Document Successful " + documentType,documentNumber+"", po.getOrderNumber()+"", null, 
    	   		inv.getUuid(), AppConstants.STATUS_COMPLETE, AppConstants.OUTSOURCING_MODULE);
    	   		                 	
            	doc = new UserDocument(); 
            	doc.setAddressBook(po.getAddressNumber());
            	doc.setDocumentNumber(documentNumber);
            	doc.setDocumentType(documentType);
            	doc.setContent(uploadedFiles.get(1).getBytes());
            	doc.setType(uploadedFiles.get(1).getContentType());
            	fileName = uploadedFiles.get(1).getOriginalFilename();
            	fileName = fileName.replace(" ", "_");
            	doc.setName(fileName);
            	doc.setSize(uploadedFiles.get(1).getSize());
            	doc.setStatus(true);
            	doc.setAccept(true);
            	doc.setFiscalType(tipoComprobante);
            	doc.setType("application/pdf");
            	doc.setFolio(inv.getFolio());
            	doc.setSerie(inv.getSerie());
            	doc.setUuid(inv.getUuid());
            	doc.setUploadDate(new Date());
            	doc.setFiscalRef(0);
            	documentsService.save(doc, new Date(), "");
            	
            	dataAuditService.saveDataAudit("SaveOutsourcingInvoice", po.getAddressNumber(), currentDate, request.getRemoteAddr(),
                userAuth, uploadedFiles.get(1).getOriginalFilename(), "validateInvoiceFromOrder", 
            	"Uploaded Outsourcing Invoice Document Successful " + documentType,documentNumber+"", po.getOrderNumber()+"", null, 
            	inv.getUuid(), AppConstants.STATUS_COMPLETE, AppConstants.OUTSOURCING_MODULE);
   		    	
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);
                
                ZipEntry entry = new ZipEntry(uploadedFiles.get(0).getOriginalFilename());
                entry.setSize(uploadedFiles.get(0).getSize());
                zos.putNextEntry(entry);
                zos.write(uploadedFiles.get(0).getBytes());

                entry = new ZipEntry(uploadedFiles.get(1).getOriginalFilename());
                entry.setSize(uploadedFiles.get(1).getSize());
                zos.putNextEntry(entry);
                zos.write(uploadedFiles.get(1).getBytes());
                
                entry = new ZipEntry(uploadedFiles.get(2).getOriginalFilename());
                entry.setSize(uploadedFiles.get(2).getSize());
                zos.putNextEntry(entry);
                zos.write(uploadedFiles.get(2).getBytes());
                zos.closeEntry();
                zos.close();
                
   		    	OutSourcingDocument docOs = new OutSourcingDocument();
   				docOs.setAddressBook(addressBook);
   				docOs.setSupplierName(s.getRazonSocial());
   				docOs.setDocumentType("REC_NOMINA");
   				docOs.setContent(baos.toByteArray());
   				docOs.setType(uploadedFiles.get(2).getContentType());
   				docOs.setName("CONSOLIDADO_" + uploadedFiles.get(2).getOriginalFilename());
   				docOs.setSize(baos.toByteArray().length);
   				docOs.setStatus(false);
   				docOs.setFolio(inv.getFolio());
   				docOs.setUuid(inv.getUuid());
   				docOs.setAttachId(inv.getUuid());
   				docOs.setUploadDate(new Date());
   				docOs.setFrequency("INV");
   				docOs.setCompany("");
   				docOs.setOrderNumber(po.getOrderNumber());
   				docOs.setOrderType(po.getOrderType());
   				docOs.setObsolete(false);
   				docOs.setDocStatus(FISCAL_DOC_PENDING);
   		    	outSourcingDao.saveDocument(docOs);
   		    	
   		    	dataAuditService.saveDataAudit("SaveOutsourcingInvoice", po.getAddressNumber(), currentDate, request.getRemoteAddr(),
   		        userAuth, "CONSOLIDADO_" + uploadedFiles.get(2).getOriginalFilename(), "validateInvoiceFromOrder", 
   		       	"Uploaded Outsourcing Invoice Document Successful REC_NOMINA",documentNumber+"", po.getOrderNumber()+"", null, 
   		       	inv.getUuid(), FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);

   		    	List<Receipt> receipts = purchaseOrderService.getReceiptsByUUID(inv.getUuid());
   		    	for(Receipt r : receipts) {
   		    		r.setPaymentStatus("PENDING");
   		    		purchaseOrderService.updateReceipt(r);
   		    	}
   		    	
   		    	String internalEmail = "";
   		    	UDC udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_RH");
				if(udc != null) {
					internalEmail = udc.getStrValue1();
				}
				
				if(!"".equals(internalEmail)) {
	   		    	String content = EMAIL_OSINVOICE_MSG;
	   		    	content = content.replace("_SUPPLIERNAME_", s.getRazonSocial());
		   		 	emailAsyncSup = new EmailServiceAsync();
					emailAsyncSup.setProperties(EMAIL_OSINV_ACCEPT_COLAB + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(content + po.getOrderNumber() + "-" + po.getOrderType() + "<br /> <br />" + AppConstants.ETHIC_CONTENT), internalEmail);
					emailAsyncSup.setMailSender(mailSenderObj);
					emailThreadSup = new Thread(emailAsyncSup);
					emailThreadSup.start();	
				}

				logger.log(LOG_DOCUMENTS, "CARGA DE FACTURAS C/RECIBO DE NOMINA PARA EL PROVEEDOR: " + s.getRazonSocial());
				
   		    	return "";
				
			}else {
				return res;
			}
    	}catch(Exception e) {
    		log4j.error("Exception" , e);
    		e.printStackTrace();
    		return e.getMessage(); 
    	}

    }
    
    public synchronized String approveDocument(int id, String notes, String user) {

    	HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarNow = Calendar.getInstance();
        String now = simpleDateFormat.format(calendarNow.getTime());
        
 		OutSourcingDocument osDoc = outSourcingDao.getDocumentById(id);
 		osDoc.setNotes(osDoc.getNotes() + " / " + now + " - " + user + " Escribió: " + notes);
 		osDoc.setStatus(false);
 		osDoc.setDocStatus(FISCAL_DOC_APPROVED);
 		outSourcingDao.updateDocument(osDoc);
 		
 		String docName = osDoc.getName();
 		Supplier s = supplierService.searchByAddressNumber(osDoc.getAddressBook());
 		
 		dataAuditService.saveDataAudit("ApproveDocument", s.getAddresNumber(), calendarNow.getTime(), request.getRemoteAddr(),
 		user, osDoc.getFrequency() + " - " + docName + " - Document Approval Successful - " + osDoc.getDocumentType() , "approveDocument", 
 		 notes, null, null, AppConstants.FINAL_STEP,
 		null, FISCAL_DOC_APPROVED, AppConstants.OUTSOURCING_MODULE);
 		
 		EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
 		String msg = EMAIL_OSDOC_ACCEPT_MSG;
 		msg = msg.replace("_DOCNAME_", osDoc.getName());
		emailAsyncSup.setProperties(EMAIL_OSDOC_ACCEPT, stringUtils.prepareEmailContent(msg), s.getEmailSupplier());
		emailAsyncSup.setMailSender(mailSenderObj);
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();

		logger.log(LOG_APPROVAL, "APROBACIÓN DE DOCUMENTOS PARA : " + s.getRazonSocial() +  " \"" + docName + "\" "  + now + " - " + user + " Escribió: " + notes);
		return "";
	}

    public synchronized String approveInvoice(String uuid, String notes, String user) {

    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarNow = Calendar.getInstance();
        String now = simpleDateFormat.format(calendarNow.getTime());
        
	    String internalEmail = "";
	    Users usr =  usersService.getByUserName(user);
		if(usr != null) {
			internalEmail = usr.getEmail();
		}
        
		List<UserDocument> docs = documentsDao.searchCriteriaByUuidOnly(uuid);
		if(docs != null) {
			InvoiceDTO inv = null;
			if(docs != null) {
				List<UDC> comUDCList =  udcService.searchBySystem("COMPANYDOMESTIC");
				List<UDC> supDomUDCList =  udcService.searchBySystem("SUPPLIERDOMESTIC");
				
				for(UserDocument u : docs) {
					Supplier s = supplierService.searchByAddressNumber(u.getAddressBook());
					if(AppConstants.INVOICE_FIELD.equals(u.getFiscalType()) && "text/xml".equals(u.getType())) {
						try {
						String xmlStr = new String(u.getContent(), StandardCharsets.UTF_8);
						inv = getInvoiceXmlFromString(xmlStr);
						
						List<Receipt> receipts = purchaseOrderDao.getOrderReceiptsByUuid(uuid);
						PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBookAndType(u.getDocumentNumber(),u.getAddressBook(), u.getDocumentType());
						
						String invCurrency = inv.getMoneda().trim();
						String domesticCurrency = AppConstants.DEFAULT_CURRENCY;

						if(comUDCList != null && !comUDCList.isEmpty()) {
							for(UDC company : comUDCList) {
								if(company.getStrValue1().trim().equals(po.getOrderCompany().trim()) && !"".equals(company.getStrValue2().trim())) {
									domesticCurrency = company.getStrValue2().trim();
									break;
								}
							}
						}

						if(supDomUDCList != null && !supDomUDCList.isEmpty()) {
							for(UDC supplier : supDomUDCList) {
								if(supplier.getStrValue1().trim().equals(u.getAddressBook()) && !"".equals(supplier.getStrValue2().trim())) {
									domesticCurrency = supplier.getStrValue2().trim();
									break;
								}
							}
						}

						if(domesticCurrency.equals(invCurrency)) {
							eDIService.createNewVoucher(po, inv, 0, s, receipts, AppConstants.NN_MODULE_VOUCHER);
						} else {
							ForeingInvoice fi = new ForeingInvoice();
							fi.setSerie(inv.getSerie());
							fi.setFolio(inv.getFolio());
							fi.setUuid(inv.getUuid());
							fi.setExpeditionDate(inv.getFechaTimbrado());
							eDIService.createNewForeignVoucher(po, fi, 0, s, receipts, AppConstants.NN_MODULE_VOUCHER);
						}
				        
				        String emailRecipent = s.getEmailSupplier();
				        if(!"".equals(internalEmail)) {
				        	emailRecipent = emailRecipent + "," + internalEmail;
				        }
				        
						EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
						emailAsyncSup.setProperties(EMAIL_OSINV_ACCEPT + po.getOrderNumber() + "-" + po.getOrderType(), stringUtils.prepareEmailContent(EMAIL_OSINVOICE_ACCEPT + po.getOrderNumber() + "-" + po.getOrderType() + "<br /> <br />" + AppConstants.ETHIC_CONTENT), emailRecipent);
						emailAsyncSup.setMailSender(mailSenderObj);
						Thread emailThreadSup = new Thread(emailAsyncSup);
						emailThreadSup.start();	
				        
				        log4j.info("Sent: " + inv.getUuid());
						}catch(Exception e){
							log4j.error("Exception" , e);
						}
					}
					
					if(AppConstants.INVOICE_FIELD.equals(u.getFiscalType()) && "application/pdf".equals(u.getType())) {
						try {
							//Send pdf to remote server
    	                	File convFile = new File(System.getProperty("java.io.tmpdir")+"/" + inv.getUuid() + ".pdf");
    	                	FileUtils.writeByteArrayToFile(convFile, u.getContent());
    	                	documentsService.sendFileToRemote(convFile, inv.getUuid() + ".pdf");
    	                	convFile.delete();
						}catch(Exception e){
							log4j.error("Exception" , e);
						}
					}
					
					logger.log(LOG_APPROVAL, "APROBACIÓN DE FACTURAS PARA : " + s.getRazonSocial() +  " \" UUID:" + u.getUuid() + "\" "  + now + " - " + user + " Escribió: " + notes);
				
				}
				
   		    	List<Receipt> receipts = purchaseOrderService.getReceiptsByUUID(uuid);
   		    	for(Receipt r : receipts) {
   		    		r.setPaymentStatus("");
   		    		purchaseOrderService.updateReceipt(r);
   		    	}
   		    	
   		    	List<OutSourcingDocument> docOs = outSourcingDao.searchByAttachID(uuid);
   		    	for(OutSourcingDocument d : docOs) {
   		    		d.setStatus(true);
   		  		    d.setDocStatus(FISCAL_DOC_APPROVED);
   		  		    d.setNotes(notes);
   		    		outSourcingDao.updateDocument(d);
   		    		
   		    		dataAuditService.saveDataAudit("ApproveInvoice", d.getAddressBook(), calendarNow.getTime(), request.getRemoteAddr(),
   		    		userAuth, "Invoice Approval Successful - " + d.getDocumentType() , "approveInvoice", 
   		    		notes, String.valueOf(d.getOrderNumber()), String.valueOf(d.getOrderNumber()), 
   		    		AppConstants.FINAL_STEP, uuid, FISCAL_DOC_APPROVED, AppConstants.OUTSOURCING_MODULE);
   		    	}
			}
		}
			
    	return "";
    }
    
    
    public synchronized String rejectDocument(int id, String notes, String user) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarNow = Calendar.getInstance();
        String now = simpleDateFormat.format(calendarNow.getTime());
        
 		OutSourcingDocument osDoc = outSourcingDao.getDocumentById(id);
 		osDoc.setNotes(osDoc.getNotes() + " / " + now + " - " + user + " Escribió: " + notes);
 		osDoc.setStatus(false);
 		osDoc.setDocStatus(FISCAL_DOC_REJECTED);
 		outSourcingDao.updateDocument(osDoc);
 		
 		String docName = osDoc.getName();
 		Supplier s = supplierService.searchByAddressNumber(osDoc.getAddressBook());
 		
 		dataAuditService.saveDataAudit("RejectDocument", s.getAddresNumber(), calendarNow.getTime(), request.getRemoteAddr(),
 		userAuth, osDoc.getFrequency() + " - " + docName  + " - Document Rejected Successful - " + osDoc.getDocumentType(), "rejectDocument", 
 		notes, null, null,AppConstants.FINAL_STEP,
 		null,FISCAL_DOC_REJECTED, AppConstants.OUTSOURCING_MODULE);
 		
 		EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
 		String msg = EMAIL_OSDOC_REJECT_MSG;
 		msg = msg.replace("_DOCNAME_", osDoc.getName());
 		msg = msg.replace("_NOTES_", osDoc.getNotes());
		emailAsyncSup.setProperties(EMAIL_OSDOC_REJECT, stringUtils.prepareEmailContent(msg), s.getEmailSupplier());
		emailAsyncSup.setMailSender(mailSenderObj);
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();
		
		logger.log(LOG_APPROVAL, "RECHAZO DE DOCUMENTOS PARA : " + s.getRazonSocial() +  " \"" + docName + "\" "  + now + " - " + user + " Escribió: " + notes);
		return "";
	}
    
    public synchronized String rejectInvoice(String uuid, String notes, String user) {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarNow = Calendar.getInstance();
        String now = simpleDateFormat.format(calendarNow.getTime());
        
    	int documentNumber = 0;
    	String documentType = "";
    	String addressNumber = "";
    	List<UserDocument> docs = documentsDao.searchCriteriaByUuidOnly(uuid);
    	for(UserDocument d : docs) {
    		documentsDao.deleteDocuments(d.getId());
    		documentNumber = d.getDocumentNumber();
    		documentType = d.getDocumentType();
    		addressNumber = d.getAddressBook();
    	}
    	
    	String docName = "";
    	List<OutSourcingDocument> docOs = outSourcingDao.searchByAttachID(uuid);
    	for(OutSourcingDocument d : docOs) {
    		docName = docName + d.getName() + " ";
    		outSourcingDao.deleteDocument(d.getId());
    		
    		dataAuditService.saveDataAudit("RejectInvoice", d.getAddressBook(), calendarNow.getTime(), request.getRemoteAddr(),
    		userAuth, d.getFrequency() + " - Invoice Rejected Successful - " + d.getDocumentType(), "rejectInvoice", 
    		notes, String.valueOf(d.getOrderNumber()), String.valueOf(d.getOrderNumber()),AppConstants.FINAL_STEP,
    		d.getUuid(),FISCAL_DOC_REJECTED, AppConstants.OUTSOURCING_MODULE);
    	}

    	List<Receipt> receipts = purchaseOrderService.getReceiptsByUUID(uuid);
    	for(Receipt r : receipts) {
    		r.setUuid("");
    		r.setEstPmtDate(null);
    		r.setFolio("");
    		r.setSerie("");
    		r.setInvDate(null);
    		r.setStatus(AppConstants.STATUS_OC_RECEIVED);
	    	r.setPaymentStatus("");
    		purchaseOrderService.updateReceipt(r);
    	}
    	
    	PurchaseOrder po = purchaseOrderDao.searchbyOrderAndAddressBookAndType(documentNumber,addressNumber, documentType);
    	po.setCreditNotUuid("");
    	po.setOrderStauts(AppConstants.STATUS_OC_RECEIVED);
    	po.setInvoiceAmount(0);
    	po.setInvoiceNumber("");
    	po.setInvoiceUploadDate(null);
    	purchaseOrderDao.updateOrders(po);
    	
    	Supplier s = supplierService.searchByAddressNumber(addressNumber);
    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
    	
    	String msg = EMAIL_OSINVOICE_REJECT;
    	msg = msg.replace("_NOTES_", notes);
    	
		emailAsyncSup.setProperties(EMAIL_OSINV_REJECT + documentNumber + "-" + documentType, stringUtils.prepareEmailContent(msg + documentNumber + "-" + documentType + "<br /> <br />" + AppConstants.ETHIC_CONTENT), s.getEmailSupplier());
		emailAsyncSup.setMailSender(mailSenderObj);
		Thread emailThreadSup = new Thread(emailAsyncSup);
		emailThreadSup.start();
		
		logger.log(LOG_APPROVAL, "RECHAZO DE FACTURA PARA : " + s.getRazonSocial() +  " \"" + " PO: " + po.getOrderNumber() + "-" + po.getOrderType() + "\" "  + now + " UUID: " + uuid + "(" + docName + ")" + " - " + user + " Escribió: " + notes);
    	
    	return "";
    }
	
	public OutSourcingDocument getDocumentById(int id) {
		return outSourcingDao.getDocumentById(id);
	}
		
	
	public synchronized String saveBaseLineDocument(MultiFileUploadBean multiFileUploadBean, String effectiveDate, String addressNumber) {

		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
		Date currentDate = new Date();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String usr = auth.getName();
 		
   		try {
   			if(uploadedFiles.size() == 1) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				if(s != null) {
   					//Registro y/o renovacion REPSE (PDF)
   					OutSourcingDocument doc = new OutSourcingDocument();
   					
   					doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("REGIS_REN_REPSE");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBaseLineDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveBaseLineDocument", 
	   		   		"Uploaded Base Line Document Successful - REGIS_REN_REPSE",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
   		    	
	   		    	//Constancia de cumplimiento de obligaciones Movido a Mensual
	   		    	
	   				/*doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CONST_CUMP_OBLIG");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		        //Constancia de Situación Fiscal 
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CONST_SIT_FIS");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBaseLineDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveBaseLineDocument", 
	   		   		"Uploaded Base Line Document Successful - CONST_SIT_FIS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Autorización vigente a que se refiere el artículo 15 de la Ley Federal del Trabajo
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("AUT_STPS");
	   		    	doc.setContent(uploadedFiles.get(1).getBytes());
	   		    	doc.setType(uploadedFiles.get(1).getContentType());
	   		    	doc.setName(uploadedFiles.get(1).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(1).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setEffectiveDate(effectiveDate);
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBaseLineDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(1).getOriginalFilename(), "saveBaseLineDocument", 
	   		   		"Uploaded Base Line Document Successful - AUT_STPS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Lista de trabajadores
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("LISTA_TRAB");
	   		    	doc.setContent(uploadedFiles.get(2).getBytes());
	   		    	doc.setType(uploadedFiles.get(2).getContentType());
	   		    	doc.setName(uploadedFiles.get(2).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(2).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBaseLineDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(2).getOriginalFilename(), "saveBaseLineDocument", 
	   		   		"Uploaded Base Line Document Successful - LISTA_TRAB",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Acta protocolizada con detalle de su objeto social
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("ACTA_PROTOCOL");
	   		    	doc.setContent(uploadedFiles.get(3).getBytes());
	   		    	doc.setType(uploadedFiles.get(3).getContentType());
	   		    	doc.setName(uploadedFiles.get(3).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(3).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("BL");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBaseLineDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(3).getOriginalFilename(), "saveBaseLineDocument", 
	   		   		"Uploaded Base Line Document Successful - ACTA_PROTOCOL",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);*/
	   		    	
	   		    	s.setOutSourcingAccept(true);
	   		    	s.setOutSourcingRecordDate(new Date());
	   		    	supplierService.updateSupplierCore(s);
	   		    	
	   		    	String subject = OUTSOURCING_RECEIPT_SUBJECT;
	   		    	subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
	   		    	
	   		    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(OUTSOURCING_RECEIPT_MESSAGE), s.getEmailSupplier());
	   				emailAsyncSup.setMailSender(mailSenderObj);
	   				Thread emailThreadSup = new Thread(emailAsyncSup);
	   				emailThreadSup.start();
	   				
	   		    	String internalEmail = "";
	   		    	UDC udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_RH");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "BASE");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
	   				
	   		    	internalEmail = "";
	   		    	udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_TAX");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "BASE");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
					
	   		    	internalEmail = "";
	   		    	udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_LEGAL");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "BASE");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
					
					logger.log(LOG_DOCUMENTS, "CARGA DE DOCUMENTOS BASE DEL PROVEEDOR: " + s.getRazonSocial());
					
   				}
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		
		return "";
		
	}
	
	public synchronized String saveMonthlyDocs(MultiFileUploadBean multiFileUploadBean, String addressNumber, String month, String year) {
		
		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
		Date currentDate = new Date();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String usr = auth.getName();
 		
 		Date today = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		 
		calendar.add(Calendar.MONTH, -1);
		
        int monthLoad = Integer.parseInt(month) + 1;
        int yearLoad = Integer.parseInt(year);
        
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
 		
   		try {
   			if(uploadedFiles.size() == 15) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				if(s != null) {
   					
   				//Listado de Personal (Ciudad, Registro Patronal, Nombre, SBC, CURP) (Excel)
   					OutSourcingDocument doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("LIST_PERS");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		        dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		        usr, uploadedFiles.get(0).getOriginalFilename(), "saveMonthlyDocs", 
	   		        "Uploaded Monthly Document Successful - LIST_PERS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		        		       		    	
	   		    	//CFDIs (xmls, pdf)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CFDIS");
	   		    	doc.setContent(uploadedFiles.get(1).getBytes());
	   		    	doc.setType(uploadedFiles.get(1).getContentType());
	   		    	doc.setName(uploadedFiles.get(1).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(1).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(1).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CFDIS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    		   		    	
	   		        //Opinion de cumplimiento SAT (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("OPI_CUMP_SAT");
	   		    	doc.setContent(uploadedFiles.get(2).getBytes());
	   		    	doc.setType(uploadedFiles.get(2).getContentType());
	   		    	doc.setName(uploadedFiles.get(2).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(2).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(2).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - OPI_CUMP_SAT",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Opinion de cumplimiento IMSS (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("OPI_CUMP_IMSS");
	   		    	doc.setContent(uploadedFiles.get(3).getBytes());
	   		    	doc.setType(uploadedFiles.get(3).getContentType());
	   		    	doc.setName(uploadedFiles.get(3).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(3).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(3).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - OPI_CUMP_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   				//Opinion de cumplimiento INFONAVIT (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("OPI_CUMP_INFO");
	   		    	doc.setContent(uploadedFiles.get(4).getBytes());
	   		    	doc.setType(uploadedFiles.get(4).getContentType());
	   		    	doc.setName(uploadedFiles.get(4).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(4).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(4).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - OPI_CUMP_INFO",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Cedula de Determinacion de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CED_CUOT_IMSS");
	   		    	doc.setContent(uploadedFiles.get(5).getBytes());
	   		    	doc.setType(uploadedFiles.get(5).getContentType());
	   		    	doc.setName(uploadedFiles.get(5).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(5).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(5).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CED_CUOT_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Resumen de Liquidacion de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("LIQ_CUOT_IMSS");
	   		    	doc.setContent(uploadedFiles.get(6).getBytes());
	   		    	doc.setType(uploadedFiles.get(6).getContentType());
	   		    	doc.setName(uploadedFiles.get(6).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(6).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(6).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - LIQ_CUOT_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		   //Resumen de Liquidacion de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   				/*doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("LIQ_CUOT_IMSS");
	   		    	doc.setContent(uploadedFiles.get(6).getBytes());
	   		    	doc.setType(uploadedFiles.get(6).getContentType());
	   		    	doc.setName(uploadedFiles.get(6).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(6).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		        dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		        usr, uploadedFiles.get(6).getOriginalFilename(), "saveMonthlyDocs", 
	   		        "Uploaded Monthly Document Successful - LIQ_CUOT_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);*/
	   		        		       		    	
	   		    //Linea de Captura para pago de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CAPT_PAG_CUOT_IMSS");
	   		    	doc.setContent(uploadedFiles.get(7).getBytes());
	   		    	doc.setType(uploadedFiles.get(7).getContentType());
	   		    	doc.setName(uploadedFiles.get(7).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(7).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(7).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CAPT_PAG_CUOT_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    		   		    	
	   		        //Comprobante de pago de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("COMP_PAG_CUOT_IMSS");
	   		    	doc.setContent(uploadedFiles.get(8).getBytes());
	   		    	doc.setType(uploadedFiles.get(8).getContentType());
	   		    	doc.setName(uploadedFiles.get(8).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(8).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(8).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - COMP_PAG_CUOT_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Declaracion de pago provisional mensual de IVA (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DEC_PAG_PROV_IVA");
	   		    	doc.setContent(uploadedFiles.get(9).getBytes());
	   		    	doc.setType(uploadedFiles.get(9).getContentType());
	   		    	doc.setName(uploadedFiles.get(9).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(9).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(9).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - DEC_PAG_PROV_IVA",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   				//Acuse de Recibo y Linea de Captura de pago provisional mensual de IVA (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CAPT_PAG_PROV_IVA");
	   		    	doc.setContent(uploadedFiles.get(10).getBytes());
	   		    	doc.setType(uploadedFiles.get(10).getContentType());
	   		    	doc.setName(uploadedFiles.get(10).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(10).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(10).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CAPT_PAG_PROV_IVA",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Comprobante de pago provisional mensual de IVA (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("COMP_PAG_PROV_IVA");
	   		    	doc.setContent(uploadedFiles.get(11).getBytes());
	   		    	doc.setType(uploadedFiles.get(11).getContentType());
	   		    	doc.setName(uploadedFiles.get(11).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(11).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(11).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - COMP_PAG_PROV_IVA",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		   //Declaracion mensual de retenciones de ISR asalariados (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DECL_MENS_ISR");
	   		    	doc.setContent(uploadedFiles.get(12).getBytes());
	   		    	doc.setType(uploadedFiles.get(12).getContentType());
	   		    	doc.setName(uploadedFiles.get(12).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(12).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(12).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - DECL_MENS_ISR",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		   //Declaracion mensual de retenciones de ISR asalariados (PDF)
	   				/*doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DECL_MENS_ISR");
	   		    	doc.setContent(uploadedFiles.get(12).getBytes());
	   		    	doc.setType(uploadedFiles.get(12).getContentType());
	   		    	doc.setName(uploadedFiles.get(12).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(12).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		        dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		        usr, uploadedFiles.get(12).getOriginalFilename(), "saveMonthlyDocs", 
	   		        "Uploaded Monthly Document Successful - DECL_MENS_ISR",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);*/
	   		        		       		    	
	   		    	//Acuse de Recibo y Linea de Captura de retencion mensual de ISR asalariados (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CAPT_PAG_ISR");
	   		    	doc.setContent(uploadedFiles.get(13).getBytes());
	   		    	doc.setType(uploadedFiles.get(13).getContentType());
	   		    	doc.setName(uploadedFiles.get(13).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(13).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(13).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CAPT_PAG_ISR",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    		   		    	
	   		        //Comprobante de pago de retenciones de ISR asalariados (PDF)
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("COMP_PAG_ISR");
	   		    	doc.setContent(uploadedFiles.get(14).getBytes());
	   		    	doc.setType(uploadedFiles.get(14).getContentType());
	   		    	doc.setName(uploadedFiles.get(14).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(14).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(14).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - COMP_PAG_ISR",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    		   		    	
	   				/*//Acuse de declaración informativa mensual del IMSS
   					OutSourcingDocument doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DECL_MENS_IMSS");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		        dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		        usr, uploadedFiles.get(0).getOriginalFilename(), "saveMonthlyDocs", 
	   		        "Uploaded Monthly Document Successful - DECL_MENS_IMSS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		        		       		    	
	   		    	//Pagos Provisionales de ISR por salarios mensual
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("PROV_ISR_SAL");
	   		    	doc.setContent(uploadedFiles.get(1).getBytes());
	   		    	doc.setType(uploadedFiles.get(1).getContentType());
	   		    	doc.setName(uploadedFiles.get(1).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(1).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(1).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - PROV_ISR_SAL",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    		   		    	
	   		        //Pago de las cuotas obrero-patronales al Instituto Mexicano del Seguro Social
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CUOT_OBR_PATR");
	   		    	doc.setContent(uploadedFiles.get(2).getBytes());
	   		    	doc.setType(uploadedFiles.get(2).getContentType());
	   		    	doc.setName(uploadedFiles.get(2).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(2).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(2).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CUOT_OBR_PATR",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Pago de ISN mensual
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("PAGO_ISN");
	   		    	doc.setContent(uploadedFiles.get(3).getBytes());
	   		    	doc.setType(uploadedFiles.get(3).getContentType());
	   		    	doc.setName(uploadedFiles.get(3).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(3).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(3).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - PAGO_ISN",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   				//Pago Provisional de IVA
	   				doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("PAGO_PROV_IVA");
	   		    	doc.setContent(uploadedFiles.get(4).getBytes());
	   		    	doc.setType(uploadedFiles.get(4).getContentType());
	   		    	doc.setName(uploadedFiles.get(4).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(4).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(4).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - PAGO_PROV_IVA",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Constancia de cumplimiento de obligaciones
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CONST_CUMP_OBLIG");
	   		    	doc.setContent(uploadedFiles.get(5).getBytes());
	   		    	doc.setType(uploadedFiles.get(5).getContentType());
	   		    	doc.setName(uploadedFiles.get(5).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(5).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("MONTH");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   				doc.setMonthLoad(monthLoad);
	   		    	doc.setYearLoad(yearLoad);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveMonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(5).getOriginalFilename(), "saveMonthlyDocs", 
	   		   		"Uploaded Monthly Document Successful - CONST_CUMP_OBLIG",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);*/
	   		    	
	   		    	s.setOutSourcingMonthlyAccept(true);
	   		    	supplierService.updateSupplierCore(s);
	   		    	
	   		    	String subject = OUTSOURCING_RECEIPT_SUBJECT_MONTH;
	   		    	subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
	   		    	
	   		    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(OUTSOURCING_RECEIPT_MESSAGE), s.getEmailSupplier());
	   				emailAsyncSup.setMailSender(mailSenderObj);
	   				Thread emailThreadSup = new Thread(emailAsyncSup);
	   				emailThreadSup.start();
	   		    	
	   		    	String internalEmail = "";
	   		    	UDC udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_RH");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "MENSUAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
	   				
	   		    	internalEmail = "";
	   		    	udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_TAX");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "MENSUAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
						   		    	
					logger.log(LOG_DOCUMENTS, "CARGA DE DOCUMENTOS MENSUALES DEL PROVEEDOR: " + s.getRazonSocial());
   				}
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		
		return "";
	}
	
	public synchronized String saveBimonthlyDocs(MultiFileUploadBean multiFileUploadBean, String addressNumber) {
		
		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
		Date currentDate = new Date();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String usr = auth.getName();
 		
		//Company c = companyService.searchByCompany(company);
   		try {
   			if(uploadedFiles.size() == 4) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				if(s != null) {
   						   		    	
   					OutSourcingDocument doc = new OutSourcingDocument();
   					
   					//Cedula de Determinacion de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
   					doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CED_CUOT_INFO");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("SECOND");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBimonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveBimonthlyDocs", 
	   		   		"Uploaded Bimonthly Document Successful - CED_CUOT_INFO",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Resumen de Liquidacion de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("LIQ_CUOT_INFO");
	   		    	doc.setContent(uploadedFiles.get(1).getBytes());
	   		    	doc.setType(uploadedFiles.get(1).getContentType());
	   		    	doc.setName(uploadedFiles.get(1).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(1).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("SECOND");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBimonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(1).getOriginalFilename(), "saveBimonthlyDocs", 
	   		   		"Uploaded Bimonthly Document Successful - LIQ_CUOT_INFO",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Linea de Captura para pago de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("CAPT_PAG_CUOT_INFO");
	   		    	doc.setContent(uploadedFiles.get(2).getBytes());
	   		    	doc.setType(uploadedFiles.get(2).getContentType());
	   		    	doc.setName(uploadedFiles.get(2).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(2).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("SECOND");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBimonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(2).getOriginalFilename(), "saveBimonthlyDocs", 
	   		   		"Uploaded Bimonthly Document Successful - CAPT_PAG_CUOT_INFO",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	//Comprobante de pago de cuotas IMSS (mensual) INFONAVIT (bimestral) (PDF)
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("COMP_PAG_CUOT_INFO");
	   		    	doc.setContent(uploadedFiles.get(3).getBytes());
	   		    	doc.setType(uploadedFiles.get(3).getContentType());
	   		    	doc.setName(uploadedFiles.get(3).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(3).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("SECOND");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBimonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(3).getOriginalFilename(), "saveBimonthlyDocs", 
	   		   		"Uploaded Bimonthly Document Successful - COMP_PAG_CUOT_INFO",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
   					
   					//Pago de las aportaciones al Instituto del Fondo Nacional de la Vivienda para los Trabajadores
	   				/*doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("APOR_FON_NAL_VIV");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("SECOND");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveBimonthlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveBimonthlyDocs", 
	   		   		"Uploaded Bimonthly Document Successful - APOR_FON_NAL_VIV",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);*/
   					
	   		 	    //s.setOutSourcingQuarterlyAccept(true);
	   		    	s.setOutSourcingBimonthlyAccept(true);
	   		    	supplierService.updateSupplierCore(s);
	   		    		   		    	
	   		    	logger.log(LOG_DOCUMENTS, "DOCUMENTOS BIMESTRALES PARA : " + s.getRazonSocial());
	   		    	
	   		    	String subject = OUTSOURCING_RECEIPT_SUBJECT_SECOND;
	   		    	subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
	   		    	
	   		    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(OUTSOURCING_RECEIPT_MESSAGE), s.getEmailSupplier());
	   				emailAsyncSup.setMailSender(mailSenderObj);
	   				Thread emailThreadSup = new Thread(emailAsyncSup);
	   				emailThreadSup.start();
	   				
	   				String internalEmail = "";
	   		    	UDC udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_RH");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "BIMESTRAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
	   				
	   		    	internalEmail = "";
	   		    	udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_TAX");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "BIMESTRAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
   				
   				}
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		
		return "";
	}
	
	
	public synchronized String saveQuarterlyDocs(MultiFileUploadBean multiFileUploadBean, String addressNumber) {
		
		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
		Date currentDate = new Date();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String usr = auth.getName();
 		
		//Company c = companyService.searchByCompany(company);
   		try {
   			if(uploadedFiles.size() == 2) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				if(s != null) {
   					
   					OutSourcingDocument doc = new OutSourcingDocument();
   					
   					doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DECL_IMSS_ICSOE"); 
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("QUARTER");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveQuarterlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveQuarterlyDocs", 
	   		   		"Uploaded Quarterly Document Successful - DECL_IMSS_ICSOE",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
	   		    	
	   		    	doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("DECL_INFO_SISUB"); 
	   		    	doc.setContent(uploadedFiles.get(1).getBytes());
	   		    	doc.setType(uploadedFiles.get(1).getContentType());
	   		    	doc.setName(uploadedFiles.get(1).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(1).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("QUARTER");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   				doc.setDocStatus(FISCAL_DOC_PENDING);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	
	   		    	dataAuditService.saveDataAudit("SaveQuarterlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
	   		   		usr, uploadedFiles.get(1).getOriginalFilename(), "saveQuarterlyDocs", 
	   		   		"Uploaded Quarterly Document Successful - DECL_INFO_SISUB",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
   						   		    	
	   				//Acuse de Informativa de Prestador de Servicios u Obras Especializados
   					/*if(uploadedFiles.get(0).getSize() > 0) {
		   		    	doc = new OutSourcingDocument();
		   				doc.setAddressBook(addressNumber);
		   				doc.setSupplierName(s.getRazonSocial());
		   		    	doc.setDocumentType("PRESTADOR_SERV_OBRAS"); 
		   		    	doc.setContent(uploadedFiles.get(0).getBytes());
		   		    	doc.setType(uploadedFiles.get(0).getContentType());
		   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
		   		    	doc.setSize(uploadedFiles.get(0).getSize());
		   		    	doc.setStatus(true);
		   		    	doc.setFolio("");
		   		    	doc.setUuid("");
		   		    	doc.setUploadDate(new Date());
		   		    	doc.setFrequency("QUARTER");
		   		    	doc.setCompany("");
		   		    	doc.setObsolete(false);
		   				doc.setDocStatus(FISCAL_DOC_PENDING);
		   		    	outSourcingDao.saveDocument(doc);
		   		    	
		   		    	dataAuditService.saveDataAudit("SaveQuarterlyDocs", addressNumber, currentDate, request.getRemoteAddr(),
		   		   		usr, uploadedFiles.get(0).getOriginalFilename(), "saveQuarterlyDocs", 
		   		   		"Uploaded Quarterly Document Successful - PRESTADOR_SERV_OBRAS",null, null, null, null, FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);
		   		    	
   					}*/
   					
	   		 	    s.setOutSourcingQuarterlyAccept(true);
	   		    	supplierService.updateSupplierCore(s);
	   		    		   		    	
	   		    	logger.log(LOG_DOCUMENTS, "DOCUMENTOS CUATRIMESTRALES PARA : " + s.getRazonSocial());
	   		    	
	   		    	String subject = OUTSOURCING_RECEIPT_SUBJECT_QUARTER;
	   		    	subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
	   		    	
	   		    	EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(OUTSOURCING_RECEIPT_MESSAGE), s.getEmailSupplier());
	   				emailAsyncSup.setMailSender(mailSenderObj);
	   				Thread emailThreadSup = new Thread(emailAsyncSup);
	   				emailThreadSup.start();
	   				
	   				String internalEmail = "";
	   		    	UDC udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_RH");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "CUATRIMESTRAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
	   				
	   		    	internalEmail = "";
	   		    	udc =  udcService.searchBySystemAndKey("OSAPPROVE", "ROLE_TAX");
					if(udc != null) {
						internalEmail = udc.getStrValue1();
					}
					
					if(!"".equals(internalEmail)) {
						emailAsyncSup = new EmailServiceAsync();
		   		    	String content = OUTSOURCING_SEND_MESSAGE;
		   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
		   		    	content = content.replace("_DOCTYPE_", "CUATRIMESTRAL");
		   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), internalEmail);
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
   				
   				}
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		
		return "";
	}
	    
	public synchronized String saveReplacementFile(MultipartFile file, String addressNumber, int id) {
		
		try {
			Supplier s = supplierService.searchByAddressNumber(addressNumber);	
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	 		String usr = auth.getName();
	 		Date currentDate = new Date();
	 		
			if(s != null) {
				
				OutSourcingDocument doc = outSourcingDao.getDocumentById(id);
				String currentName = doc.getName();
				doc.setNotes(doc.getNotes() + " / Se ha reemplazado un nuevo archivo para el anterior denominado " + doc.getName());
				doc.setName(file.getOriginalFilename());
				doc.setSize(file.getSize());
				doc.setStatus(true);
				doc.setType(file.getContentType());
				doc.setUploadDate(new Date());
				doc.setDocStatus(FISCAL_DOC_PENDING);
				doc.setContent(file.getBytes());
				outSourcingDao.updateDocument(doc);

				dataAuditService.saveDataAudit("ReplacementFile", s.getAddresNumber(), currentDate, request.getRemoteAddr(),
				usr, doc.getFrequency() + " - Replaced File Successful - " + file.getOriginalFilename(), "saveReplacementFile", 
			    "Se ha reemplazado un nuevo archivo para el anterior denominado" + doc.getName(), null, null,null,
			    null,FISCAL_DOC_PENDING, AppConstants.OUTSOURCING_MODULE);

				String subject = OUTSOURCING_RECEIPT_SUBJECT;
   		    	subject = subject.replace("_SUPPLIER_", s.getRazonSocial());
   				
   				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
   		    	String content = OUTSOURCING_SEND_MESSAGE;
   		    	content = content.replace("_SUPPLIER_", s.getRazonSocial());
   		    	content = content.replace("_DOCTYPE_", "DE REEMPLAZO POR RECHAZO");
   				emailAsyncSup.setProperties(subject, stringUtils.prepareEmailContent(content), s.getEmailSupplier());
   				emailAsyncSup.setMailSender(mailSenderObj);
   				Thread emailThreadSup = new Thread(emailAsyncSup);
   				emailThreadSup.start();
   				
				logger.log(LOG_DOCUMENTS, "REEMPLAZO DE ARCHIVO DEL PROVEEDOR : " + addressNumber + ". Original: " + currentName + " - Reemplazo: " + doc.getName());
			}
		}catch(Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return e.getMessage();
		}
		
		return "";
	}

    public synchronized String saveInvoiceDocs(MultiFileUploadBean multiFileUploadBean, String addressNumber, int orderNumber, String orderType) {
  		
		List<MultipartFile> uploadedFiles = multiFileUploadBean.getUploadedFiles();
   		
   		try {
   			if(uploadedFiles.size() == 1) {
   				
   				Supplier s = supplierService.searchByAddressNumber(addressNumber);
   				if(s != null) {
   					
	   				//Recibos de Nómina
	   				OutSourcingDocument doc = new OutSourcingDocument();
	   				doc.setAddressBook(addressNumber);
	   				doc.setSupplierName(s.getRazonSocial());
	   		    	doc.setDocumentType("REC_NOMINA");
	   		    	doc.setContent(uploadedFiles.get(0).getBytes());
	   		    	doc.setType(uploadedFiles.get(0).getContentType());
	   		    	doc.setName(uploadedFiles.get(0).getOriginalFilename());
	   		    	doc.setSize(uploadedFiles.get(0).getSize());
	   		    	doc.setStatus(true);
	   		    	doc.setFolio("");
	   		    	doc.setUuid("");
	   		    	doc.setOrderNumber(orderNumber);
	   		    	doc.setOrderType(orderType);
	   		    	doc.setUploadDate(new Date());
	   		    	doc.setFrequency("INV");
	   		    	doc.setCompany("");
	   		    	doc.setObsolete(false);
	   		    	outSourcingDao.saveDocument(doc);
	   		    	 
   				}
   			}
   	    	
   		}catch(Exception e) {
   			log4j.error("Exception" , e);
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		
		return "";
	}

	@Scheduled(cron="0 0 22 * * *") // Valida si no ha cargado documentos iniciales
	public void searchBaseLineDocs() {
		
		List<Supplier> supList = supplierService.searchByOutSourcingStatus();
		if(supList != null) {
			for(Supplier s : supList) {
				List<OutSourcingDocument> baseDocList = outSourcingDao.searchDocsByFrequency(s.getAddresNumber(), "BL");
				if(baseDocList == null || baseDocList.size() <=0) {
					s.setOutSourcingAccept(false);
					supplierService.updateSupplierCore(s);
				}
			}
		}	
	}
	
	@Scheduled(cron="0 0/40 13 17-28 * *") // Valida si despues de los 17 días de cada mes tiene documentos pendientes
	//@Scheduled(cron="0 0/40 13 7-28 * *")  // Valida si despues de los 7 (Por fin de año)
	public void searchMonthlyDocs() {
		
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int currentMonth = cal.get(Calendar.MONTH);
		
		List<Supplier> supList = supplierService.searchByOutSourcingStatus();
		if(supList != null) {
			for(Supplier s : supList) {
				if(s.isOutSourcingAccept()) {
					List<OutSourcingDocument> baseDocList = outSourcingDao.searchActiveDocsByFrequency(s.getAddresNumber(), "MONTH");
					if(baseDocList == null || baseDocList.size() <=0) {
						s.setOutSourcingMonthlyAccept(false);
						supplierService.updateSupplierCore(s);
						
						EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_MESSAGE), s.getEmailSupplier());
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				Thread emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
						
					}else {
						boolean isObsolete = false;
						for(OutSourcingDocument doc : baseDocList) {					
							cal.setTime(doc.getUploadDate());
							int documentMonth = cal.get(Calendar.MONTH);
							if(currentMonth == 1 && documentMonth == 12) {
								isObsolete = true;
								doc.setObsolete(true);
								outSourcingDao.updateDocument(doc);
							}else {
								if(documentMonth < currentMonth) {
									isObsolete = true;
									doc.setObsolete(true);
									outSourcingDao.updateDocument(doc);
								}
							}
							
						}
						
						if(isObsolete) {
							s.setOutSourcingMonthlyAccept(false);
							supplierService.updateSupplierCore(s);
							
							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_MESSAGE), s.getEmailSupplier());
			   				emailAsyncSup.setMailSender(mailSenderObj);
			   				Thread emailThreadSup = new Thread(emailAsyncSup);
			   				emailThreadSup.start();
						}
					}
				}
			}
		}	
	}
	
	@Scheduled(cron="0 0/30 13 5-10 * *") // Notifica los primeros días del mes
	public void searchMonthlyDocsNotification() {
		
		List<Supplier> supList = supplierService.searchByOutSourcingStatus();
		if(supList != null) {
			for(Supplier s : supList) {
				if(s.isOutSourcingAccept() && !s.isOutSourcingMonthlyAccept()) {
					EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	   				emailAsyncSup.setProperties(OUTSOURCING_NOTIF_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_NOTIF_MESSAGE), s.getEmailSupplier());
	   				emailAsyncSup.setMailSender(mailSenderObj);
	   				Thread emailThreadSup = new Thread(emailAsyncSup);
	   				emailThreadSup.start();
				}
			}
		}		
	}

	//@Scheduled(cron = "0 0/10 13 15-28 1,5,9 *")//Deshabilita los documentos vencidos que se cargaron el bimestre anterior
		@Scheduled(cron = "0 20 13 28-31 1,3,5,7,9,11 *")//Deshabilita los documentos vencidos que se cargaron el bimestre anterior
	public void searchBimonthlyDocs() {
		final Calendar c = Calendar.getInstance();
	    if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {//Verifica que sea el último día del mes para continuar con el proceso
	    
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			int currentMonth = cal.get(Calendar.MONTH);
			
			List<Supplier> supList = supplierService.searchByOutSourcingStatus();
			if(supList != null) {
				for(Supplier s : supList) {
					if(s.isOutSourcingAccept()) {
						List<OutSourcingDocument> baseDocList = outSourcingDao.searchActiveDocsByFrequency(s.getAddresNumber(), "SECOND");
						if(baseDocList == null || baseDocList.size() <=0) {
							s.setOutSourcingBimonthlyAccept(false);
							supplierService.updateSupplierCore(s);
	
							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_SECOND_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_SECOND_MESSAGE), s.getEmailSupplier());
			   				emailAsyncSup.setMailSender(mailSenderObj);
			   				Thread emailThreadSup = new Thread(emailAsyncSup);
			   				emailThreadSup.start();
							
						}else {
							boolean isObsolete = false;
							for(OutSourcingDocument doc : baseDocList) {					
								cal.setTime(doc.getUploadDate());
								int documentMonth = cal.get(Calendar.MONTH);
								if (currentMonth == 0 && documentMonth > 0) {
									isObsolete = true;
									doc.setObsolete(true);
									outSourcingDao.updateDocument(doc);
								} else {
									if (documentMonth < currentMonth) {
										isObsolete = true;
										doc.setObsolete(true);
										outSourcingDao.updateDocument(doc);
									}
								}
								
							}
							
							if(isObsolete) {
								s.setOutSourcingBimonthlyAccept(false);
								supplierService.updateSupplierCore(s);
								
								EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_SECOND_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_SECOND_MESSAGE), s.getEmailSupplier());
				   				emailAsyncSup.setMailSender(mailSenderObj);
				   				Thread emailThreadSup = new Thread(emailAsyncSup);
				   				emailThreadSup.start();
							}
						}
					}
				}
			}
	    }
	}
	
	//@Scheduled(cron="0 0/20 13 5-20 1,5,9 *") // Notifica los primeros días de cada bimestre (enero,mayo,sep)
	@Scheduled(cron="0 50 13 5-10 1,3,5,7,9,11 *") // Notifica los primeros días de cada bimestre (enero,mayo,sep)	
	public void searchBimonthlyDocsNotification() {
			
			List<Supplier> supList = supplierService.searchByOutSourcingStatus();
			if(supList != null) {
				for(Supplier s : supList) {
					if(s.isOutSourcingAccept() && !s.isOutSourcingBimonthlyAccept()) {
						EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		   				emailAsyncSup.setProperties(OUTSOURCING_NOTIF_SECOND_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_NOTIF_SECOND_MESSAGE), s.getEmailSupplier());
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				Thread emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
				}
			}		
		}
	
	
	//@Scheduled(cron = "0 0/10 13 15-28 1,5,9 *")//Deshabilita los documentos vencidos que se cargaron el cuatrimestre anterior
	@Scheduled(cron = "0 20 13 28-31 1,5,9 *")//Deshabilita los documentos vencidos que se cargaron el cuatrimestre anterior
	public void searchQuarterlyDocs() {
		final Calendar c = Calendar.getInstance();
	    if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {//Verifica que sea el último día del mes para continuar con el proceso
	    
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(today);
			int currentMonth = cal.get(Calendar.MONTH);
			
			List<Supplier> supList = supplierService.searchByOutSourcingStatus();
			if(supList != null) {
				for(Supplier s : supList) {
					if(s.isOutSourcingAccept()) {
						List<OutSourcingDocument> baseDocList = outSourcingDao.searchActiveDocsByFrequency(s.getAddresNumber(), "QUARTER");
						if(baseDocList == null || baseDocList.size() <=0) {
							s.setOutSourcingQuarterlyAccept(false);
							supplierService.updateSupplierCore(s);
	
							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_QUARTERLY_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_QUARTERLY_MESSAGE), s.getEmailSupplier());
			   				emailAsyncSup.setMailSender(mailSenderObj);
			   				Thread emailThreadSup = new Thread(emailAsyncSup);
			   				emailThreadSup.start();
							
						}else {
							boolean isObsolete = false;
							for(OutSourcingDocument doc : baseDocList) {					
								cal.setTime(doc.getUploadDate());
								int documentMonth = cal.get(Calendar.MONTH);
								if (currentMonth == 0 && documentMonth > 0) {
									isObsolete = true;
									doc.setObsolete(true);
									outSourcingDao.updateDocument(doc);
								} else {
									if (documentMonth < currentMonth) {
										isObsolete = true;
										doc.setObsolete(true);
										outSourcingDao.updateDocument(doc);
									}
								}
								
							}
							
							if(isObsolete) {
								s.setOutSourcingQuarterlyAccept(false);
								supplierService.updateSupplierCore(s);
								
								EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				   				emailAsyncSup.setProperties(OUTSOURCING_ALERT_QUARTERLY_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_ALERT_QUARTERLY_MESSAGE), s.getEmailSupplier());
				   				emailAsyncSup.setMailSender(mailSenderObj);
				   				Thread emailThreadSup = new Thread(emailAsyncSup);
				   				emailThreadSup.start();
							}
						}
					}
				}
			}
	    }
	}
	
	//@Scheduled(cron="0 0/20 13 5-20 1,5,9 *") // Notifica los primeros días de cada cuatrimestre (enero,mayo,sep)
	@Scheduled(cron="0 50 13 5-10 1,5,9 *") // Notifica los primeros días de cada cuatrimestre (enero,mayo,sep)	
	public void searchQuarterlyDocsNotification() {
			
			List<Supplier> supList = supplierService.searchByOutSourcingStatus();
			if(supList != null) {
				for(Supplier s : supList) {
					if(s.isOutSourcingAccept() && !s.isOutSourcingQuarterlyAccept()) {
						EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		   				emailAsyncSup.setProperties(OUTSOURCING_NOTIF_QUARTERLY_SUBJECT, stringUtils.prepareEmailContent(OUTSOURCING_NOTIF_QUARTERLY_MESSAGE), s.getEmailSupplier());
		   				emailAsyncSup.setMailSender(mailSenderObj);
		   				Thread emailThreadSup = new Thread(emailAsyncSup);
		   				emailThreadSup.start();
					}
				}
			}		
		}
		
	
	public List<OutSourcingDocument> searchDocsByQuery(String supplierName, String status, String documentType, 
			                                           String supplierNumber, int start, int limit, int monthLoad, int yearLoad){
		
		 List<OutSourcingDocument> list = outSourcingDao.searchDocsByQuery(supplierName, status, documentType, supplierNumber, start, limit, monthLoad, yearLoad);
		 if(list != null) {
			 for(OutSourcingDocument doc : list) {
				 doc.setContent(null);
			 }
			 return list;
		}else { 
			return null;
		}
	}
	

	public int searchDocsByQueryCount(String supplierName, String status, String documentType, String supplierNumber, int monthLoad, int yearLoad){
		return outSourcingDao.searchDocsByQueryCount(supplierName, status, documentType, supplierNumber, monthLoad, yearLoad);
	}
	


	public List<OutSourcingDocument> searchCriteria(String query){
		return outSourcingDao.searchCriteria(query);
	}
	
	public List<OutSourcingDocument> searchByAttachID(String attachId){
		return outSourcingDao.searchByAttachID(attachId);
	}
	
	public OutSourcingDocument saveDocument(OutSourcingDocument o) {
		return outSourcingDao.saveDocument(o);
	}

	public void updateDocument(OutSourcingDocument o) {
		outSourcingDao.updateDocument(o);
	}

	public void updateDocumentList(List<OutSourcingDocument> list) {
		outSourcingDao.updateDocumentList(list);
	}
	
	public void deleteDocument(int id) {
		outSourcingDao.deleteDocument(id);
	}
	
	public int getTotalRecords(){
		return outSourcingDao.getTotalRecords();
	}
	
	public String validateInvoiceCodes(List<Concepto> cList) {

		String textResult = "";
		List<String> osValidList = new ArrayList<String>();
		List<CodigosSAT> osList = codigosSATService.searchByTipoCodigo("OUTSOURCING");
		if(osList != null) {
			if(osList.size() > 0) {
				for(CodigosSAT cs : osList) {
					osValidList.add(cs.getCodigoSAT());
				}
			}
		}
		
		if(osValidList.size() > 0) {
			if(cList != null) {
				for(Concepto concepto : cList) {
					if(osValidList.contains(concepto.getClaveProdServ())) {
						textResult = "La factura contiene claves de producto servicio para servicios especializados";
						break;
					}
				}
			}
		}
		
		return textResult;
		
	}
	
	public InvoiceDTO getInvoiceXmlFromString(String xmlContent){
		try{
			InvoiceDTO dto = null;
			if(xmlContent.contains(AppConstants.NAMESPACE_CFDI_V4)) {
				dto = xmlToPojoService.convertV4(xmlContent);
			} else {
				dto = xmlToPojoService.convert(xmlContent);
			}
			return dto;
		}catch(Exception e){
			log4j.error("Exception" , e);
			return null;
		}
	}
	
	 public String getPendingDocuments(String addressNumber) {
		 List<OutSourcingDocument> pendingDocuments = outSourcingDao.searchPendingDocuments(addressNumber);
		 StringBuilder strb = new StringBuilder();
		 if(pendingDocuments != null) {
			 if(pendingDocuments.size() > 0) {
				 for(OutSourcingDocument doc : pendingDocuments) {
					 strb.append(doc.getName().trim());
					 strb.append(", ");
				 }
			 }
		 }
		 return strb.toString();
	 }

}
