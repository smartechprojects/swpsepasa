package com.eurest.supplier.util;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;

import com.eurest.supplier.dao.NonComplianceSupplierDao;
import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.model.NonComplianceSupplier;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.service.EmailServiceAsync;

public class InsertBatchRecords implements Runnable {
	
	List<NonComplianceSupplier> list;
	NonComplianceSupplierDao nonComplianceSupplierDao;
	UDCDao udcDao;
	SupplierDao supplierDao;
	JavaMailSender mailSenderObj;
	StringUtils stringUtils;
	
	private Logger log4j = Logger.getLogger(InsertBatchRecords.class);
	
	public InsertBatchRecords(List<NonComplianceSupplier> list, NonComplianceSupplierDao nonComplianceSupplierDao, UDCDao udcDao, SupplierDao supplierDao, JavaMailSender mailSenderObj, StringUtils stringUtils) {
        this.list = list;
        this.nonComplianceSupplierDao = nonComplianceSupplierDao;
        this.udcDao = udcDao;
        this.supplierDao = supplierDao;
        this.mailSenderObj = mailSenderObj;
        this.stringUtils = stringUtils;
    }
	
	public void run() {
		if(list.size() > 0) {
			try {
				log4j.info("Deleting Non Compliance....");
				nonComplianceSupplierDao.deleteAll();
				log4j.info("Inserting Non Compliance....");
				nonComplianceSupplierDao.saveSuppliers(list);
				log4j.info("Inserted Non Compliance....");
				if(list != null && !list.isEmpty()) {
					for(NonComplianceSupplier o : list) {
						List<Supplier> sups = this.supplierDao.searchByRfc(o.getTaxId());
						try {
						    if (sups != null && !sups.isEmpty() && (
								      o.getRefDate1().contains("Definitivo") || 
								      o.getRefDate1().contains("Presunto") || 
								      o.getRefDate1().contains("Desvirtuado") || 
								      o.getRefDate2().contains("Definitivo") || 
								      o.getRefDate2().contains("Presunto") || 
								      o.getRefDate2().contains("Desvirtuado") || 
								      o.getStatus().contains("Definitivo") || 
								      o.getStatus().contains("Presunto") || 
								      o.getStatus().contains("Desvirtuado"))) {
								    	
								  	String emailAlert = "";
								  	UDC udcSupportMail = this.udcDao.searchBySystemAndKey("NONCOMPLIANCE", "SUPPORTMAIL");
								  	if(udcSupportMail != null && !udcSupportMail.getStrValue1().trim().isEmpty()) {
								  		emailAlert = udcSupportMail.getStrValue1().trim();
								  	}
							        EmailServiceAsync emailAsyncPur = new EmailServiceAsync();
							        emailAsyncPur.setProperties(AppConstants.EMAIL_NON_COMPLIANCE_SUBJECT,
							        		this.stringUtils.prepareEmailContent(AppConstants.EMAIL_NON_COMPLIANCE_CONTENT
							        		.replace("_ADDRESS_", sups.get(0).getAddresNumber())
							        		.replace("_NAME_", sups.get(0).getRazonSocial())
							        		.replace("_RFC_", sups.get(0).getRfc()))
							        		, emailAlert);
							        emailAsyncPur.setMailSender(this.mailSenderObj);
							        Thread emailThreadPur = new Thread(emailAsyncPur);
							        emailThreadPur.start();
						    }
						} catch (Exception e) {
							log4j.error("Exception" , e);
							e.printStackTrace();
						}
					}
				}
				log4j.info("Notification sended to Non Compliance....");
			}catch(Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
			}
		}
    }

}
