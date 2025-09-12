package com.eurest.supplier.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dao.DocumentsDao;
import com.eurest.supplier.dao.PurchaseOrderDao;
import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.dto.ReportInvoicesDTO;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.util.StringUtils;

@Service("invoiceService")
public class InvoiceService {
	
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
	UsersService usersService;
	
	@Autowired
	DataAuditService dataAuditService;
	
	@Autowired
	FiscalDocumentService fiscalDocumentService;

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
    
    private Logger log4j = Logger.getLogger(InvoiceService.class);
		
	
	public List<ReportInvoicesDTO> searchDocsByQuery(String uuid, String supplierNumber, int start, int limit, Date poFromDate, Date poToDate, String pFolio, String module) throws ParseException{ // String status, String documentType, int monthLoad, int yearLoad
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		ReportInvoicesDTO reportInvoicesDTO = new ReportInvoicesDTO();
		List<ReportInvoicesDTO> resultList = new ArrayList<ReportInvoicesDTO>();
		if("PO".equals(module)) {
		List<Receipt> receiptList = purchaseOrderDao.searchReceiptsByQuery(uuid, supplierNumber, start, limit, poFromDate, poToDate, pFolio); // status documentType monthLoad, yearLoad
		 if(receiptList != null) {
			 for(Receipt rec : receiptList) {
				 reportInvoicesDTO = new ReportInvoicesDTO();
				 reportInvoicesDTO.setId(rec.getId());
				 reportInvoicesDTO.setAddressBook(rec.getAddressNumber());
				 reportInvoicesDTO.setSerie(rec.getSerie());
				 reportInvoicesDTO.setFolio(rec.getFolio());
				 reportInvoicesDTO.setUuid(rec.getUuid());
				 reportInvoicesDTO.setInvoiceDate(formatter.format(rec.getInvDate()));
				 UserDocument doc = documentsDao.searchInvXMLByUuidOnly(rec.getUuid());
				 if(doc != null) {
					 InvoiceDTO invoiceDTO = documentsService.getInvoiceXmlFromBytes(doc.getContent());
					 if(invoiceDTO != null) {
						 reportInvoicesDTO.setRfcEmisor(invoiceDTO.getRfcEmisor());
						 reportInvoicesDTO.setRfcReceptor(invoiceDTO.getRfcReceptor());
						 reportInvoicesDTO.setMoneda(invoiceDTO.getMoneda());
						 reportInvoicesDTO.setTotal(String.valueOf(invoiceDTO.getTotal()));
					 }
				 }
				 resultList.add(reportInvoicesDTO);
			 }			 
		   }
		}
		 

		 //Factura sin OC
		if("FD".equals(module)) {
		 List<FiscalDocuments> fiscalList = fiscalDocumentService.searchFiscalDocsByQuery(uuid, supplierNumber, start, limit, poFromDate, poToDate, pFolio);
		 if(fiscalList != null) {
			 for(FiscalDocuments fd : fiscalList) {
				 reportInvoicesDTO = new ReportInvoicesDTO();
				 reportInvoicesDTO.setId(fd.getId());
				 reportInvoicesDTO.setAddressBook(fd.getAddressNumber());
				 reportInvoicesDTO.setSerie(fd.getSerie());
				 reportInvoicesDTO.setFolio(fd.getFolio());
				 reportInvoicesDTO.setUuid(fd.getUuidFactura());
				 LocalDateTime dateTime = LocalDateTime.parse(fd.getInvoiceDate());
				 reportInvoicesDTO.setInvoiceDate(fd.getInvoiceDate() != null ? dateTime.format(dateFormatter) : null);
				 UserDocument doc = documentsDao.searchInvXMLByUuidOnly(fd.getUuidFactura());
				 if(doc != null) {
					 InvoiceDTO invoiceDTO = documentsService.getInvoiceXmlFromBytes(doc.getContent());
					 if(invoiceDTO != null) {
						 reportInvoicesDTO.setRfcEmisor(invoiceDTO.getRfcEmisor());
						 reportInvoicesDTO.setRfcReceptor(invoiceDTO.getRfcReceptor());
						 reportInvoicesDTO.setMoneda(invoiceDTO.getMoneda());
						 reportInvoicesDTO.setTotal(String.valueOf(invoiceDTO.getTotal()));
					 }
				 }
				 resultList.add(reportInvoicesDTO);
			 }
			 
		   }
	     }
	   
		 return resultList;
	}
	

	

	public int searchDocsByQueryCount(String uuid, String supplierNumber, Date poFromDate, Date poToDate, String pFolio, String module){ // String status String documentType, String supplierName,  int monthLoad, int yearLoad
		int total = 0;
		if("PO".equals(module)) {
		 total = purchaseOrderDao.searchReceiptsByQueryCount(uuid, supplierNumber, poFromDate, poToDate, pFolio); // status documentType
		}
		
		if("FD".equals(module)) {
		 total = fiscalDocumentService.searchFiscalDocsByQueryCount(uuid, supplierNumber, poFromDate, poToDate, pFolio);
		}
		
		return total;
	}
	



}
