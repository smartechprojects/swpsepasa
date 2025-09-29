package com.eurest.supplier.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public final class AppConstants {

    public enum STATUS{
    	 STATUS_OC_REJECTED,
   	 	 STATUS_OC_REQUESTED,
	   	 STATUS_OC_RECEIVED,
	   	 STATUS_OC_APPROVED,
	   	 STATUS_OC_SENT,
	   	 STATUS_OC_CLOSED,
	   	 STATUS_OC_INVOICED,
	   	 STATUS_OC_PROCESSED,
	   	 STATUS_OC_PAID,
	   	 STATUS_OC_CANCEL;
    }

    EnumMap<STATUS, String> statusMap = new EnumMap<STATUS, String>(STATUS.class);
	
    public static final String USER_SMARTECH = "SMARTECH"; 
    
    public static final String INVOICE_FIELD = "Factura";
    
    public static final String NC_FIELD = "NotaCredito";
    
    public static final String NC_TC = "E";
    
    public static final String PAYMENT_FIELD = "ComplementoPago";
    
    public static final String INVOICE_FIELD_UDC = "FACTURA";
    
    public static final String NC_FIELD_UDC = "NOTACREDITO";
    
    public static final String PAYMENT_FIELD_UDC = "COMPLEMENTOPAGO";
    
    public static final String OTHER_FIELD = "Otros";
    
    public static final String MESSAGE_FIELD = "MESSAGE";
    
    public static final String WELCOME_FIELD = "HOMEPAGE";
    
    public static final String STATUS_LOADINV = "FAC_CARGADA";
    
    public static final String STATUS_LOADINV_VALIDATE = "POR_VALIDAR";
    
    public static final String STATUS_LOADNC = "NC_CARGADA";
    
    public static final String STATUS_LOADCP = "CP_CARGADO";
    
    public static final String STATUS_ACCEPT = "APROBADO";
    
    public static final String STATUS_INPROCESS = "PENDIENTE";
    
    public static final String STATUS_REJECT = "RECHAZADO";
    
    public static final String STATUS_PAID = "PAGADO";
    
    public static final String STATUS_COMPLEMENT = "COMPLEMENTO";
    
    public static final String STATUS_APPROVALFIRSTSTEP = "FIRST";
    
    public static final String STATUS_APPROVALSECONDSTEP = "SECOND";
    
    public static final String STATUS_APPROVALTHIRDSTEP = "THIRD";
    
    public static final String STATUS_APPROVALFOURTHSTEP = "FOURTH";
    
    public static final String STATUS_RECEIVED = "CERRADA";
    
    public static final String STATUS_PARTIAL = "PARCIAL";
    
    public static final String STATUS_UNCOMPLETE = "UNCOMPLETE";
    
    public static final String STATUS_COMPLETE = "COMPLETE";
    
    public static final String INV_TYPE_RECEIPT = "RECIBO";
    
    public static final String EMAIL_FROM = "portal-proveedores@smartechcloud-apps.com";
    
    public static final String CFDI_V3 = "3.3";
    
    public static final String CFDI_V4 = "4.0";

    public static final String NAMESPACE_CFDI_V3 = "http://www.sat.gob.mx/cfd/3";
    
    public static final String NAMESPACE_CFDI_V4 = "http://www.sat.gob.mx/cfd/4";
    
    public static final String START_PASS = "NuevoProv.29";
    
    public static final String START_PASS_ENCODED = "==a20$TnVldm9Qcm92LjI5";
    
    public static final String EMAIL_APPROVAL_SENT = "Su solicitud ha sido enviada para revisión. El número de ticket asociado es: ";
    
    public static final String EMAIL_APPROVAL_PURCHASE_MSG = "Su solicitud ha sido aprobada por el área de compras. <br /><br /> Notas: <br /> ";
    
    public static final String EMAIL_APPROVAL_CXP_MSG = "Su solicitud ha sido aprobada por el de CxP. <br /><br /> Notas: <br /> ";
    
    public static final String EMAIL_APPROVAL_CXPMGR_MSG = "Su solicitud ha sido aprobada por la directiva";
    
    public static final String EMAIL_APPROVAL_QA_MSG = "Su solicitud ha sido aprobada por el personal de Calidad";
    
    public static final String EMAIL_APPROVAL_APPROVED = "Su solicitud ha sido APROBADA. En los próximos días recibirá correo con los detalles de su ingreso al portal";
    
    public static final String EMAIL_APPROVAL_REJECTED = "Su solicitud ha sido RECHAZADA. Motivo: ";
    
    public static final String EMAIL_RECEIPT_COMPLETE = "El recibo ha sido completado para la Orden de Compra: ";
    
    public static final String EMAIL_RECEIPT_INCOMPLETE = "El recibo de los productos ha sido rechazado para la Orden de Compra: ";
    
    public static final String EMAIL_PURCHASE_NEW = "Estimado Usuario. La siguiente orden de compra ha sido autorizada. Favor de revisar los detalles en el Portal de Proveedores:<br /><br />";
    
    public static final String EMAIL_PURCHASE_NEW2 = "Estimado Proveedor. La siguiente orden de compra ha sido solicitada. Favor de revisar los detalles en el Portal de Proveedores:<br /><br />";

    public static final String EMAIL_PURCHASE_NEW3 = "Estimado Usuario. La siguiente orden de compra ha sido rechazada. Favor de revisar los detalles en el Portal de Proveedores:<br /><br />";
    
    public static final String EMAIL_RECEIPT_NEW = "Estimado Proveedor. Se ha completado un recibo de producto o servicio. Favor de considerar la carga de su factura para el recibo número: <br /><br />";
    
    public static final String EMAIL_INVOICE_ACCEPTED = "Estimado Proveedor: <br /><br />Su factura se ha recibido correctamente, y será programada a pago a partir de la fecha de recepción de la factura.<br />" +"La orden de compra asociada a su factura es: ";
    
    public static final String EMAIL_INVOICE_ACCEPTED_NOTIF = "Estimado Proveedor. Su factura ha sido ACEPTADA y enviada a pago. El uuid de la factura aceptada es: ";

    public static final String EMAIL_INVOICE_REJECTED_NOTIF = "Estimado Proveedor. Su factura ha sido RECHAZADA. El uuid de la factura rechazada es: ";
    
    public static final String EMAIL_CN_ACCEPTED = "Estimado Proveedor. La nota de crédito ha sido cargada y enviada a revisión. La Orden de Compra asociada a su nota de crédito es: ";
    
    public static final String EMAIL_INVBATCH_ACCEPTED = "Estimado Proveedor. Las facturas han sido cargadas y enviadas a revisión. La Orden de Compra asociada a sus facturas es: ";
    
    public static final String EMAIL_CNBATCH_ACCEPTED = "Estimado Proveedor. Las notas de crédito han sido cargadas y enviadas a revisión. La Orden de Compra asociada a sus notas de crédito es: ";
    
    public static final String EMAIL_FOREIGINVOICE_ACCEPTED = "Estimado colaborador, una factura de extranjeros ha sido cargada y enviada a revisión. La Orden de Compra asociada a su factura es: ";
    
    public static final String EMAIL_FOREIGINVOICE_RECEIVED = "Estimado colaborador, una factura de extranjeros ha sido recibida. La Orden de Compra asociada a su factura es: ";
    
    public static final String EMAIL_INVOICE_UPLOAD = "Notificación del portal: Una factura ha sido cargada y enviada a revisión. El proveedor y la Orden de Compra asociada es: ";
    
    public static final String EMAIL_INVOICEANDCN_ACCEPTED = "La factura y la nota de crédito han sido aceptadas para la Orden de Compra: ";
    
    public static final String EMAIL_INVOICE_NOPURCHASE_ACCEPTED = "Estimado Proveedor. Su factura ha sido aceptada para pago. La orden de compra asociada a su factura aceptada es: ";
    
    public static final String EMAIL_INVOICE_ACCEPTED_NOPAYMENT = "La factura se ha enviado a validación por el personal de Cuentas por Pagar. Se le notificará por correo en cuanto sea aceptada para pago. La orden de compra relacionada es: ";
    
    public static final String EMAIL_INVOICE_REJECTED = "La factura ha sido RECHAZADA para la Orden de Compra: ";
    
    public static final String EMAIL_INVBATCH_REJECTED = "Las facturas han sido RECHAZADAS para la Orden de Compra: ";
    
    public static final String EMAIL_INVOICE_REJECTED_WITHOUT_OC = "La factura ha sido RECHAZADA (Sin Orden de Compra).";
    
    public static final String EMAIL_CNBATCH_REJECTED = "Las notas de crédito han sido RECHAZADAS para la Orden de Compra: ";
    
    public static final String EMAIL_CONTACT_SUPPORT = "<br /><br />Póngase en contacto para conocer los detalles: ";
    
    public static final String EMAIL_INVOICE_SUBJECT = "SEPASA - Notificación del Portal de Proveedores. No Responder.";
    
    public static final String EMAIL_NEW_SUPPLIER_ACCEPT = "SEPASA - Notificación de aceptación de portal de proveedores.";
    
    public static final String EMAIL_NEW_SUPPLIER_REJECT = "SEPASA - Notificación de rechazo de portal de proveedores.";
    
    public static final String EMAIL_ACCEPT_SUPPLIER_NOTIFICATION = "Estimado proveedor: <br /> Su solicitud con número _NUMTICKET_, ha sido ACEPTADA.<br />Ponemos a su disposición las credenciales para poder acceder a nuestro portal _URL_: <br />Usuario: _USER_<br />Contraseña: _PASS_<br />"; 
    
    public static final String EMAIL_REJECT_SUPPLIER_NOTIFICATION = "Estimado proveedor: <br /> Su solicitud con número _NUMTICKET_, desafortunadamente ha sido RECHAZADA.<br />El motivo es: _REASON_ <br /> Favor de volver a ingresar de nuevo al portal _URL_ en la opción de Click aquí para registrarse como un nuevo proveedor.<br />Posteriormente en el campo de \"Num de ticket\" ingresar el ticket previamente mencionado, dar clic en Buscar y favor de modificar los datos que se han indicado previamente."; 
    
    public static final String EMAIL_MASS_SUPPLIER_NOTIFICATION = "Estimado Proveedor. <br /><br />Usted ha sido aprobado para utilizar el Portal de Proveedores de la compañía SEPASA. A continuación encontrará sus credenciales temporales de acceso.<br /><br />";
  
    public static final String EMAIL_FIRST_APP_SUBJECT = "SEPASA - Solicitud de la 1a aprobación de proveedor.";
    
    public static final String EMAIL_FIRST_APP_CONTENT = "Estimado Aprobador:<br />La solicitud con número _NUMTICKET_ requiere de su aprobación.<br />Favor de revisar la información en el portal _URL_";

    public static final String EMAIL_SECOND_APP_SUBJECT = "SEPASA - Solicitud de la 2da aprobación de proveedor.";
    
    public static final String EMAIL_SECOND_APP_CONTENT = "Estimado Aprobador:<br />La solicitud con número _NUMTICKET_ requiere de su aprobación.<br />Favor de revisar la información en el portal _URL_";
    
    public static final String EMAIL_THIRD_APP_SUBJECT = "SEPASA - Solicitud de la 3ra aprobación de proveedor.";

    public static final String EMAIL_REQUEST_RECEIVED_SUBJECT = "SEPASA - Solicitud de alta de proveedor recibida.";
    
    public static final String EMAIL_REQUEST_RECEIVED_CONTENT = "Estimado Proveedor:<br />Su solicitud con número _NUMTICKET_, ha sido RECIBIDA.<br /> En breve analizaremos la información proporcionada y nos contactaremos con usted.<br />Gracias por querer ser nuestro socio comercial. ";
    
    public static final String EMAIL_MASS_SUPPLIER_CHANGE_NOTIFICATION = "Estimado Proveedor. <br /><br />La actualización de datos en el portal de proveedores ha sido revisada y aprobada por la compañía SEPASA.<br /><br />";
    
    public static final String EMAIL_SUPPPLIER_NOTIFICATION_PURCHASE = "Tiene una solicitud de Alta de Proveedor por revisar y aprobar con ticket: ";
    
    public static final String EMAIL_DRAFT_SUPPPLIER_NOTIFICATION_PURCHASE = "Estimado proveedor <br /><br />Hemos recibido una solicitud de tipo borrador en nuestros sistemas. Su solicitud será procesada una vez que someta el formato de forma definitiva. <br /> <br /> Puede continuar actualizando sus datos utilizando el número de ticket que le enviamos a continuación ";
    
    public static final String EMAIL_INV_JDE_ERROR_SUBJECT =  "SEPASA - Notificación de incidencia de factura recibida.";
    
    public static final String EMAIL_INV_JDE_ERROR_CONTENT =  "Estimado Usuario:<br />Se recibió la factura _VINV_ correspondiente al proveedor _SUPPLIER_, sin embargo no fue posible realizar el envío a JDE.<br />En breve se enviará por medio de nuestro proceso automático.<br /><br />*Este mensaje fue generado de forma automática, no es necesario responder a este mensaje.";   

    public static final String EMAIL_NON_COMPLIANCE_SUBJECT =  "SEPASA - Notificación de proveedor no cumplido.";
    
    public static final String EMAIL_NON_COMPLIANCE_CONTENT =  "Estimado Usuario:<br />El proveedor _ADDRESS_ - _NAME_ con RFC _RFC_ registrado en el portal, se encuentra en la lista de proveedores no cumplidos del SAT.<br /><br />*Este mensaje fue generado de forma automática, no es necesario responder a este mensaje.";
    
    public static final String NN_MODULE_PURCHASE = "PURCHASE";
    
    public static final String NN_MODULE_RECEIPT = "RECEIPT";
    
    public static final String NN_MODULE_VOUCHER = "VOUCHER";
    
    public static final String NN_MODULE_BATCHJOURNAL = "BATCHJOURNAL";
    
    public static final String NN_MODULE_ADDRESSBOOK = "ADDRESSBOOK";
 
    public static final String NN_MODULE_SUPPLIER = "SUPPLIER";
    
    public static final String NN_MODULE_INVBATCH = "INVBATCH";
    
    public static final String ROLE_SUPPLIER = "SUPPLIER";
    
    public static final String ROLE_PURCHASE = "ROLE_PURCHASE";
    
    public static final String ROLE_ADMIN_PURCHASE = "ROLE_ADMIN_PURCHASE";
    
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    public static final String ROLE_PURCHASE_IMPORT = "ROLE_PURCHASE_IMPORT";
    
    public static final String ROLE_TAX = "ROLE_TAX";
    
    public static final String ROLE_SUPPLIER_EMAIL = "ROLE_SUPPLIER";
    
    public static final String ROLE_PURCHASE_VALID = "ROLE_SUPPLIER";
    
	 public static final String CFDI_VALIDATION_URL = "https://consultaqr.facturaelectronica.sat.gob.mx/ConsultaCFDIService.svc";
	 public static final String CFDI_VALIDATION_ACTION = "http://tempuri.org/IConsultaCFDIService/Consulta";
    
    public static final String CFDI_SUCCESS_MSG = "S - Comprobante obtenido satisfactoriamente.";
    
    public static final String CFDI_SUCCESS_MSG_ACTIVE = "Vigente";
    
    public static final String STATUS_OC_RECEIVED = "OC RECIBIDA";
    
    public static final String STATUS_OC_REJECTED = "OC RECHAZADA";
    public static final String STATUS_OC_REQUESTED = "OC SOLICITADA";
    
    public static final String STATUS_OC_APPROVED = "OC APROBADA";
    
    public static final String STATUS_OC_SENT = "OC ENVIADA";
    
    public static final String STATUS_OC_CLOSED = "OC CERRADA";
    
    public static final String STATUS_OC_PENDING = "OC PENDIENTE";
    
    public static final String STATUS_OC_INVOICED = "OC FACTURADA";
    
    public static final String STATUS_OC_PROCESSED = "OC PROCESADA";
    
    public static final String STATUS_OC_PAID = "OC PAGADA";
    
    public static final String STATUS_GR_PAID = "P";
    
    public static final String STATUS_OC_PAYMENT_COMPL = "OC COMPLEMENTO";
    
    public static final String STATUS_OC_CANCEL = "OC CANCELADA";
    
    public static final String STATUS_OC_FOREIGN = "OC EXTRANJERO";
    
    public static final String STATUS_FACT_FOREIGN = "FACT EXTRANJERO";
    
    public static final String RECEIPT_MASSIVE_UPLOAD = "CARGA MASIVA";
    
    public static final String SAT_CFDIVER = "CFDI33";
    
    public static final String SAT_NONCOMPLANCE_URL = "http://omawww.sat.gob.mx/cifras_sat/Documents/Listado_Completo_69-B.csv";
    
    public static final String SAT_NONCOMPLANCE_EXIGIBLES_URL = "http://omawww.sat.gob.mx/cifras_sat/Documents/Exigibles.csv";
    
    public static final String SAT_NONCOMPLANCE_FIRMES_URL = "http://omawww.sat.gob.mx/cifras_sat/Documents/Firmes.csv";

    public static final String SAT_NONCOMPLANCE_SENTENCIAS_URL = "http://omawww.sat.gob.mx/cifras_sat/Documents/sentencias.csv";
    
    public static final String SAT_NONCOMPLANCE_NO_LOCALIZADOS_URL = "http://omawww.sat.gob.mx/cifras_sat/Documents/No%20Localizados.csv";
    
    public static final String REF_METODO_PAGO = "PPD";
    
    public static final String REF_METODO_PAGO_PUE = "PUE";
    
    public static final String REF_FORMA_PAGO = "99";
    
    public static final String USO_CFDI = "P01";
    
    public static final String CANCEL_JDE_STS = "999";
    
    public static final String JDE_RETENTION_CODE = "2171";
    
    public static final String RECEIPT_CODE_INVOICE = "I";
    
    public static final String RECEIPT_CODE_RETENTION = "R";
    
    public static final String RECEIPT_CODE_CREDIT_NOTE = "N";
    
    public static final String URL_HOST = "https://pxpmiddleware.grupohame.com:8443"; //"http://200.33.142.39:8080";
    
    public static final String URL_UPDATE_ORDERSTATUS = "http://localhost:8081/supplierWebPortalRestHame/poUpdateOrderStatus";
    
    public static final String EMAIL_PORTAL_LINK = "https://3.222.152.41/supplierWebPortalSepasa/";
    
    public static final String EMAIL_NEW_SUPPLIER_SUBJECT = "SEPASA - Solicitud Alta de Proveedor";
    
    public static final String EMAIL_DRAFT_SUPPLIER_SUBJECT = "SEPASA - Registro como borrador en Alta de Proveedor. Ticket ";
    
    public static final String EMAIL_NEXT_NOTIFICATION = "Tiene una solicitud de aprobación de un nuevo proveedor en el portal. El número de ticket asociado es: ";      
    
    public static final String EMAIL_APPROVED_SUPPLIER_SUBJECT = "SEPASA - Solicitud Alta de Proveedor Aprobada";
    
    public static final String EMAIL_NEW_ORDER_NOTIF = "SEPASA - Nueva Orden de Compra No. ";
    
    public static final String EMAIL_NEW_RECEIPT_NOTIF = "SEPASA - Nuevo Recibo para la Orden de Compra No. ";
    
    public static final String EMAIL_PAYMENT_RECEIPT_NOTIF = "SEPASA - Notificación de pago de factura. ";
    
    public static final String EMAIL_PAYMENT_RECEIPT_NOTIF_CONTENT = "Estimado proveedor <br /><br /> A través de este medio le notificamos el pago de la siguiente factura: <br/><br />No. Factura: _VINV_<br />Orden de Compra No.: _PO_<br />Fecha de pago: _DATE_<br />Importe: _AMOUNT_<br />Id del pago: _PID_<br />Uuid: _UUID_<br /><br />";
    
    public static final String EMAIL_PAYMENT_NO_OC_NOTIF_CONTENT = "Estimado proveedor <br /><br /> A través de este medio le notificamos el pago de la siguiente factura: <br/><br /> Uuid: _UUID_ <br /> Fecha de pago: _DATE_ <br /> Importe: _AMOUNT_<br />Id del pago: _PID_<br /> <br />  ";
    
    public static final String EMAIL_NO_COMPLIANCE_INVOICE = "SEPASA - Proveedor en lista negra. ";
    
    public static final String EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER = "El siguiente proveedor intentó ingresar una factura, sin embargo, se detectó que se encuentra en la lista negra: <br /><br/>";
    
    public static final String EMAIL_NO_COMPLIANCE_INVOICE_SUPPLIER_NOTIF = "Estimado proveedor <br /> Su factura ha sido rechazada debido a que hemos detectado que existen problemas con su situación fiscal:  <br/><br/>";    
    
    public static final String DEFAULT_CURRENCY = "MXN";
    
    public static final String DEFAULT_CURRENCY_JDE = "PME";
    
    public static final String EMAIL_INV_ACCPET_BUYER = "SEPASA - Solicitud de Revisión y Aprobación de Factura para la Orden de Compra No. ";
    
    public static final String EMAIL_INV_ACCEPT_SUP = "SEPASA - Notificación de aceptación del portal de proveedores para la Orden de Compra No. ";
    
    public static final String EMAIL_NC_ACCEPT_SUP = "SEPASA - Nota de crédito recibida para la Orden de Compra No. ";
    
    public static final String EMAIL_INV_REJECT_SUP = "SEPASA - Factura RECHAZADA para la Orden de Compra No. ";
    
    public static final String EMAIL_INV_REQUEST_NO_OC = "SEPASA - Solicitud de Factura sin Orden de Compra";     
    
    public static final String EMAIL_INV_APPROVAL_MSG_1_NO_OC = "Tiene una solicitud de aprobación de una factura sin orden de compra en el Portal de Proveedores. Con el UUID: ";
    
    public static final String EMAIL_INV_APPROVAL_MSG_2_NO_OC = " del proveedor ";
    
    public static final String EMAIL_INV_REJECT_SUP_NO_OC = "SEPASA - Factura RECHAZADA sin Orden de Compra";
    
    public static final String EMAIL_NC_REJECT_SUP_NO_OC = "SEPASA - Nota de Crédito RECHAZADA sin Orden de Compra.";
    
    public static final String EMAIL_INV_APPROVED_SUP = "SEPASA - Factura ACEPTADA para la Orden de Compra No. ";
    
    public static final String EMAIL_INV_APPROVED_SUP_NO_OC = "SEPASA - Factura ACEPTADA sin Orden de Compra";
    
    public static final String EMAIL_INV_PAYMENT_SUP = "SEPASA - Notificación de Factura PAGADA ";
    
    public static final String EMAIL_INV_BATCH_ACCEPT_SUP = "SEPASA - Facturas Recibidas para la Orden de Compra No. ";
    
    public static final String EMAIL_NC_BATCH_ACCEPT_SUP = "SEPASA - Notas de Crédito Recibidas para la Orden de Compra No. ";
    
    public static final String EMAIL_INV_BATCH_REJECT_SUP = "SEPASA - Facturas RECHAZADAS para la Orden de Compra No. ";
    
    public static final String EMAIL_NC_BATCH_REJECT_SUP = "SEPASA - Notas de Crédito RECHAZADAS para la Orden de Compra No. ";
  
    public static final String PASS_RESET = "SEPASA - Notificación de Cambio de Contraseña";
    
    public static final String EMAIL_PASS_RESET_NOTIFICATION = "Estimado Proveedor. <br />Usted ha solicitado un cambio de contraseña. Ingrese al portal con las siguientes credenciales y posteriormente renueve su contraseña<br /><br />Credenciales temporales de acceso: <br />";
    
    public static final String EMAIL_REASIGN_SUBJECT = "SEPASA - Reasignado:Solicitud de revisión y aprobación de proveedores";
    
    public static final String EMAIL_REASIGN_CONTENT = "Estimado Aprobador:<br />Usted ha sido reasignado para revisar y validar nueva información de proveedores que ha sido registrada en el Portal de Alta de Proveedores. La solicitud con número _NUMTICKET_ requiere de su revisión y aprobación.<br />Consulte los detalles utilizando el siguiente enlace: _URL_";
    
    public static final String NO_VALIDATE_DATE = "DATE";
    
    public static final String PROPINA_TEXT = "/PROPINA";
    
    public static final String DOCTYPE_REGALIAS = "RE";
    
    public static final String DOCTYPE_PROD = "GR";
    
    public static final String DOCTYPE_PUB1 = "P1";
    
    public static final String DOCTYPE_PUB2 = "P2";
    
    public static final String DOCTYPE_OPS = "GR";
    
    public static final String FACTURA_PUE = "PUE";
    
    public static final String OFFSET_DAYS = "OFFSET";
    
    public static final String LOG_INVREJECTED_TITLE = "RECHAZO_FACTURA";
    
    public static final String LOG_INVREJECTED_MDG = "La factura de la siguiente orden ha sido rechazada: ";
    
    public static final String LOG_REASIGN_TITLE = "REASIGNAR_ORDEN";
    
    public static final String LOG_REASIGN_MSG = "La orden ORDER_NUMBER ha sido reasignada a la cuenta NEW_ORDER_EMAIL: ";
    
    public static final String LOG_BATCH_PROCESS = "Proceso BATCH";
    
    public static final String LOG_BATCH_PROCESS_CODSAT = "El proceso de carga de los códigos de SAT ha finalizado exitosamente. Registros: ";

    public static final String LOG_BATCH_PROCESS_MASS_LOAD = "Carga Masiva BATCH";
    
    public static final String LOG_BATCH_PROCESS_MASS_LOAD_MSG = "Se asigna con éxito la factura INVOICE_NUMBER a la Orden de Compra No. DOCUMENT_NUMBER-DOCUMENT_TYPE del proveedor ADDRESS_NUMBER.";
    
    public static final String LOG_BATCH_PROCESS_MASS_LOAD_ERR =  "Ha ocurrido un error al asignar la factura INVOICE_NUMBER a la Orden de Compra No. DOCUMENT_NUMBER-DOCUMENT_TYPE del proveedor ADDRESS_NUMBER.";
    
    public static final String FTP_FILEPATH = "/PROD/FACTURAS/INPUT/";
    
    public static final String FTP_FILEPATH_OTHERS = "/PROD/FACTURAS/OTROS/";
    
    public static final String FTP_FILEPATH_COMPLETE = "/PROD/FACTURAS/OUTPUT/";
    
    public static final String FTP_FILEPATH_ERROR = "/PROD/FACTURAS/ERROR/";
    
    public static final String LOG_FTP_PROCESS = "FACTURAS_FTP";
    
    public static final String LOCAL_COUNTRY_CODE = "MX";
    
    public static final String NON_COMPLIANCE_ACCEPT = "Sentencia Favorable";
    
    public static final String NON_COMPLIANCE_REJECT_1 = "Definitivo";
    
    public static final String NON_COMPLIANCE_REJECT_2 = "Presunto";
    
    public static final String NON_COMPLIANCE_REJECT_3 = "Desvirtuado";
    
    public static final String LOG_DELETE_DOC = "ELIMINAR_DOCTO";
    
    public static final String LOG_DELDOC_MSG = "El documento DOC_NAME parteneciente a la orden ORDER_NUMBER ha sido eliminado por USER_NAME ";

    public static final String ETHIC_CONTENT = "";
    /*
    		"<p style='text-align: justify;'>" +
    		"<b style='font-size:12px;font-weight:bold;color:cornflowerblue;'>CÓDIGO DE CONDUCTA DE SEPASA</b>" +
    		"<b style='font-size:12px;font-weight:bold;'> ¿TIENES PREGUNTAS O ALGO QUE REPORTAR?</b><br />" +
    		"Todos los colaboradores tanto internos como externos de SEPASA, somos responsables de cumplir" + 
    		"con el Código de Conducta de SEPASA y de reportar cualquier conducta que vaya en contra de este Código," + 
    		"así como de cumplir y reportar violaciones a leyes o disposiciones externas, políticas y procedimientos" + 
    		"internos de SEPASA. Dichos reportes podrán realizarse a través de la Línea de Ética de SEPASA" + 
    		"Energía, donde serán canalizados e investigados por la Dirección de Cumplimiento, de manera" + 
    		"confidencial y en su caso, anónimamente." +
    		"</p>" +
    		"<p>La Línea de Ética de SEPASA está disponible los 365 días del año, las 24 horas, en los siguientes medios de contacto:</p>" +
    		"<div align='center'>" +
	    		"<b style='font-size:16px;color:navy;'>Atención telefónica</b><br />" + 
	    		"<b style='font-size:18px;color:cornflowerblue;font-weight:bold;'>800 999 0784</b><br />" + 
	    		"<b style='font-size:16px;color:navy;'>Página web</b><br />" + 
	    		"<b>https://www.tipsanonimos.com/eticaSEPASA/</b><br />" +
	    		"<b style='font-size:16px;color:navy;'>Correo electrónico</b><br />" +
	    		"<b>eticaSEPASA@tipsanonimos.com</b>" +
    		"</div>";
    */
    public static final String ETHIC_CONTENT_OLD = "";
    /*
    		"CÓDIGO DE ÉTICA <br />Estimado Usuario: <br /> " + 
    		"En nombre de SEPASA agradezco tu interés en nuestro sitio de Tips Anónimos. Para nosotros es muy importante contar <br /> " + 
    		"con medios que nos permitan detectar conductas que vayan en contra de nuestro Código de ética, fortaleciendo nuestros principios y valores. <br /> " +  
    		"Para garantizar la confidencialidad y anonimato de tus denuncias, hemos contratado a Deloitte, una firma con más de 10 años de experiencia  <br /> " + 
    		"en el servicio. Ponemos a tu disposición los siguientes medios:  <br /> " + 
    		"1.Línea telefónica sin costo: 01 800 999 0784   <br />" +
    		"2.Página Web: https://www.tipsanonimos.com/eticaSEPASA/   <br /> " + 
    		"3.Email: eticaSEPASA@tipsanonimos.com   <br /> " + 
    		"4.Fax: 01 (55) 5255 1322   <br /> " + 
    		"5.Apartado Postal: Galaz, Yamazaki, Ruiz Urquiza, S.C., A.P. (CON-080), Ciudad de México, CP 06401  <br />" +
    		"Muchas Gracias,<br />" +
    		"Director General<br /><br />";
    */
    public static final String ETHIC_CONTENT_INVOICE = 
    		"Estimado Proveedor: <br /><br />Su factura fue recibida con éxito. Le recordamos que:<br /><br />" + 
    		"■Los términos de pago de las facturas inician conforme a la fecha de ingreso al portal. En el portal puede dar seguimiento al periodo de pago en la columna fecha de vencimiento. Una vez cumplida esta fecha, el documento se programará para pago en la siguiente propuesta (las propuestas de pagos se ejecutan la última semana del mes).<br />" +  
    		"■Es indispensable que ingrese todos sus complementos de pago al portal posterior a que reciba el pago de sus documentos. Debe asegurarse de contar con la carga completa a más tardar el 5to día natural del siguiente mes, esto para que pueda continuar ingresando sus facturas a cobro. <br /> " +  
    		"■Si requiere cancelar o sustituir cualquier documento es necesario que lo valide con el área de cuentas por pagar, previo a realizar cualquier cambio.  <br /> " + 
    		"■Para cualquier duda o seguimiento puede contactar a su comprador para que lo canalice con su asociado de cuentas por pagar. <br /><br /><br /> " +
    		ETHIC_CONTENT;
    		/*
    		"CÓDIGO DE CONDUCTA DE SEPASA ¿TIENES PREGUNTAS O ALGO QUE REPORTAR? <br />" +
    		"Todos los colaboradores tanto internos como externos de SEPASA, somos responsables de cumplir con el Código de Conducta de SEPASA y de reportar cualquier conducta que vaya en contra de este Código, así como de cumplir y reportar violaciones a leyes o disposiciones externas, políticas y procedimientos internos de SEPASA. Dichos reportes podrán realizarse a través de la Línea de Ética de SEPASA, donde serán canalizados e investigados por la Dirección de Cumplimiento, de manera confidencial y en su caso, anónimamente. <br /> " + 
    		"La Línea de Ética de SEPASA está disponible los 365 días del año, las 24 horas, en los siguientes medios de contacto: <br /> " + 
    		"Atención telefónica <br /> " + 
    		"800 999 0784 <br />" +
    		"Página web <br />" +
    		"https://www.tipsanonimos.com/eticaSEPASA <br />" +
    		"Correo electrónico <br />" +
    		"eticaSEPASA@tipsanonimos.com<br /><br />";
    		*/
    
	public static final String FILE_EXT_XML = "xml";	
	
	public static final String FILE_EXT_PDF = "pdf";
	
    public static final String FISCAL_DOC_APPROVED = "APROBADO";
    
    public static final String FISCAL_DOC_REJECTED = "RECHAZADO";
    
    public static final String FISCAL_DOC_OTHER = "OTHER";

    public static final String APPROVE_MAIL_SUBJECT_INVOICE = "SEPASA - Solicitud de Aprobación Factura. OC ";
    
    public static final String FISCAL_DOC_MAIL_SUBJECT_INVOICE = "SEPASA - Aprobación Factura sin Orden de Compra";
    
    public static final String FISCAL_DOC_MAIL_SUBJECT_NC = "SEPASA - Aprobación Nota de Credito sin Orden de Compra";
    
    public static final String FISCAL_DOC_MAIL_MSJ_INVOICE = "Su factura sin orden de compra con el UUID: ";
    
    public static final String FISCAL_DOC_MAIL_MSJ_NC = "Su Nota de Credito sin orden de compra Con el UUID: ";
    
    public static final String FISCAL_DOC_MAIL_MSJ_APPROVED = " en el Portal de Proveedores fue aprobada con exito.<br /><br />";
    
    public static final String FISCAL_DOC_MAIL_MSJ_REJECTED = " en el Portal de Proveedores fue rechazada con el siguiente motivo.  <br /><br />";
    
    public static final String FISCAL_DOC_MAIL_MSJ_INVOICE_SHOP_AREA = "Una factura sin orden de compra con el UUID: ";
    
    public static final String FISCAL_DOC_MAIL_MSJ_NC_SHOP_AREA = "Una Nota de Credito sin orden de compra con el UUID: ";
    
    public static final String FISCAL_DOC_MAIL_MSJ_SHOP_AREA_2 = " del proveedor ";
    
    public static final String FISCAL_DOC_MAIL_MSJ_SHOP_AREA_3 = " fue aprobada con exito, continuar con el proceso para envio a JDE <br /><br />";
    
    public static final String PROGRAM_ID_ZP0411Z1 = "ZP0411Z1";
    
    public static final String WORK_STN_ID_COBOWB04 = "COBOWB04";
    
    public static final String EXPLANATION_REMARK = "PORTAL DE PROVEEDORES";
    
    public static final String INVOICE_TAX0 = "MX0";
    
    public static final String INVOICE_TAX_RATE_TAX0 = "0.000000";
    
    public static final String CURRENCY_MODE_DOMESTIC = "D";
    
    public static final String CURRENCY_MODE_FOREIGN = "F";
    
    public static final String GL_OFFSET_DEFAULT = "100";
    
    public static final String GL_OFFSET_FOREIGN = "200";
    
    public static final String INV_FIRST_APPROVER = "FIRST_APPROVER";
    
    public static final String INV_SECOND_APPROVER = "SECOND_APPROVER";
    
    public static final String FIRST_STEP = "FIRST";
    
    public static final String SECOND_STEP = "SECOND";
    
    public static final String FINAL_STEP = "FINAL";
    
    public static final String LOCAL_TAX_ISH = "Impuesto Sobre Hospedaje";
    
    public static final String ORDER_TYPE_WITHOUT_RECEIPTS = "P0";
    
    public static final String CONCEPT_001 = "CNT";
    public static final String CONCEPT_002 = "Validation";
    public static final String CONCEPT_003 = "Maneuvers";
    public static final String CONCEPT_004 = "Deconsolidation";
    public static final String CONCEPT_005 = "RedManeuvers";
    public static final String CONCEPT_006 = "Fumigation";
    public static final String CONCEPT_007 = "Docking";
    public static final String CONCEPT_008 = "Storage";
    public static final String CONCEPT_009 = "Delays";
    public static final String CONCEPT_010 = "Dragging";
    public static final String CONCEPT_011 = "Permissions";
    public static final String CONCEPT_012 = "Duties";
    public static final String CONCEPT_013 = "Other1";
    public static final String CONCEPT_014 = "Other2";
    public static final String CONCEPT_015 = "Other3";

    public static final String CONCEPT_016 = "NoPECEAccount";
    public static final String CONCEPT_017 = "DTA";
    public static final String CONCEPT_018 = "IVA";
    public static final String CONCEPT_019 = "IGI";
    public static final String CONCEPT_020 = "PRV";
    public static final String CONCEPT_021 = "IVAPRV";
    public static final String CONCEPT_022 = "ManeuversNoF";
    public static final String CONCEPT_023 = "DeconsolidationNoF";
    public static final String CONCEPT_024 = "Other1NoF";
    public static final String CONCEPT_025 = "Other2NoF";
    public static final String CONCEPT_026 = "Other3NoF";

    public static final String CONCEPT_DESC_001 = "CNT";
    public static final String CONCEPT_DESC_002 = "Validación";
    public static final String CONCEPT_DESC_003 = "Maniobras";
    public static final String CONCEPT_DESC_004 = "Desconsolidación";
    public static final String CONCEPT_DESC_005 = "Maniobras en Rojo";
    public static final String CONCEPT_DESC_006 = "Fumigación";
    public static final String CONCEPT_DESC_007 = "Muellaje";
    public static final String CONCEPT_DESC_008 = "Almacenaje";
    public static final String CONCEPT_DESC_009 = "Demoras";
    public static final String CONCEPT_DESC_010 = "Arrastres";
    public static final String CONCEPT_DESC_011 = "Permisos";
    public static final String CONCEPT_DESC_012 = "Derechos";
    public static final String CONCEPT_DESC_013 = "Otros 1";
    public static final String CONCEPT_DESC_014 = "Otros 2";
    public static final String CONCEPT_DESC_015 = "Otros 3";
	
    public static final String CONCEPT_DESC_016 = "Impuestos no pagados con cuenta PECE";
    public static final String CONCEPT_DESC_017 = "DTA";
    public static final String CONCEPT_DESC_018 = "IVA";
    public static final String CONCEPT_DESC_019 = "IGI";
    public static final String CONCEPT_DESC_020 = "PRV";
    public static final String CONCEPT_DESC_021 = "IVA/PRV";
    public static final String CONCEPT_DESC_022 = "Maniobras (Sin Factura Fiscal)";
    public static final String CONCEPT_DESC_023 = "Desconsolidación (Sin Factura Fiscal)";
    public static final String CONCEPT_DESC_024 = "Otros 1 (Sin Factura Fiscal)";
    public static final String CONCEPT_DESC_025 = "Otros 2 (Sin Factura Fiscal)";
    public static final String CONCEPT_DESC_026 = "Otros 3 (Sin Factura Fiscal)";
    
	public static final String OUTSOURCING_APPROVAL_SUBJECT = "Portal de Proveedores: Aprobación de documentos de Outsourcing del proveedor _SUPPLIER_";
	public static final String OUTSOURCING_APPROVAL_MESSAGE = "Estimado Aprobador. <br /><br />El proveedor _SUPPLIER_ categorizado como OUTSOURCING, ha enviado la documentación que se anexa en este correo para su revisión y aprobación. <br /><br />Para APROBAR esta documentación y notificar al proveedor, haga click en el siguiente enlace:<br /><br /> _LINK_ <br /><br /><br />";
	public static final String OUTSOURCING_APPROVED_MESSAGE = "Estimado Proveedor de OutSourcing. <br /><br />Su documentación ha sido aceptada y a partir de este momento usted podrá utulizar el Portal de Proveedores sin mayor incoveniente. <br /><br />Nota Importante: De conformidad con las disposiciones en materia fiscal, en el momento en que cargue facturas en el Portal de Proveedores y éstas pertenezcan a Servicios de OutSourcing, el sistema le permitirá cargar de forma adicional los comprobantes correspondientes.<br /><br />";
	public static final String EMAIL_PORTAL_LINK_PUBLIC = "https://3.222.152.41/supplierWebPortalSepasa/public";
	
	public static final String LOG_SFTP_PROCESS = "SFTPPROCESS";
	public static final String MSG_PORTAL_INACTIVE = "Lo sentimos, el portal esta en mantenimiento,intentalo mas tarde";
	public static final String LOGGER_JEDWARS_RELOAD  = "ENVIO_JEDR_RELOAD";
	public static final String LOGGER_JEDWARS_ERROR  = "ERROR";
	public static final String LOGGER_JEDWARS_SEND  = "SEND";
	
	public static final String FISCALDOCUMENT_MODULE = "FISCALDOCUMENT";
	public static final String SALESORDER_MODULE = "SALESORDER";
	public static final String SUPPLIER_MODULE = "SUPPLIER";
	public static final String APPROVALSEARCH_MODULE = "APPROVALSEARCH";
	public static final String OUTSOURCING_MODULE = "OUTSOURCING";
	public static final String USERS_MODULE = "USERS";
	public static final String SAT_MODULE = "SAT";
	public static final String UDC_MODULE = "UDC";
	public static final String ANONYMOUS_USER = "anonymousUser";
	
	public static final String NEWREGISTER_SUBJECT = "Portal de Alta de Proveedores - SEPASA. Enlace para nuevo registro. No responder.";
	public static final String NEWREGISTER_RENEW_SUBJECT = "Portal de Alta de Proveedores - SEPASA. Renovación del enlace para nuevo registro. No responder.";
	public static final String NEWREGISTER_MESSAGE = "Estimado Proveedor. <br /><br />En SEPASA estamos en búsqueda de aliados comerciales capaces de alcanzar nuestros requerimientos en términos de innovaciónn, seguridad, calidad y servicio. <br /><br />Es un placer notificarle que ha sido seleccionado para ser parte de nuestros proveedores, a continuación, encontrará un nuevo enlace para su registro en nuestro portal.<br /><br />Considere las siguientes condiciones de uso:<br />1.Abra la URL utilizando copiar y pegar en los navegadores Chrome, Mozilla, Safari.<br />2.El enlace podrá ser utilizado por una sola ocasión y es intransferible.<br/>3. El enlace tendrá una vigencia de 3 días a partir de la recepción de este correo. Si el enlace vence, puede solicitar una renovación poniéndose en contacto con SEPASA.<br />4. Cualquier otro enlace enviado con anterioridad quedará desactivado.<br />5. En caso de existir alguna duda contactar a su representante de adquisicione.<br /><br />Para completar este proceso requerirás los siguientes documentos:<br /><br /><br /> <strong>Nacionales: Persona Física/Moral</strong> <br /><br /> 1. Constancia de situación fiscal (menor a 3 meses de antigüedad).<br />2. Comprobante de domicilio fiscal (menor a 3 meses de antigüedad).<br />3. Encabezado de Estado de Cuenta Bancario donde aparezca cuenta, clabe y banco (menor a 3 meses de antigüedad).<br> 4. Identificación oficial vigente.<br /> 5. Acta constitutiva.<br /><br /> <strong>Extranjeros</strong> <br /><br /> 1. Constancia fiscal de residencia<br /> 2. Carta instrucción de transferencia internacional<br /><br /> Enlace para su registro: <br /><br />";
	public static final String NEWREGISTER_RENEW_MESSAGE="Estimado Proveedor. <br /><br />En SEPASA estamos en búsqueda de aliados comerciales capaces de alcanzar nuestros requerimientos en términos de innovaciónn, seguridad, calidad y servicio. <br /><br />Es un placer notificarle que ha sido seleccionado para ser parte de nuestros proveedores, a continuación, encontrará un nuevo enlace para su registro en nuestro portal.<br /><br />Considere las siguientes condiciones de uso:<br />1.Abra la URL utilizando copiar y pegar en los navegadores Chrome, Mozilla, Safari.<br />2.El enlace podra ser utilizado por una sola ocasión y es intransferible.<br/>3. El enlace tendrá una vigencia de 3 días a partir de la recepción de este correo. Si el enlace vence, puede solicitar una renovación poniéndose en contacto con SEPASA.<br />4. Cualquier otro enlace enviado con anterioridad quedará desactivado.<br />5. En caso de existir alguna duda contactar a su representante de adquisicione.<br /><br />Para completar este proceso requerirás los siguientes documentos:<br /><br /><br /> <strong>Nacionales: Persona Física/Moral</strong> <br /><br /> 1. Constancia de situación fiscal (menor a 3 meses de antigüedad).<br />2. Comprobante de domicilio fiscal (menor a 3 meses de antigüedad).<br />3. Encabezado de Estado de Cuenta Bancario donde aparezca cuenta, clabe y banco (menor a 3 meses de antigüedad).<br> 4. Identificación oficial vigente.<br /> 5. Acta constitutiva.<br /><br /> <strong>Extranjeros</strong> <br /><br /> 1. Constancia fiscal de residencia<br /> 2. Carta instrucción de transferencia internacional<br /><br /> Enlace para su registro: <br /><br />";
	
	public static final String EMAIL_NOFOUND_APP_SUBJECT = "SEPASA - Solicitud de aprobaci�n de proveedor.";
    
    public static final String EMAIL_NOFOUND_APP_CONTENT = "Estimado Usuario:<br />La solicitud con n�mero _NUMTICKET_ no pudo ser asignada.<br />Favor de revisar la informaci�n en el portal _URL_"
    														+ "<br /><br />---<br />Dear User:<br />The request with number _NUMTICKET_ could not be assigned.<br />Please review the information on the portal _URL_";

    public static final Map<String, Double> FISCAL_DOC_CRRENCY_LIMIT =new HashMap<String, Double>();
	public static final String ACCOUNT_NUMBER  = "358.131002.02";
    public static final String FISCAL_DOC_MAIL_SUBJECT_FAC_SN_ORDER = "SEPASA - Solicitud Factura sin Orden de Compra";
    
    public static final String FISCAL_DOC_MAIL_MSJ_FAC_SN_ORDER_COMP = "Estimado comprador <br /> Tiene una solicitud de aprobación de una factura sin orden de compra en el Portal de Proveedores.  El UUID de la factura para aprobar es: %s del proveedor %s<br /><br /> %s" ;
    
    public static final String FISCAL_DOC_MAIL_MSJ_FAC_SN_ORDER_PROV = "Estimado proveedor <br /> Su factura con uuid  %s ha sido cargada y sometida a revisión. En cuanto sea aprobada usted sera notificado <br /><br />  %s";
    
    public static final String FISCAL_DOC_MAIL_SUBJECT_FAC = "SEPASA - Solicitud Factura";
    
    public static final String FISCAL_DOC_MAIL_MSJ_FAC_COMP = "Estimado usuario:<br />Tiene una solicitud de aprobación de una factura en el Portal de Proveedores.  El Folio de la factura para aprobar es: %s con UUID %s del proveedor %s<br /><br /> %s" ;
    
    public static final String FISCAL_DOC_MAIL_MSJ_FAC_PROV = "Estimado proveedor:<br />Su factura con folio %s y UUID %s ha sido recibida y será sometida a revisión. En cuanto su factura sea aprobada le estará llegando una nueva notificación de dicha aprobación.<br /><br />%s"
    									+ "<br /><br />---<br />Dear supplier:<br />Your invoice with folio %s and UUID %s has been received and will be reviewed. As soon as your invoice is approved, you will receive a new notification of said approval.<br /><br />%s";
    
    public static final String FISCAL_DOC_MAIL_MSJ_INVOICE_FOLIO = "Estimado proveedor:<br />Su factura con folio %s y UUID %s fue aprobada y será pagada de acuerdo a sus condiciones de crédito.<br />Fecha Estimada de Pago (Día-Mes-Año): %s<br /><br />Aprobador: %s<br /><br />%s"
			+ "<br /><br />---<br />Dear supplier:<br />Your invoice with folio %s and UUID %s was approved and will be paid according to your credit conditions.<br />Estimated Payment Date (Day-Month-Year): %s<br /><br />Approver: %s<br /><br />%s";
    
    public static final String EMAIL_INVOICE_REJECTED_NOTIF_FOLIO = "Estimado proveedor:<br />Su factura ha sido RECHAZADA. El folio de la factura rechazada es: %s con uuid: %s<br /><br />Rechaza: %s<br />Notas:<br />%s<br /><br />%s"
			 + "<br /><br />---<br />Dear supplier:<br />Your invoice has been REJECTED. The folio of the rejected invoice is: %s with uuid: %s<br /><br />Rejects: %s<br />Notes:<br />%s<br /><br />%s";
    
	public static final String EMAIL_PAYMENT_COMPL_NOTIF_CONTENT =  "<b>ENGLISH TRANSLATION CAN BE FOUND AT THE BOTTOM</b> <br><br><br>"
			+ "Estimado proveedor <br><br> Le comentamos que las siguientes transacciones no cuentan con su complemento de pago.\r\n" +  
			" <br/><br> _INVOICES_ES_<br /><br />"
			+ "Es importante recordar que debe de cargar los complementos a la brevedad posible, <br />de lo contrario el portal no le permitirá ingresar nuevas facturas para pago."
			+ "<br /><br />---<br />Dear supplier <br><br>Please find attached the transactions pending to receive your payment complement.\r\n" + 
			"<br/><br> _INVOICES_EN_ <br/><br>Be aware that you mut complete this process as soon as possible, <br>otherwise you will not be able to issue new invoices for payment.";

	public static final String EMAIL_PAYMENT_COMPL_NOTIF = "SEPASA - Complemento(s) de Pago Pendiente de Carga/Payment Complement Notice.";
	
	public static final String EMAIL_FAC_NOTIF_CONTENT =  "<b>ENGLISH TRANSLATION CAN BE FOUND AT THE BOTTOM</b> <br><br><br>"
			+ "Estimado proveedor <b>_supplier_</b><br><br> Usted tiene pendiente de cargar su(s) factura(s) para el(los) siguiente(s) recibo(s). <br/><br> _INVOICES_ES_<br /><br /> "
			+ "Le recordamos que los dias de vencimiento inician en el momento en que la factura<br /> es aceptada en nuestro portal.<br /><br />Favor de subir su(s) factura(s) a nuestro portal a la brevedad.<br /><br />---<br />Dear supplier <b>_supplier_</b><br><br> Be aware that your payment has not been scheduled as we have not received your invoice yet. <br/>"
			+ "Please uploadyour invoice(s) as soon as possible in our portal. <br/><br> _INVOICES_EN_ <br/><br>"
			+ "It is important to notice that your payment terms begin to count as soon as your invoice(s)<br /> is (are) acepted in our portal.<br />" + 
			"<br/><br>Please upload your invoice(s) to our portal as soon as possible.";

	public static final String EMAI_FAC_NOTIF = "SEPASA - Factura(s) Pendiente(s) de cargar/Invoice(s) Pending Upload";
	 
	public static String truncate(String value, int places) {
		return new BigDecimal(value)
		    .setScale(places, RoundingMode.DOWN)
		    .stripTrailingZeros()
		    .toString();
	}
	
	public static String round(double value) {
		double roundOff = Math.round(value * 100.0) / 100.0;
		return String.valueOf(roundOff);
	}
	
	@PostConstruct
	public void init() {
			statusMap.put(STATUS.STATUS_OC_REJECTED, "OC RECHAZADA");
			statusMap.put(STATUS.STATUS_OC_REQUESTED, "OC SOLICITADA");
	        statusMap.put(STATUS.STATUS_OC_RECEIVED, "OC RECIBIDA");
	        statusMap.put(STATUS.STATUS_OC_APPROVED, "OC APROBADA");
	        statusMap.put(STATUS.STATUS_OC_SENT, "OC ENVIADA");
	        statusMap.put(STATUS.STATUS_OC_CLOSED, "OC CERRADA");
	        statusMap.put(STATUS.STATUS_OC_INVOICED, "OC FACTURADA");
	        statusMap.put(STATUS.STATUS_OC_PROCESSED, "OC PROCESADA");
	        statusMap.put(STATUS.STATUS_OC_PAID, "OC PAGADA");
	        statusMap.put(STATUS.STATUS_OC_CANCEL, "OC CANCELADA");
	        
	        FISCAL_DOC_CRRENCY_LIMIT.put("MXN_UPLIMIT", 20000.0);
	        FISCAL_DOC_CRRENCY_LIMIT.put("MXN_DOWNLIMIT", 0.0);
	        
	        FISCAL_DOC_CRRENCY_LIMIT.put("USD_UPLIMIT", 100.0);
	        FISCAL_DOC_CRRENCY_LIMIT.put("USD_DOWNLIMIT", 0.0);
	}
	 
}
