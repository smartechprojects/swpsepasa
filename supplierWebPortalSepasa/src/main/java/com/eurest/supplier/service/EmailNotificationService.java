package com.eurest.supplier.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dto.SupplierDTO;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.UserDocument;

@Service("emailNotificationService")
public class EmailNotificationService {
	  
	  @Autowired
	  EmailService emailService;
	  
	  @Autowired
	  SupplierService supplierService;
	  
	  @Autowired
	  DocumentsService documentsService;
	  
	  @Autowired
	  UdcService udcService;
	  
	  public static final String EMAIL_EOYNOTIF_SUBJECT = "SEPASA - Notificación de Cierre Anual / End of year closing process. No Responder/Do not response.";
	  public static final String EMAIL_ACCEPT_SUPPLIER_NOTIFICATION = "Estimado proveedor: <br /> <br /> A continuación encontrará información importante sobre el cierre anual. Por favor lea atentamente los documentos que se anexan en éste correo.<br /><br />Dear supplier: <br /> <br /> Below you will find important information about the financial annual closing process. Please read carefully the documents that are attached to this email. <br /> <br />";
	  
	  Logger log4j = Logger.getLogger(EmailNotificationService.class);

  	  //@Scheduled(fixedDelay = 4200000, initialDelay = 15000)
	  //@Scheduled(cron = "0 30 5 * * ?")
	  //@Scheduled(cron="0 5 11 22-25 11 ?") //Ejecuta del 19 al 29 de noviembre a las 10:25
	  public void endOfYearSupplierNotification() {
			
			UDC udcEoyNotif = udcService.searchBySystemAndKey("EOY", "SUPNOTIF");
			if(udcEoyNotif != null) {
				if(!"".equals(udcEoyNotif.getStrValue1())) {
					if("TRUE".equals(udcEoyNotif.getStrValue1())) {
						Map<String, InputStreamSource> isr = new HashMap<String, InputStreamSource>();

						try {
							List<UserDocument> docs = documentsService.searchCriteriaByType("EOYNOTIF");
						    if(docs != null && docs.size() > 0) {
						    	for(UserDocument doc : docs) {
						    		InputStream myInputStream = new ByteArrayInputStream(doc.getContent()); 
						    		InputStreamSource fileStreamSource = new ByteArrayResource(IOUtils.toByteArray(myInputStream));
						    		isr.put(doc.getName().trim(), fileStreamSource);
						    	}
						    }					    
						}catch(Exception e) {
							log4j.error("Exception" , e);
							e.printStackTrace();
						}
						
						List<SupplierDTO> supDtoList = supplierService.getActiveList(); // Max suppliers = 10,000
						if (!supDtoList.isEmpty()) {
							for(SupplierDTO s : supDtoList) {
								if(s.getEmail() != null && !"".equals(s.getEmail())) {
									emailService.sendEmailAttachMultiPart(EMAIL_EOYNOTIF_SUBJECT, EMAIL_ACCEPT_SUPPLIER_NOTIFICATION, s.getEmail(), isr);
								}
							}

						}
						udcEoyNotif.setStrValue1("FALSE");
						udcService.update(udcEoyNotif, new Date(), "");
					}
				}
			}	
		}

}
