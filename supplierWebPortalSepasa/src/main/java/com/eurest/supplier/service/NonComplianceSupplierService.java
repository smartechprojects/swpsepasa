package com.eurest.supplier.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dao.NonComplianceSupplierDao;
import com.eurest.supplier.dao.SupplierDao;
import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.model.NonComplianceSupplier;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.InsertBatchRecords;
import com.eurest.supplier.util.StringUtils;

@Service("nonComplianceSupplierService")
public class NonComplianceSupplierService {
	
	@Autowired
	NonComplianceSupplierDao nonComplianceSupplierDao;
	
	@Autowired
	SupplierDao supplierDao;
	
	@Autowired
	UDCDao udcDao;
	
	@Autowired
	JavaMailSender mailSenderObj;
	
	@Autowired
	StringUtils stringUtils;
	
	private Logger log4j = Logger.getLogger(NonComplianceSupplierService.class);
	
	public NonComplianceSupplier getNonComplianceSupplierById(int id) {
		return nonComplianceSupplierDao.getNonComplianceSupplierById(id);
	}
	
	public List<NonComplianceSupplier> getList(int start,int limit) {
		return nonComplianceSupplierDao.getList(start, limit);
	}
			
	public List<NonComplianceSupplier> searchByCriteria(String query,
			                                      int start,
			                                      int limit) {
		return nonComplianceSupplierDao.searchByCriteria(query, start, limit);
	}
	
	public NonComplianceSupplier searchByTaxId(String query,
            int start,
            int limit) {
		return nonComplianceSupplierDao.searchByTaxId(query, start, limit);
	}
	
	public long searchByCriteriaTotalRecords(String query) {
		return nonComplianceSupplierDao.searchByCriteriaTotalRecords(query);
	}
	
	public void saveSuppliers(List<NonComplianceSupplier> list) {
		nonComplianceSupplierDao.saveSuppliers(list);
	}
	
	public void deleteAll() {
		nonComplianceSupplierDao.deleteAll();
	}
	
	public int getTotalRecords(){
		return nonComplianceSupplierDao.getTotalRecords();
	}
	
	@Scheduled(cron = "0 0 21 * * SAT")
	//@Scheduled(fixedDelay = 420000, initialDelay = 3000)
	public void getNonComplianceSuppliers() {
		try {
			
			
			System.out.println("Deleting Non Compliance....");
			nonComplianceSupplierDao.deleteAll();
			
			URL urlCSV = new URL(AppConstants.SAT_NONCOMPLANCE_URL);
			//URLConnection urlConn = urlCSV.openConnection();
			InputStreamReader inputCSV = new InputStreamReader(urlCSV.openStream(), StandardCharsets.ISO_8859_1);
			BufferedReader br = new BufferedReader(inputCSV);
			String line;
			
			List<NonComplianceSupplier> list = new ArrayList<NonComplianceSupplier>();
			String updDate = "";
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
					String[] values = line.split(",", -1);
					if(values.length >= 18) {  // El reporte contiene solamente 18 columnas
						
						/*byte[] b = line.getBytes(StandardCharsets.UTF_8);
						//byte[] b = values[2].getBytes(StandardCharsets.UTF_8);
						String name = new String(b, StandardCharsets.UTF_8);
						values = name.split(",", -1);*/
						
						if(!"".equals(values[1]) && values[1].length()>3) {
							NonComplianceSupplier o = new NonComplianceSupplier();
							
							o.setLastUpdate(updDate);
							o.setLineNumber(values[0]);
							o.setTaxId(values[1]);
							o.setRefDate2("69-B");
							
							
							if(values[3].contains(".")) {
								
								String razonS = values[2]+values[3];
								if(razonS.length()>254) { 
									o.setSupplierName(razonS.substring(0, 254));
								}else o.setSupplierName(razonS);
								
								o.setStatus(values[4].toUpperCase());
								o.setRefDate1(values[6]);
							}else {
								o.setSupplierName(values[2]);
								o.setStatus(values[3].toUpperCase());
								o.setRefDate1(values[5]);
							}
							
							if(o.getSupplierName().contains("/")) {
								o.setSupplierName(o.getSupplierName().substring(0,o.getSupplierName().indexOf("/")));
							}
							
							list.add(o);
						}
						
						/*o.setSupplierName(values[2]);
						o.setStatus(values[3]);
						o.setRefDate1(values[4]);
						o.setRefDate2(values[5]);
						o.setRefDate3(values[6]);
						o.setRefDate4(values[7]);
						o.setRefDate5(values[8]);
						o.setRefDate6(values[9]);
						o.setRefDate7(values[10]);
						o.setRefDate8(values[11]);
						o.setRefDate9(values[12]);
						o.setRefDate10(values[13]);
						o.setRefDate11(values[14]);
						o.setRefDate12(values[15]);
						o.setRefDate13(values[16]);
						o.setRefDate14(values[17]);*/
						
					}else {
						if("".equals(updDate)) {
							updDate = values[0];
						}
						
					}
				
            }
			br.close();
			
			
			if(list.size() > 0) {
				this.InsertBatchRecords(list);
				/*InsertBatchRecords c = new InsertBatchRecords(list, nonComplianceSupplierDao, udcDao, supplierDao, mailSenderObj, stringUtils);
		        Thread t = new Thread(c);
		        t.start();*/
			}
			
			//Exigibles
			urlCSV = new URL(AppConstants.SAT_NONCOMPLANCE_EXIGIBLES_URL);
			//urlConn = urlCSV.openConnection();
			inputCSV = new InputStreamReader(urlCSV.openStream(), StandardCharsets.ISO_8859_1);
			br = new BufferedReader(inputCSV);
			
			
			list = new ArrayList<NonComplianceSupplier>();
			updDate = "";
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
					String[] values = line.split(",", -1);
					if(values.length >= 6) {
						if(!"RFC".equals(values[0])) {  // El reporte contiene solamente 6 columnas
							
							/*byte[] b = line.getBytes(StandardCharsets.UTF_8);
							//byte[] b = values[2].getBytes(StandardCharsets.UTF_8);
							String name = new String(b, StandardCharsets.UTF_8);
							values = name.split(",", -1);*/
							
							NonComplianceSupplier o = new NonComplianceSupplier();
							o.setLastUpdate(updDate);
							o.setTaxId(values[0]);
							o.setRefDate2("69");
							
							if(values[2].length()>1) {
								
								String razonS = values[1]+values[2];
								if(razonS.length()>254) {
									o.setSupplierName(razonS.substring(0, 254));
								}else o.setSupplierName(razonS);
								
								o.setStatus(values[4]);
								o.setRefDate1(values[5]);
							}else {
								o.setSupplierName(values[1]);
								o.setStatus(values[3]);
								o.setRefDate1(values[4]);
							}
							
							/*o.setSupplierName(values[1]);
							o.setStatus(values[3]);
							o.setRefDate2(values[4]);
							o.setRefDate4(values[4]);
							o.setRefDate1("");
							o.setRefDate2("");*/
							list.add(o);
						}
					}
				
            }
			br.close();
			
			if(list.size() > 0) {
				this.InsertBatchRecords(list);
				/*InsertBatchRecords c = new InsertBatchRecords(list, nonComplianceSupplierDao, udcDao, supplierDao, mailSenderObj, stringUtils);
		        Thread t = new Thread(c);
		        t.start();*/
			}
			
			//SENTENCIAS
			urlCSV = new URL(AppConstants.SAT_NONCOMPLANCE_SENTENCIAS_URL);
			//urlConn = urlCSV.openConnection();
			inputCSV = new InputStreamReader(urlCSV.openStream(), StandardCharsets.ISO_8859_1);
			br = new BufferedReader(inputCSV);
			
			
			list = new ArrayList<NonComplianceSupplier>();
			updDate = "";
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
					String[] values = line.split(",", -1);
					
					if(values.length >= 6) {
						if(!"RFC".equals(values[0])) {  // El reporte contiene solamente 6 columnas
							
							/*byte[] b = line.getBytes(StandardCharsets.UTF_8);
							//byte[] b = values[2].getBytes(StandardCharsets.UTF_8);
							String name = new String(b, StandardCharsets.UTF_8);
							values = name.split(",", -1);*/
							
							NonComplianceSupplier o = new NonComplianceSupplier();
							o.setLastUpdate(updDate);
							o.setTaxId(values[0]);
							o.setRefDate2("69");
							
							if(values[2].length()>1) {
								
								String razonS = values[1]+values[2];
								if(razonS.length()>254) {
									o.setSupplierName(razonS.substring(0, 254));
								}else o.setSupplierName(razonS);
								
								o.setStatus(values[4]);
								o.setRefDate1(values[5]);
							}else {
								o.setSupplierName(values[1]);
								o.setStatus(values[3]);
								o.setRefDate1(values[4]);
							}
							
							/*o.setSupplierName(values[1]);
							o.setStatus(values[3]);
							o.setRefDate2(values[4]);
							o.setRefDate4(values[4]);
							o.setRefDate1("");
							o.setRefDate2("");*/
							list.add(o);
						}
					}
				
            }
			br.close();
			
			if(list.size() > 0) {
				this.InsertBatchRecords(list);
				/*InsertBatchRecords c = new InsertBatchRecords(list, nonComplianceSupplierDao, udcDao, supplierDao, mailSenderObj, stringUtils);
		        Thread t = new Thread(c);
		        t.start();*/
			}
			
			//NO LOCALIZADOS
			urlCSV = new URL(AppConstants.SAT_NONCOMPLANCE_NO_LOCALIZADOS_URL);
			//urlConn = urlCSV.openConnection();
			inputCSV = new InputStreamReader(urlCSV.openStream(), StandardCharsets.ISO_8859_1);
			br = new BufferedReader(inputCSV);
			
			
			list = new ArrayList<NonComplianceSupplier>();
			updDate = "";
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
					String[] values = line.split(",", -1);
					
					if(values.length >= 6) {
						if(!"RFC".equals(values[0])) {  // El reporte contiene solamente 6 columnas
							
							/*byte[] b = line.getBytes(StandardCharsets.UTF_8);
							//byte[] b = values[2].getBytes(StandardCharsets.UTF_8);
							String name = new String(b, StandardCharsets.UTF_8);
							values = name.split(",", -1);*/
							
							NonComplianceSupplier o = new NonComplianceSupplier();
							o.setLastUpdate(updDate);
							o.setTaxId(values[0]);
							o.setRefDate2("69");
							
							if(values[2].length()>1) {
								
								String razonS = values[1]+values[2];
								if(razonS.length()>254) {
									o.setSupplierName(razonS.substring(0, 254));
								}else o.setSupplierName(razonS);
								
								o.setStatus(values[4]);
								o.setRefDate1(values[5]);
							}else {
								o.setSupplierName(values[1]);
								o.setStatus(values[3]);
								o.setRefDate1(values[4]);
							}
							
							/*o.setSupplierName(values[1]);
							o.setStatus(values[3]);
							o.setRefDate2(values[4]);
							o.setRefDate4(values[4]);
							o.setRefDate1("");
							o.setRefDate2("");*/
							list.add(o);
						}
					}
				
            }
			br.close();
			
			if(list.size() > 0) {
				this.InsertBatchRecords(list);
				/*InsertBatchRecords c = new InsertBatchRecords(list, nonComplianceSupplierDao, udcDao, supplierDao, mailSenderObj, stringUtils);
		        Thread t = new Thread(c);
		        t.start();*/
			}
			
			//FIRMES
			urlCSV = new URL(AppConstants.SAT_NONCOMPLANCE_FIRMES_URL);
			//urlConn = urlCSV.openConnection();
			inputCSV = new InputStreamReader(urlCSV.openStream(), StandardCharsets.ISO_8859_1);
			br = new BufferedReader(inputCSV);
			
			
			list = new ArrayList<NonComplianceSupplier>();
			updDate = "";
			while ((line = br.readLine()) != null) {
				line = line.replace("\"", "");
					String[] values = line.split(",", -1);
					if(values.length >= 6) {
						if(!"RFC".equals(values[0])) {  // El reporte contiene solamente 6 columnas
							
							/*byte[] b = line.getBytes(StandardCharsets.UTF_8);
							//byte[] b = values[2].getBytes(StandardCharsets.UTF_8);
							String name = new String(b, StandardCharsets.UTF_8);
							values = name.split(",", -1);*/
							
							NonComplianceSupplier o = new NonComplianceSupplier();
							o.setLastUpdate(updDate);
							o.setTaxId(values[0]);
							o.setRefDate2("69");
							
							if(values[2].length()>1) {
								
								String razonS = values[1]+values[2];
								if(razonS.length()>254) {
									o.setSupplierName(razonS.substring(0, 254));
								}else o.setSupplierName(razonS);
								
								o.setStatus(values[4]);
								o.setRefDate1(values[5]);
							}else {
								o.setSupplierName(values[1]);
								o.setStatus(values[3]);
								o.setRefDate1(values[4]);
							}
							
							/*o.setSupplierName(values[1]);
							o.setStatus(values[3]);
							o.setRefDate2(values[4]);
							o.setRefDate4(values[4]);
							o.setRefDate1("");
							o.setRefDate2("");*/
							list.add(o);
						}
					}
				
            }
			br.close();
			
			if(list.size() > 0) {
				this.InsertBatchRecords(list);
				/*InsertBatchRecords c = new InsertBatchRecords(list, nonComplianceSupplierDao, udcDao, supplierDao, mailSenderObj, stringUtils);
		        Thread t = new Thread(c);
		        t.start();*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void InsertBatchRecords (List<NonComplianceSupplier> list) {

		if(list.size() > 0) {
			try {
				//log4j.info("Deleting Non Compliance....");
				//nonComplianceSupplierDao.deleteAll();
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
