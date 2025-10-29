package com.eurest.supplier.service;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.dto.AddressBookLayot;
import com.eurest.supplier.dto.IntegerListDTO;
import com.eurest.supplier.dto.InvoiceRequestDTO;
import com.eurest.supplier.dto.OrderGridWrapper;
import com.eurest.supplier.dto.PurchaseOrderDTO;
import com.eurest.supplier.dto.PurchaseOrderGridDTO;
import com.eurest.supplier.dto.SupplierDTO;
import com.eurest.supplier.dto.UserDocumentDTO;
import com.eurest.supplier.edi.BatchJournalDTO;
import com.eurest.supplier.edi.ReceivingAdviceHeaderDTO;
import com.eurest.supplier.edi.SupplierJdeDTO;
import com.eurest.supplier.edi.VoucherHeaderDTO;
import com.eurest.supplier.model.ExchangeRate;
import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.LogDataJEdwars;
import com.eurest.supplier.model.PurchaseOrder;
import com.eurest.supplier.model.PurchaseOrderDetail;
import com.eurest.supplier.model.PurchaseOrderPayment;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.Tolerances;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.JdeJavaJulianDateTools;
import com.eurest.supplier.util.LoggerJEdwars;
import com.eurest.supplier.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

@Component
public class JDERestService {
  @Autowired
  PurchaseOrderService purchaseOrderService;
  
  @Autowired
  TolerancesService tolerancesService;
  
  @Autowired
  NextNumberService nextNumberService;
  
  @Autowired
  SupplierService supplierService;
  
  @Autowired
  UsersService usersService;
  
  @Autowired
  EmailService emailService;
  
  @Autowired
  UdcService udcService;
  
  @Autowired
  StringUtils stringUtils;
  
  @Autowired
  UDCDao udcDao;
  
  @Autowired
  DocumentsService documentsService;
  
  @Autowired
  FiscalDocumentService fiscalDocumentService;
  
  @Autowired
  ExchangeRateService exchangeRateService;
  
  @Autowired
  LoggerJEdwars loggerJEdwars;
  
  String orderDate;
  String lttr;
  String nxtr;
  
  @Autowired
  private JavaMailSender mailSenderObj;
  
  private Logger log4j = Logger.getLogger(JDERestService.class);

//	@Scheduled(fixedDelay = 4200000, initialDelay = 3000)
//	@Scheduled(cron = "0 30 2 * * ?")
	public void getOrderPayments() {
		try {			
			for(int i=0; i<=20; i++) {
				int start = i*500;

				List<FiscalDocuments> fdList = fiscalDocumentService.getPaymentPendingFiscalDocuments(start, 500);
				List<FiscalDocuments> fdUpdateList = new ArrayList<FiscalDocuments>();
				
				if (fdList != null) {
					if (fdList.size() > 0) {
						IntegerListDTO idto = new IntegerListDTO();
						List<String> uuidList = new ArrayList<String>();
						for(FiscalDocuments fd : fdList) {
							
							String invNbr = "";
							String folio = "";
							if(fd.getFolio() != null && !"null".equals(fd.getFolio()) && !"NULL".equals(fd.getFolio()) ) {
								folio = fd.getFolio();
							}
							
							String serie = "";
							if(fd.getSerie() != null && !"null".equals(fd.getSerie()) && !"NULL".equals(fd.getSerie()) ) {
								serie = fd.getSerie();
							}
							
							invNbr = serie + folio;
							String tempUuid = String.format("%08d", fd.getOrderNumber()) + "_" + fd.getAddressNumber() + "_" + invNbr;
							if(!uuidList.contains(tempUuid)) {
								uuidList.add(tempUuid);
							}
							//uuidList.add(po.getUuid());
						}
						
						if(uuidList.size() > 0) {
							idto.setUuidList(uuidList);
							ObjectMapper jsonMapper = new ObjectMapper();
							String jsonInString = jsonMapper.writeValueAsString(idto);
							System.out.println("jsonInString:"+jsonInString);
							HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
							httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			 				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/payments";
							Map<String, String> params = new HashMap<String, String>();
							HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
							RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class, params);
							HttpStatus statusCode = responseEntity.getStatusCode();

							if (statusCode.value() == 200) {
								String body = responseEntity.getBody(); System.out.println("body:"+body);
								if (body != null) {
									ObjectMapper mapper = new ObjectMapper();
									Receipt[] response = mapper.readValue(body, Receipt[].class);
									List<Receipt> objList = Arrays.asList(response);
									
									if (!objList.isEmpty()) {
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
	 	 	 					        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
	 	 	 					        List<String> uuidAddList = new ArrayList<String>();
	 	 	 					        List<Receipt> recUpdateList = new ArrayList<Receipt>();
	 	 	 					        List<PurchaseOrder> poUpdateList = new ArrayList<PurchaseOrder>();
	 	 	 					     
	 	 	 					        for(Receipt o : objList) {//Lista de Pagos de JDE
		 	 	 							for(FiscalDocuments fd : fdList) {
		 	 	 								
		 	 	 								String invNbr = "";
		 	 	 								String folio = "";
		 	 	 								if(fd.getFolio() != null && !"null".equals(fd.getFolio()) && !"NULL".equals(fd.getFolio()) ) {
		 	 	 									folio = fd.getFolio();
		 	 	 								}
		 	 	 								
		 	 	 								String serie = "";
		 	 	 								if(fd.getSerie() != null && !"null".equals(fd.getSerie()) && !"NULL".equals(fd.getSerie()) ) {
		 	 	 									serie = fd.getSerie();
		 	 	 								}
		 	 	 								
		 	 	 								invNbr = serie + folio;
		 	 	 								
												if(o.getAddressNumber().equals(fd.getAddressNumber())
														&& o.getOrderNumber() == fd.getOrderNumber()
														&& invNbr.equalsIgnoreCase(o.getUuid())) {
													
													//Agrega registros de Documentos Fiscales
													fd.setPaymentAmount(o.getPaymentAmount());
													fd.setPaymentDate(o.getPaymentDate());
													fd.setPaymentReference(o.getPaymentReference());
													fd.setPaymentStatus(AppConstants.STATUS_GR_PAID);
													fd.setPaymentUploadDate(new Date());
													fd.setStatus(AppConstants.STATUS_PAID);
													fdUpdateList.add(fd);
													
													//Agrega registros de Ordenes de Compra y Recibos
													if(fd.getUuidFactura() != null && !fd.getUuidFactura().trim().isEmpty()) {
														String uuid = fd.getUuidFactura().trim();
														if(!uuidAddList.contains(uuid)) {
															uuidAddList.add(uuid);
															List<Receipt> rList = purchaseOrderService.getReceiptsByUUID(uuid);
															if(rList != null) {
																for(Receipt r : rList) {
																	r.setPaymentReference(o.getPaymentReference());
																	r.setPaymentAmount(o.getPaymentAmount());
																	r.setPaymentDate(o.getPaymentDate());
																	recUpdateList.add(r);
																}
															}
															
															PurchaseOrder p = purchaseOrderService.searchbyOrderUuid(uuid);
															if(p != null) {
																p.setPaymentUploadDate(new Date());
																p.setOrderStauts(AppConstants.STATUS_OC_PAID);
																p.setRelatedStatus(AppConstants.STATUS_COMPLETE);
																poUpdateList.add(p);
															}
														}
													}
													break;
												}
		 	 	 							}
	 	 	 					        }
	 	 	 					        
	 	 	 					        //Actualiza Ordenes de Compra, Recibos y Documentos Fiscales
										purchaseOrderService.updatePaymentReceiptsFD(fdUpdateList);
										purchaseOrderService.updatePaymentReceipts(recUpdateList);
										purchaseOrderService.updateMultiple(poUpdateList);
	 	 	 					        
										for (FiscalDocuments o : fdUpdateList) {
											Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
											if(s!=null) {
		 	 	 								String invNbr = "";
		 	 	 								String folio = "";
		 	 	 								if(o.getFolio() != null && !"null".equals(o.getFolio()) && !"NULL".equals(o.getFolio()) ) {
		 	 	 									folio = o.getFolio();
		 	 	 								}
		 	 	 								
		 	 	 								String serie = "";
		 	 	 								if(o.getSerie() != null && !"null".equals(o.getSerie()) && !"NULL".equals(o.getSerie()) ) {
		 	 	 									serie = o.getSerie();
		 	 	 								}
		 	 	 								
		 	 	 								invNbr = serie + folio;
		 	 	 								
			 	 								String emailRecipient = (s.getEmailSupplier());
			 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			 	 	 							String emailContent = AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF_CONTENT;
			 	 	 							emailContent = emailContent.replace("_VINV_", String.valueOf(invNbr));
												String strDate = dateFormat.format(o.getPaymentDate()); 
												emailContent = emailContent.replace("_PO_", String.valueOf(o.getOrderNumber()));
												emailContent = emailContent.replace("_DATE_", strDate);

			 	 	 							String currency = format.format(o.getPaymentAmount());
			 	 	 							emailContent = emailContent.replace("_AMOUNT_", currency);
			 	 	 							emailContent = emailContent.replace("_PID_", o.getPaymentReference());
			 	 	 							emailContent = emailContent.replace("_UUID_", o.getUuidFactura());

			 	 	 							emailAsyncSup.setProperties(
			 	 	 									AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF,
			 	 	 									stringUtils.prepareEmailContent(emailContent + AppConstants.EMAIL_PORTAL_LINK),emailRecipient);
			 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
			 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
			 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
			 	 	 							emailThreadSup.start();
			 								}											
										}

										log4j.info("Guardado:" + response.length);
									}
								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
	}
	
//  @Scheduled(fixedDelay = 4200000, initialDelay = 3000)
//	@Scheduled(cron = "0 30 2 * * ?")
	public void getOrderPaymentsOLD() {
		try {			
			for(int i=0; i<=20; i++) {
				int start = i*500;
				
				List<Receipt> poList = purchaseOrderService.getPaymentPendingReceipts(start, 500);
				List<Receipt> poUpdateList = new ArrayList<Receipt>();
				
				if (poList != null) {
					if (poList.size() > 0) {
						IntegerListDTO idto = new IntegerListDTO();
						List<String> uuidList = new ArrayList<String>();
						for(Receipt po : poList) {
							
							String invNbr = "";
							String folio = "";
							if(po.getFolio() != null && !"null".equals(po.getFolio()) && !"NULL".equals(po.getFolio()) ) {
								folio = po.getFolio();
							}
							
							String serie = "";
							if(po.getSerie() != null && !"null".equals(po.getSerie()) && !"NULL".equals(po.getSerie()) ) {
								serie = po.getSerie();
							}
							
							invNbr = serie + folio;
							String tempUuid = String.format("%08d", po.getOrderNumber()) + "_" + po.getAddressNumber() + "_" + invNbr;
							if(!uuidList.contains(tempUuid)) {
								uuidList.add(tempUuid);
							}
							//uuidList.add(po.getUuid());
						}
						
						if(uuidList.size() > 0) {
							idto.setUuidList(uuidList);
							ObjectMapper jsonMapper = new ObjectMapper();
							String jsonInString = jsonMapper.writeValueAsString(idto);
							System.out.println("jsonInString:"+jsonInString);
							HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
							httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			 				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/payments";
							Map<String, String> params = new HashMap<String, String>();
							HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
							RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class, params);
							HttpStatus statusCode = responseEntity.getStatusCode();

							if (statusCode.value() == 200) {
								String body = responseEntity.getBody(); System.out.println("body:"+body);
								if (body != null) {
									ObjectMapper mapper = new ObjectMapper();
									Receipt[] response = mapper.readValue(body, Receipt[].class);
									List<Receipt> objList = Arrays.asList(response);
									
									if (!objList.isEmpty()) {
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
	 	 	 					        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
	 	 	 					        List<String> uuidAddList = new ArrayList<String>();
	 	 	 					        List<FiscalDocuments> fdUpdateList = new ArrayList<FiscalDocuments>();
	 	 	 					        
	 	 	 					        for(Receipt o : objList) {
		 	 	 							for(Receipt po : poList) {
		 	 	 								
		 	 	 								String invNbr = "";
		 	 	 								String folio = "";
		 	 	 								if(po.getFolio() != null && !"null".equals(po.getFolio()) && !"NULL".equals(po.getFolio()) ) {
		 	 	 									folio = po.getFolio();
		 	 	 								}
		 	 	 								
		 	 	 								String serie = "";
		 	 	 								if(po.getSerie() != null && !"null".equals(po.getSerie()) && !"NULL".equals(po.getSerie()) ) {
		 	 	 									serie = po.getSerie();
		 	 	 								}
		 	 	 								
		 	 	 								invNbr = serie + folio;
		 	 	 								
												if(o.getAddressNumber().equals(po.getAddressNumber())
														&& o.getOrderNumber() == po.getOrderNumber()
														&& invNbr.toUpperCase().equals(o.getUuid())) {
													
													//Agrega registros de Receipt
													po.setPaymentReference(o.getPaymentReference());
													po.setPaymentAmount(o.getPaymentAmount());
													po.setPaymentDate(o.getPaymentDate());
													poUpdateList.add(po);
													
													//Agrega registros de Fiscal Documents
													if(po.getUuid() != null && !po.getUuid().trim().isEmpty()) {
														if(!uuidAddList.contains(po.getUuid())) {
															uuidAddList.add(po.getUuid());
															FiscalDocuments fd = fiscalDocumentService.getFiscalDocumentsByUuid(po.getUuid());
															if(fd != null) {
																fd.setPaymentAmount(o.getPaymentAmount());
																fd.setPaymentDate(o.getPaymentDate());
																fd.setPaymentReference(o.getPaymentReference());
																fd.setPaymentStatus(AppConstants.STATUS_GR_PAID);
																fd.setPaymentUploadDate(new Date());
																fd.setStatus(AppConstants.STATUS_PAID);
																fdUpdateList.add(fd);
															}
														}
													}
													break;
												}
		 	 	 							}
	 	 	 					        }
	 	 	 					        
	 	 	 					        //Actualiza Recibos y Documentos Fiscales
										purchaseOrderService.updatePaymentReceipts(poUpdateList);
										purchaseOrderService.updatePaymentReceiptsFD(fdUpdateList);
	 	 	 					        
										for (Receipt o : poUpdateList) {										
											Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
											if(s!=null) {
			 	 								String emailRecipient = (s.getEmailSupplier());
			 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			 	 	 							String emailContent = AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF_CONTENT;
			 	 	 							emailContent = emailContent.replace("_UUID_", o.getUuid());
			 	 	 							emailContent = emailContent.replace("_GR_", String.valueOf(o.getDocumentNumber()));
												String strDate = dateFormat.format(o.getPaymentDate()); 
												emailContent = emailContent.replace("_PO_", String.valueOf(o.getOrderNumber()));
												emailContent = emailContent.replace("_DATE_", strDate);

			 	 	 							String currency = format.format(o.getPaymentAmount());
			 	 	 							emailContent = emailContent.replace("_AMOUNT_", currency);
			 	 	 							emailContent = emailContent.replace("_PID_", o.getPaymentReference());

			 	 	 							emailAsyncSup.setProperties(
			 	 	 									AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF,
			 	 	 									stringUtils.prepareEmailContent(emailContent + AppConstants.EMAIL_PORTAL_LINK),emailRecipient);
			 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
			 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
			 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
			 	 	 							emailThreadSup.start();
			 								}											
										}

										log4j.info("Guardado:" + response.length);
									}
								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
	}

	
	  //@Scheduled(fixedDelay = 4200000, initialDelay = 30000)
	//	@Scheduled(cron = "0 40 5 * * ?")
	//  public Map<String,Object> getOrderPaymentsProvUO() {
			public void getOrderPaymentsProvUO() {
//		  gama pago de orden PUO
		  //test------------------------
			Map<String,Object> mapResponse = new HashMap<>();
			boolean errors = false;
			int op = 0;
			String msg = "";
			//test------------------------
		  
		  	try {
				
				for(int i=0; i<=10; i++) {
		 			int start = i*4;
		 			int limit = 4;
		 			List<FiscalDocuments> poList=purchaseOrderService.getPaymentPendingReceiptsSOC(start, limit);
					Map<String,FiscalDocuments> mapList = new HashMap<String,FiscalDocuments>();
					List<FiscalDocuments> poUpdateList = new ArrayList<FiscalDocuments>();
					
					if (poList != null) {
						if (poList.size() > 0) {
							IntegerListDTO idto = new IntegerListDTO();
							List<String> uuidList = new ArrayList<String>();
							for(FiscalDocuments po : poList) {
//								String invNbr = "";
//								String folio = "";
//								if(po.getFolio() != null && !"null".equals(po.getFolio()) && !"NULL".equals(po.getFolio()) ) {
//									folio = po.getFolio();
//								}
//								
//								String serie = "";
//								if(po.getSerie() != null && !"null".equals(po.getSerie()) && !"NULL".equals(po.getSerie()) ) {
//									serie = po.getSerie();
//								}
//								
//								invNbr = serie + folio;
////								String tempUuid = String.format("%08d", po.getOrderNumber()) + "_" + po.getAddressNumber() + "_" + invNbr;
//								
//								String tempUuid ="PO_"+ po.getAddressNumber() + "_" + invNbr;
								uuidList.add(po.getId()+"");
								mapList.put(po.getId()+"", po);
							}
							
							if(uuidList.size() > 0) {
								idto.setUuidList(uuidList);
								ObjectMapper jsonMapper = new ObjectMapper();
								String jsonInString = jsonMapper.writeValueAsString(idto);
		
								HttpHeaders httpHeaders = new HttpHeaders();
								httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
								httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				 				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/paymentsPUO";
								Map<String, String> params = new HashMap<String, String>();
								HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
								RestTemplate restTemplate = new RestTemplate();
								ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class, params);
								HttpStatus statusCode = responseEntity.getStatusCode();
		
								if (statusCode.value() == 200) {
									String body = responseEntity.getBody();
									if (body != null) {
										ObjectMapper mapper = new ObjectMapper();
										FiscalDocuments[] response = mapper.readValue(body, FiscalDocuments[].class);
										List<FiscalDocuments> objList = Arrays.asList(response);
										if (!objList.isEmpty()) {
		 
											DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
		 	 	 					        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
		 	 	 					        
			 	 	 					     for (FiscalDocuments o : objList) {
//													o.setUuidFactura(o.getUuidFactura().trim());
													o.setAddressNumber(o.getAddressNumber());
													
													for(FiscalDocuments p : poList) {
													
														if(p.getId()==o.getId()) {
															p.setUuidPago(o.getUuidPago());
															p.setPaymentTerms(o.getPaymentTerms());
															p.setPaymentUploadDate(o.getPaymentUploadDate());
															p.setStatus(AppConstants.STATUS_PAID);
															poUpdateList.add(p);
															break;
														}
													}
												}
			 	 	 					     	
												purchaseOrderService.updatePaymentReceiptsFD(poUpdateList);
			 	 	 					        
												for (FiscalDocuments o : poUpdateList) {										
													Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
													if(s!=null) {
					 	 								String emailRecipient = (s.getEmailSupplier());
					 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
					 	 	 							String emailContent = AppConstants.EMAIL_PAYMENT_NO_OC_NOTIF_CONTENT; // EMAIL_PAYMENT_RECEIPT_NOTIF_CONTENT_PUO;
					 	 	 							emailContent = emailContent.replace("_UUID_", o.getUuidPago());
//					 	 	 							emailContent = emailContent.replace("_GR_", String.valueOf(o.getAddressNumber()));
														String strDate = dateFormat.format(o.getPaymentUploadDate()); 
														emailContent = emailContent.replace("_DATE_", strDate);
	//	
					 	 	 							String currency = format.format(o.getAmount());
					 	 	 							emailContent = emailContent.replace("_AMOUNT_", currency);
					 	 	 							emailContent = emailContent.replace("_PID_", o.getPaymentTerms());
		
					 	 	 							emailAsyncSup.setProperties(
					 	 	 									AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF,
					 	 	 									stringUtils.prepareEmailContent(emailContent + AppConstants.EMAIL_PORTAL_LINK),emailRecipient);
					 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
					 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
					 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
					 	 	 							emailThreadSup.start();
					 								}
													
												}
												
												//test-------------------------------
					 							op = op + response.length;
					 							//test-------------------------------
												
					 							log4j.info("Guardado:" + response.length);
										}
									}

								}else {
				 					errors = true;
				 					msg = msg + "HttpResponse with Server - 4XX url:" + url;
				 				}
							}
						}
					}
				}
			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				errors = true;
	 			msg = msg + "CatchError: " + e.getMessage();
	 		}
	 		
	 		//test-----------------------------------
	 		if(errors) {
	 			mapResponse.put("errors", true);
	 			mapResponse.put("errorMsg", msg);
	 			mapResponse.put("size", op);
	 		}else {
	 			mapResponse.put("errors", false);
	 			mapResponse.put("errorMsg", "");
	 			mapResponse.put("size", op);
	 		}
	 		
//	 		return mapResponse;
	 		//test-----------------------------------
		}

			
	
  	//@Scheduled(fixedDelay = 4200000, initialDelay = 30000)
	/*@Scheduled(cron = "0 0 2 * * ?")
	public void getFiscalDocumentPayments() {
		try {			
			for(int i=0; i<=10; i++) {
				int start = i*500;
				
				List<FiscalDocuments> fdList = fiscalDocumentService.getFiscalDocuments("", AppConstants.STATUS_ACCEPT, "", "", start, 500,"");
				if (fdList != null) {
					if (fdList.size() > 0) {
						IntegerListDTO idto = new IntegerListDTO();
						List<String> uuidList = new ArrayList<String>();
						for(FiscalDocuments fd : fdList) {
							if(!uuidList.contains(fd.getUuidFactura())) {
								uuidList.add(fd.getUuidFactura());	
							}
						}
						
						if(uuidList.size() > 0) {
							idto.setUuidList(uuidList);
							ObjectMapper jsonMapper = new ObjectMapper();
							String jsonInString = jsonMapper.writeValueAsString(idto);

							HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
							httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			 				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/payments";
							Map<String, String> params = new HashMap<String, String>();
							HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
							RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class, params);
							HttpStatus statusCode = responseEntity.getStatusCode();

							if (statusCode.value() == 200) {
								String body = responseEntity.getBody();
								if (body != null) {
									ObjectMapper mapper = new ObjectMapper();
									Receipt[] response = mapper.readValue(body, Receipt[].class);
									List<Receipt> objList = Arrays.asList(response);
									
									if (!objList.isEmpty()) {
	 
										DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");   
	 	 	 					        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
	 	 	 					        
	 	 	 					        for(FiscalDocuments fd : fdList) {
											for (Receipt o : objList) {
												o.setUuid(o.getUuid().trim());
												if(fd.getUuidFactura().equals(o.getUuid())) {
													fd.setPaymentReference(o.getPaymentReference());
													fd.setPaymentAmount(o.getPaymentAmount());
													fd.setPaymentDate(o.getPaymentDate());
													fd.setPaymentStatus(AppConstants.STATUS_GR_PAID);
													fd.setStatus(AppConstants.STATUS_PAID);
													fd.setPortalPaymentDate(new Date());
													fiscalDocumentService.updateDocument(fd);
												}
											}
	 	 	 					        }
										//purchaseOrderService.updatePaymentReceipts(objList);										
										
										for (Receipt o : objList) {										
											Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
											if(s!=null) {
			 	 								String emailRecipient = (s.getEmailSupplier());
			 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
			 	 	 							String emailContent = AppConstants.EMAIL_PAYMENT_NO_OC_NOTIF_CONTENT;
			 	 	 							emailContent = emailContent.replace("_UUID_", o.getUuid());
												String strDate = dateFormat.format(o.getPaymentDate());
												emailContent = emailContent.replace("_DATE_", strDate);

			 	 	 							String currency = format.format(o.getPaymentAmount());
			 	 	 							emailContent = emailContent.replace("_AMOUNT_", currency);
			 	 	 							emailContent = emailContent.replace("_PID_", o.getPaymentReference());

			 	 	 							emailAsyncSup.setProperties(
			 	 	 									AppConstants.EMAIL_PAYMENT_RECEIPT_NOTIF,
			 	 	 									stringUtils.prepareEmailContent(emailContent + AppConstants.EMAIL_PORTAL_LINK),emailRecipient);
			 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
			 	 	 							//emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
			 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
			 	 	 							emailThreadSup.start();
			 								}
											
										}

										log4j.info("Fiscal Docs Guardados:" + response.length);
									}
								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}

	}*/
	
	public void getPurchaseOrderListBySelection(int orderNumber, String addressNumber, String fromDate, String toDate, String lttr, String nxtr) {
		this.getPurchaseOrderListByFilter(orderNumber, addressNumber, fromDate, toDate);
	}

	private void getPurchaseOrderListByFilter(int orderNumber, String addressNumber, String fromDate, String toDate) {
 		try {
 			
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			map.add("addressNumber", addressNumber);
			map.add("orderNumber", String.valueOf(orderNumber));
			map.add("fromDate", fromDate);
			map.add("toDate", toDate);
			
			String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poListBySelection";
			HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
			HttpStatus statusCode = responseEntity.getStatusCode();

			if (statusCode.value() == 200) {
				String body = responseEntity.getBody();
				if (body != null) {
					ObjectMapper mapper = new ObjectMapper();
					PurchaseOrder[] response = mapper.readValue(body, PurchaseOrder[].class);
					List<PurchaseOrder> objList = Arrays.asList(response);
					if (!objList.isEmpty()) {
						for(PurchaseOrder o : objList) {
							if(o.getPurchaseOrderDetail() != null && !o.getPurchaseOrderDetail().isEmpty()) {
								for(PurchaseOrderDetail d : o.getPurchaseOrderDetail()) {
	 								if((d.getAmuntReceived() < 0d|| d.getForeignAmount() < 0d) 
	 										&& AppConstants.JDE_RETENTION_CODE.equals(String.valueOf(d.getObjectAccount()).trim())) {
	 									d.setAmount(d.getAmount()*-1);
	 									d.setAmuntReceived(d.getAmuntReceived()*-1);
	 									d.setForeignAmount(d.getForeignAmount()*-1);
	 									d.setPending(d.getPending()*-1);
	 									d.setQuantity(d.getQuantity()*-1);
	 									d.setReceived(d.getReceived()*-1); 		 									
	 									d.setReceiptType(AppConstants.RECEIPT_CODE_RETENTION);
	 								}
								}
							}
							o.setOrderStauts(AppConstants.STATUS_OC_RECEIVED);
							Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
							if(s!=null) {
								o.setLongCompanyName(s.getRazonSocial());
							}
						}
						purchaseOrderService.saveMultiple(objList);
					}
				}
			}
			getPurchaseReceiptListBySelection(orderNumber, addressNumber, fromDate, toDate);
 		} catch (Exception e) {
 			log4j.error("Exception" , e);
 			e.printStackTrace();
 		}
	}
	
	private void getPurchaseReceiptListBySelection(int orderNumber, String addressNumber, String fromDate, String toDate) {
		log4j.info("Carga recibos: " + new Date());
 		try {

 				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
 				map.add("addressNumber", addressNumber);
 				map.add("orderNumber", String.valueOf(orderNumber));
 				map.add("fromDate", fromDate);
 				map.add("toDate", toDate);
 				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poReceiptsBySelection";
				
				HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
 				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
				HttpStatus statusCode = responseEntity.getStatusCode();
 				if (statusCode.value() == 200) {
 					String body = responseEntity.getBody();
 					if (body != null) {
 						ObjectMapper mapper = new ObjectMapper();
 						Receipt[] response = mapper.readValue(body, Receipt[].class);
 						List<Receipt> objList = Arrays.asList(response);
 						if (!objList.isEmpty()) {
 							List<Receipt> insertedRows = purchaseOrderService.saveMultipleReceipt(objList);
 							List<PurchaseOrder> updateList = new ArrayList<PurchaseOrder>();
 							List<Integer> idList = new ArrayList<Integer>();
 							for(Receipt o : insertedRows) {
 								PurchaseOrder ao = purchaseOrderService.searchbyOrderAndAddressBook(o.getOrderNumber(), o.getAddressNumber(), o.getOrderType());			
 	 							if(ao != null) {
 	 								if(!idList.contains(ao.getId())) {
 	 	 								ao.setOrderStauts(AppConstants.STATUS_OC_APPROVED);
 	 	 								ao.setRelatedStatus(AppConstants.STATUS_UNCOMPLETE);
 	 	 								ao.setStatus(AppConstants.STATUS_OC_APPROVED);
 	 	 								updateList.add(ao);
 	 	 								idList.add(ao.getId());
 	 								}
 	 							}
 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 								if(s!=null) {
 	 								String emailRecipient = (s.getEmailSupplier());
 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
 	 	 							emailAsyncSup.setProperties(
 	 	 									AppConstants.EMAIL_NEW_RECEIPT_NOTIF + o.getOrderNumber(),
 	 	 									stringUtils.prepareEmailContent("Estimado proveedor,<br /><br />" + 
 	 	 											"Su orden de compra " + o.getOrderNumber() + " ha sido marcada como recibida en nuestro sistema.<br /><br />" + 
 	 	 											"Puede proceder a cargar su factura en el portal utilizando los siguientes datos:<br /><br />" + 
 	 	 											"- Número de recibo: " + o.getDocumentNumber() + "<br /><br />" + 
 	 	 											"- Orden de compra: " + o.getOrderNumber() + "-" + o.getOrderType() + "<br /><br />" + 
 	 	 											"Le recordamos que nuestro portal de proveedores está disponible en: " + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />" +
 	 	 											"NOTA: No responda a este correo, ha sido enviado desde una cuenta no monitoreada.<br /><br />"),
 	 	 									emailRecipient);
 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
 	 	 							emailThreadSup.start();
 								}
 							}
 							purchaseOrderService.updateMultiple(updateList); 							
 						}
 					}
 				}
 				log4j.info("Procesados los recibos (manual)");
 		} catch (Exception e) {
 			log4j.error("Exception" , e);
 			e.printStackTrace();
 		}
 	}
	
	//@Scheduled(fixedDelay = 9920000, initialDelay = 3000)
	//@Scheduled(cron = "0 0 7,10,13,16,19 * * ?")
 	public void getPurchaseOrderList() {
		log4j.info("Carga órdenes: " + new Date());
 		try {
 			 List<String> noApprovalFlowPOList = new ArrayList<String>();
 			 UDC noApprovalFlowPO = udcService.searchBySystemAndKey("NOAPPROVALFLOWPO", "UNIQUE");
 			 if(noApprovalFlowPO != null && noApprovalFlowPO.getStrValue1() != null && !noApprovalFlowPO.getStrValue1().trim().isEmpty()) {
 				String[] poTypeArray = noApprovalFlowPO.getStrValue1().trim().replace(" ", "").split(","); 				
 				noApprovalFlowPOList = Arrays.asList(poTypeArray);
 			 }
 			 
			  for(int i=0; i<=800; i++) {
			  int start = i*3;
			  int limit = 3;
			  List<SupplierDTO> supDtoList = supplierService.getList(start, limit);
 			  String supList = "";
 			  if (!supDtoList.isEmpty()) {
					List<String> sList = new ArrayList<String>();
					for (SupplierDTO sdto : supDtoList) {
						if(sdto.getAddresNumber() != null && !"".equals(sdto.getAddresNumber())){
							sList.add(sdto.getAddresNumber().trim());
						}
					}
					String idList = sList.toString();
					supList = idList.substring(1, idList.length() - 1).replace(", ", ",");
 			  }
 			
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
				map.add("supList", supList);
				
				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poList";
				
				if(this.orderDate != null && !"".equals(this.orderDate)) {
					map.add("orderDate", this.orderDate);
					map.add("lttr", lttr);
					map.add("nxtr", nxtr);
					url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poListBySelection";
				}
				
				HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
	
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
				HttpStatus statusCode = responseEntity.getStatusCode();

 				if (statusCode.value() == 200) {
 					String body = responseEntity.getBody();
 					if (body != null) {
 						ObjectMapper mapper = new ObjectMapper();
 						PurchaseOrder[] response = mapper.readValue(body, PurchaseOrder[].class);
 						List<PurchaseOrder> objList = Arrays.asList(response);
 						if (!objList.isEmpty()) {
 							//log4j.info("REGISTROS:" + objList.size());
 							
 							for(PurchaseOrder o : objList) {
 								if(o.getPurchaseOrderDetail() != null && !o.getPurchaseOrderDetail().isEmpty()) {
 									for(PurchaseOrderDetail d : o.getPurchaseOrderDetail()) {
 		 								if((d.getAmuntReceived() < 0d|| d.getForeignAmount() < 0d) 
 		 										&& AppConstants.JDE_RETENTION_CODE.equals(String.valueOf(d.getObjectAccount()).trim())) {
 		 									
 		 									d.setAmount(d.getAmount()*-1);
 		 									d.setAmuntReceived(d.getAmuntReceived()*-1);
 		 									d.setForeignAmount(d.getForeignAmount()*-1);
 		 									d.setPending(d.getPending()*-1);
 		 									d.setQuantity(d.getQuantity()*-1);
 		 									d.setReceived(d.getReceived()*-1); 		 									
 		 									d.setReceiptType(AppConstants.RECEIPT_CODE_RETENTION);
 		 								}
 									}
 									
 								}
 								if(noApprovalFlowPOList.contains(o.getOrderType())) {
 									o.setOrderStauts(AppConstants.STATUS_OC_RECEIVED);//No pasa por flujo de aprobación
 								} else {
 									o.setOrderStauts(AppConstants.STATUS_OC_REQUESTED);
 								} 								
 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 								if(s!=null) {
 									o.setLongCompanyName(s.getRazonSocial());
 								}
 							}
 							
 							List<PurchaseOrder> returnedList = purchaseOrderService.saveMultiple(objList);
 							if(returnedList != null && !returnedList.isEmpty()) {
 								List<String> poNumberList = new ArrayList<String>();
 								for(PurchaseOrder poSaved : returnedList) {
 									poNumberList.add(String.valueOf(poSaved.getOrderNumber()));	 									
 								}
 								log4j.info("OC Registradas: " + String.join(", ", poNumberList)  + ".");
 							}
 							/*JSC: Se comenta la notificación de OC en acuerdo con Cristian. 29/10/2025 
 							for(PurchaseOrder o : returnedList) {
 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 								if(s!=null) {	
 	 								String emailRecipient = (s.getEmailSupplier());
	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	 	 							emailAsyncSup.setProperties(
	 	 									AppConstants.EMAIL_NEW_ORDER_NOTIF + o.getOrderNumber() + "-" + o.getOrderType(),
	 	 									stringUtils.prepareEmailContent(AppConstants.EMAIL_PURCHASE_NEW2 + o.getOrderNumber() + "-"
	 	 											+ o.getOrderType() + "<br /><br />" + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />"
	 	 											+ "Cuenta: " + o.getAddressNumber() + "<br /> Razon Social: " + s.getRazonSocial()),
	 	 									emailRecipient);
	 	 							emailAsyncSup.setMailSender(mailSenderObj);
	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
	 	 							emailThreadSup.start();
 								}
 							}
 							*/
 							//log4j.info("Procesado:" + response.length);
 						}
 					}
 				}
 				
 				getPurchaseReceiptList(supList);
			  }
			  log4j.info("================== Fin Carga órdenes: " + new Date() + " ==================");
 		} catch (Exception e) {
 			log4j.error("Exception" , e);
 			e.printStackTrace();
 		}
 	}
  

 	public void getPurchaseReceiptList(String supList) {
 		//log4j.info("Carga recibos: " + new Date());
 		try {
 				
 				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
				map.add("supList", supList);
				
 				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poReceipts";
				
				if(this.orderDate != null && !"".equals(this.orderDate)) {
					map.add("orderDate", this.orderDate);
					map.add("lttr", lttr);
					map.add("nxtr", nxtr);
					url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poReceiptsBySelection";
				}
				
				HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
 				
 				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
				HttpStatus statusCode = responseEntity.getStatusCode();
 				
 				if (statusCode.value() == 200) {
 					String body = responseEntity.getBody();
 					if (body != null) {
 						ObjectMapper mapper = new ObjectMapper();
 						Receipt[] response = mapper.readValue(body, Receipt[].class);
 						List<Receipt> objList = Arrays.asList(response);
 						if (!objList.isEmpty()) {
 							//log4j.info("REGISTROS:" + objList.size());
 							
 							List<Receipt> insertedRows = purchaseOrderService.saveMultipleReceipt(objList);
 							List<PurchaseOrder> updateList = new ArrayList<PurchaseOrder>();
 							List<Integer> idList = new ArrayList<Integer>();
 							List<String> receiptList = new ArrayList<String>();
 							
 							for(Receipt o : insertedRows) {
 								String receiptString = String.valueOf(o.getAddressNumber()).concat("_")
											.concat(String.valueOf(o.getOrderNumber())).concat("_")
											.concat(o.getOrderType()).concat("_")
											.concat(String.valueOf(o.getDocumentNumber()));
 								
 								if(!receiptList.contains(receiptString)) {
 									receiptList.add(receiptString);
 								}
 								
 								PurchaseOrder ao = purchaseOrderService.searchbyOrderAndAddressBook(o.getOrderNumber(), o.getAddressNumber(), o.getOrderType());			
 	 							if(ao != null && !AppConstants.STATUS_OC_REQUESTED.equals(ao.getOrderStauts())) {
 	 								if(!idList.contains(ao.getId())) {
 	 	 								ao.setOrderStauts(AppConstants.STATUS_OC_APPROVED);
 	 	 								ao.setRelatedStatus(AppConstants.STATUS_UNCOMPLETE);
 	 	 								ao.setStatus(AppConstants.STATUS_OC_APPROVED);
 	 	 								updateList.add(ao);
 	 	 								idList.add(ao.getId());
 	 								}
 	 							}
 	 							
 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 								if(s!=null) {
 	 								String emailRecipient = (s.getEmailSupplier());
 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
 	 	 							emailAsyncSup.setProperties(
 	 	 									AppConstants.EMAIL_NEW_RECEIPT_NOTIF + o.getOrderNumber(),
 	 	 									stringUtils.prepareEmailContent("Estimado proveedor,<br /><br />" + 
 	 	 											"Su orden de compra " + o.getOrderNumber() + " ha sido marcada como recibida en nuestro sistema.<br /><br />" + 
 	 	 											"Puede proceder a cargar su factura en el portal utilizando los siguientes datos:<br /><br />" + 
 	 	 											"- Número de recibo: " + o.getDocumentNumber() + "<br /><br />" + 
 	 	 											"- Orden de compra: " + o.getOrderNumber() + "-" + o.getOrderType() + "<br /><br />" + 
 	 	 											"Le recordamos que nuestro portal de proveedores está disponible en: " + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />" +
 	 	 											"NOTA: No responda a este correo, ha sido enviado desde una cuenta no monitoreada.<br /><br />"),
 	 	 									emailRecipient);
 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
 	 	 							emailThreadSup.start();
 								} 							
 							}
 							
 							purchaseOrderService.updateMultiple(updateList);
 							
 							//log4j.info("Procesados los recibos");
 							if(receiptList != null && !receiptList.isEmpty()) {
 								log4j.info("Recibos Registrados: " + String.join(", ", receiptList)  + ".");
 							}
 						}
 					}
 				}
 		} catch (Exception e) {
 			log4j.error("Exception" , e);
 			e.printStackTrace();
 		}
 	}
  
 	//@Scheduled(cron = "0 0 22 * * ?")
	//@Scheduled(fixedDelay = 9920000, initialDelay = 15000)
 	public void getPurchaseOrderListHistory() {
 				log4j.info("Carga órdenes Histórico: " + new Date());
 	 		try {
 	 			
 				  for(int i=0; i<=800; i++) {
 				  int start = i*3;
 				  int limit = 3;
 				  List<SupplierDTO> supDtoList = supplierService.getList(start, limit);
 	 			  String supList = "";
 	 			  if (!supDtoList.isEmpty()) {
 						List<String> sList = new ArrayList<String>();
 						for (SupplierDTO sdto : supDtoList) {
 							if(sdto.getAddresNumber() != null && !"".equals(sdto.getAddresNumber())){
 								sList.add(sdto.getAddresNumber().trim());
 							}
 						}
 						String idList = sList.toString();
 						supList = idList.substring(1, idList.length() - 1).replace(", ", ",");
 	 			  }
 	 			
 					HttpHeaders httpHeaders = new HttpHeaders();
 					httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
 	 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
 					map.add("supList", supList);
 					String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poListHistory";
 					HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
 					RestTemplate restTemplate = new RestTemplate();
 					ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
 					HttpStatus statusCode = responseEntity.getStatusCode();

 	 				if (statusCode.value() == 200) {
 	 					String body = responseEntity.getBody();
 	 					if (body != null) {
 	 						ObjectMapper mapper = new ObjectMapper();
 	 						PurchaseOrder[] response = mapper.readValue(body, PurchaseOrder[].class);
 	 						List<PurchaseOrder> objList = Arrays.asList(response);
 	 						if (!objList.isEmpty()) {
 	 							log4j.info("REGISTROS:" + objList.size());
 	 							
 	 							for(PurchaseOrder o : objList) {
 	 								if(o.getPurchaseOrderDetail() != null && !o.getPurchaseOrderDetail().isEmpty()) {
 	 									for(PurchaseOrderDetail d : o.getPurchaseOrderDetail()) {
 	 		 								if((d.getAmuntReceived() < 0d|| d.getForeignAmount() < 0d) 
 	 		 										&& AppConstants.JDE_RETENTION_CODE.equals(String.valueOf(d.getObjectAccount()).trim())) {
 	 		 									
 	 		 									d.setAmount(d.getAmount()*-1);
 	 		 									d.setAmuntReceived(d.getAmuntReceived()*-1);
 	 		 									d.setForeignAmount(d.getForeignAmount()*-1);
 	 		 									d.setPending(d.getPending()*-1);
 	 		 									d.setQuantity(d.getQuantity()*-1);
 	 		 									d.setReceived(d.getReceived()*-1); 		 									
 	 		 									d.setReceiptType(AppConstants.RECEIPT_CODE_RETENTION);
 	 		 								}
 	 									}
 	 								}
 	 								o.setOrderStauts(AppConstants.STATUS_OC_RECEIVED);
 	 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 	 								if(s!=null) {
 	 									o.setLongCompanyName(s.getRazonSocial());
 	 								}
 	 							}
 	 							purchaseOrderService.saveMultiple(objList);
 	 						}
 	 					}
 	 				}
 	 				getPurchaseReceiptListHistory(supList);
 				  }
 				   log4j.info("Termina la carga................");
 	 		} catch (Exception e) {
 	 			log4j.error("Exception" , e);
 	 			e.printStackTrace();
 	 		}
 	 	}
 	  
 	  
 	 	public void getPurchaseReceiptListHistory(String supList) {
 	 		log4j.info("Carga recibos: " + new Date());
 	 		try {
 	 				
 	 				HttpHeaders httpHeaders = new HttpHeaders();
 					httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
 	 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
 					map.add("supList", supList);
 					
 	 				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHameProd/poReceiptsHistory";
 					HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);
 	 				
 	 				RestTemplate restTemplate = new RestTemplate();
 					ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
 					HttpStatus statusCode = responseEntity.getStatusCode();
 	 				
 	 				if (statusCode.value() == 200) {
 	 					String body = responseEntity.getBody();
 	 					if (body != null) {
 	 						ObjectMapper mapper = new ObjectMapper();
 	 						Receipt[] response = mapper.readValue(body, Receipt[].class);
 	 						List<Receipt> objList = Arrays.asList(response);
 	 						if (!objList.isEmpty()) {
 	 							log4j.info("REGISTROS:" + objList.size());
 	 							
 	 							List<Receipt> insertedRows = purchaseOrderService.saveMultipleReceipt(objList);
 	 							List<PurchaseOrder> updateList = new ArrayList<PurchaseOrder>();
 	 							List<Integer> idList = new ArrayList<Integer>();
 	 							
 	 							for(Receipt o : insertedRows) {
 	 								PurchaseOrder ao = purchaseOrderService.searchbyOrderAndAddressBook(o.getOrderNumber(), o.getAddressNumber(), o.getOrderType());			
 	 	 							if(ao != null) {
 	 	 								if(!idList.contains(ao.getId())) {
 	 	 	 								ao.setOrderStauts(AppConstants.STATUS_OC_APPROVED);
 	 	 	 								ao.setRelatedStatus(AppConstants.STATUS_UNCOMPLETE);
 	 	 	 								ao.setStatus(AppConstants.STATUS_OC_APPROVED);
 	 	 	 								updateList.add(ao);
 	 	 	 								idList.add(ao.getId());
 	 	 								}
 	 	 							}
 	 	 							
 	 								Supplier s = supplierService.searchByAddressNumber(o.getAddressNumber());
 	 								if(s!=null) {
 	 	 								String emailRecipient = (s.getEmailSupplier());
 	 	 	 							EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
 	 	 	 							emailAsyncSup.setProperties(
 	 	 	 									AppConstants.EMAIL_NEW_RECEIPT_NOTIF + o.getOrderNumber(),
 	 	 	 									stringUtils.prepareEmailContent("Estimado proveedor,<br /><br />" + 
 	 	 	 											"Su orden de compra " + o.getOrderNumber() + " ha sido marcada como recibida en nuestro sistema.<br /><br />" + 
 	 	 	 											"Puede proceder a cargar su factura en el portal utilizando los siguientes datos:<br /><br />" + 
 	 	 	 											"- Número de recibo: " + o.getDocumentNumber() + "<br /><br />" + 
 	 	 	 											"- Orden de compra: " + o.getOrderNumber() + "-" + o.getOrderType() + "<br /><br />" + 
 	 	 	 											"Le recordamos que nuestro portal de proveedores está disponible en: " + AppConstants.EMAIL_PORTAL_LINK + "<br /><br />" +
 	 	 	 											"NOTA: No responda a este correo, ha sido enviado desde una cuenta no monitoreada.<br /><br />"),
 	 	 	 									emailRecipient);
 	 	 	 							emailAsyncSup.setMailSender(mailSenderObj);
 	 	 	 							emailAsyncSup.setAdditionalReference(udcDao, o.getOrderType());
 	 	 	 							Thread emailThreadSup = new Thread(emailAsyncSup);
 	 	 	 							emailThreadSup.start();
 	 								} 							
 	 							}
 	 							
 	 							purchaseOrderService.updateMultiple(updateList); 							
 	 							log4j.info("Procesados los recibos");
 	 						}
 	 					}
 	 				}
 	 		} catch (Exception e) {
 	 			log4j.error("Exception" , e);
 	 			e.printStackTrace();
 	 		}
 	 	}
 	 	
  @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public void getTolerances() {
	log4j.info("Inicio carga tolerancias:");
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Accept", "application/json");
      String url = "http://localhost:8081/supplierWebPortalRest/toList";
      Map<String, String> params = new HashMap<>();
      HttpEntity<?> httpEntity = new HttpEntity((MultiValueMap)httpHeaders);
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081/supplierWebPortalRest/toList", HttpMethod.GET, httpEntity, String.class, 
          params);
      HttpStatus statusCode = responseEntity.getStatusCode();
      if (statusCode.value() == 200) {
        String body = (String)responseEntity.getBody();
        if (body != null) {
          ObjectMapper mapper = new ObjectMapper();
          Tolerances[] response = (Tolerances[])mapper.readValue(body, Tolerances[].class);
          List<Tolerances> objList = Arrays.asList(response);
          if (!objList.isEmpty()) {
            this.tolerancesService.deleteRecords();
            this.tolerancesService.saveMultiple(objList);
            log4j.info("Guardado:" + response.length);
          } 
        } 
      } 
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
    } 
  }
  
  

	
  
  @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public boolean sendOrderInvoiceConfirmation(List<PurchaseOrderDTO> poList) {
    try {
      if (poList.size() > 0) {
        ObjectMapper jsonMapper = new ObjectMapper();
        String jsonInString = jsonMapper.writeValueAsString(poList);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:8081/supplierWebPortalRestHame/orderInvoiceConfirm";
        Map<String, String> params = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity(jsonInString, (MultiValueMap)httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081/supplierWebPortalRestHame/orderInvoiceConfirm", HttpMethod.POST, httpEntity, 
            String.class, params);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode.value() == 200) {
          String body = (String)responseEntity.getBody();
          if (body != null) {
            ObjectMapper mapper = new ObjectMapper();
            PurchaseOrderDTO[] response = (PurchaseOrderDTO[])mapper.readValue(body, PurchaseOrderDTO[].class);
            List<PurchaseOrderDTO> objList = Arrays.asList(response);
            if (!objList.isEmpty())
            	log4j.info("Guardado:" + response.length); 
            return true;
          } 
        } 
      } 
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
      return false;
    } 
    return false;
  }
  
  	@SuppressWarnings("unused")
  	public boolean sendOrderConfirmation(PurchaseOrderDTO o) {
		final SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
		String resp = "";
		String jsonInString="";
	    String url="";
	    String addressBook = "";
	    String vinv = "";
	    
		try {
			if (o != null) {
				ObjectMapper jsonMapper = new ObjectMapper();
				jsonInString = jsonMapper.writeValueAsString(o);
				log4j.info(jsonInString);
				
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/updateOrderStatus";
				Map<String, String> params = new HashMap<String, String>();
				HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
						String.class, params);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					ObjectMapper mapper = new ObjectMapper();
					PurchaseOrderDTO response = mapper.readValue(body, PurchaseOrderDTO.class);
					log4j.info("Actualizado:" + response.getPHDOCO());
					resp = String.valueOf(response.getPHDOCO());
				}
				else if (statusCode.value() == 500) {
					loggerJEdwars.putInitial(url, jsonInString, "Error 500:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);
					this.sendErrorMail(addressBook, vinv, false);
				}
				else {
					loggerJEdwars.putInitial(url, jsonInString, "Error estatus:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);			
					this.sendErrorMail(addressBook, vinv, false);
				}
			}
			return true;
		} catch (HttpServerErrorException e500) {
			log4j.error("HttpServerErrorException" , e500);
			loggerJEdwars.putInitial(url, jsonInString, "Error [HttpServerErrorException]:"+e500.getMessage()+">>>"+ StringUtils.getString(e500), AppConstants.LOGGER_JEDWARS_ERROR);
			e500.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return false;
		} catch (ResourceAccessException eRE) {
			log4j.error("ResourceAccessException" , eRE);
			loggerJEdwars.putInitial(url, jsonInString, "Error [ResourceAccessException]:"+eRE.getMessage()+">>>"+ StringUtils.getString(eRE), AppConstants.LOGGER_JEDWARS_ERROR);
			eRE.printStackTrace();
			this.sendErrorMail(addressBook, vinv, true);
			return false;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			loggerJEdwars.putInitial(url, jsonInString, "Error [General]:"+e.getMessage()+">>>"+ StringUtils.getString(e), AppConstants.LOGGER_JEDWARS_ERROR);
			e.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return false;
		}
  	}

  
  @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public void sendReceivingAdvice(ReceivingAdviceHeaderDTO o) {
    SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
    try {
      if (o != null) {
        ObjectMapper jsonMapper = new ObjectMapper();
        String jsonInString = jsonMapper.writeValueAsString(o);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/json");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:8081/supplierWebPortalRestHame/postReceivingAdvice";
        Map<String, String> params = new HashMap<>();
        HttpEntity<?> httpEntity = new HttpEntity(jsonInString, (MultiValueMap)httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081/supplierWebPortalRestHame/postReceivingAdvice", HttpMethod.POST, httpEntity, 
            String.class, params);
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (statusCode.value() == 200) {
          String body = (String)responseEntity.getBody();
          ObjectMapper mapper = new ObjectMapper();
          ReceivingAdviceHeaderDTO response = (ReceivingAdviceHeaderDTO)mapper.readValue(body, ReceivingAdviceHeaderDTO.class);
          log4j.info("Guardado:" + response.getSYEDOC());
        } 
      } 
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
    } 
  }
  
  @SuppressWarnings("unused")
	public String sendVoucher(VoucherHeaderDTO o) {
		final SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
		String resp = "";
		String jsonInString="";
	    String url="";
	    String addressBook = "";
	    String vinv = "";
	    
		try {
			if (o != null) {
				try {
					addressBook = String.valueOf(o.getSYAN8());
					vinv = o.getSYVINV();	
				} catch (Exception e) {
					log4j.error("Exception" , e);
				}
				
				ObjectMapper jsonMapper = new ObjectMapper();
				jsonInString = jsonMapper.writeValueAsString(o);
				log4j.info(jsonInString);
				
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/postVoucher";
				Map<String, String> params = new HashMap<String, String>();
				HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
						String.class, params);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					ObjectMapper mapper = new ObjectMapper();
					VoucherHeaderDTO response = mapper.readValue(body, VoucherHeaderDTO.class);
					log4j.info("Guardado:" + response.getSYEDOC());
					resp = String.valueOf(response.getSYEDOC());
				}
				else if (statusCode.value() == 500) {
					loggerJEdwars.putInitial(url, jsonInString, "Error 500:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);
					this.sendErrorMail(addressBook, vinv, false);
				}
				else {
					loggerJEdwars.putInitial(url, jsonInString, "Error estatus:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);			
					this.sendErrorMail(addressBook, vinv, false);
				}
			}
			return resp;
		} catch (HttpServerErrorException e500) {
			log4j.error("HttpServerErrorException" , e500);
			loggerJEdwars.putInitial(url, jsonInString, "Error [HttpServerErrorException]:"+e500.getMessage()+">>>"+ StringUtils.getString(e500), AppConstants.LOGGER_JEDWARS_ERROR);
			e500.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return null;
		} catch (ResourceAccessException eRE) {
			log4j.error("ResourceAccessException" , eRE);
			loggerJEdwars.putInitial(url, jsonInString, "Error [ResourceAccessException]:"+eRE.getMessage()+">>>"+ StringUtils.getString(eRE), AppConstants.LOGGER_JEDWARS_ERROR);
			eRE.printStackTrace();
			this.sendErrorMail(addressBook, vinv, true);
			return null;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			loggerJEdwars.putInitial(url, jsonInString, "Error [General]:"+e.getMessage()+">>>"+ StringUtils.getString(e), AppConstants.LOGGER_JEDWARS_ERROR);
			e.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return null;
		}

	}
  
  @SuppressWarnings("unused")
	public String sendVoucherWithoutReceipt(VoucherHeaderDTO o) {
		final SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
		String resp = "";
		String jsonInString="";
	    String url="";
	    String addressBook = "";
	    String vinv = "";
	    
		try {
			if (o != null) {
				try {
					addressBook = String.valueOf(o.getSYAN8());
					vinv = o.getSYVINV();	
				} catch (Exception e) {
					log4j.error("Exception" , e);
				}
				
				ObjectMapper jsonMapper = new ObjectMapper();
				jsonInString = jsonMapper.writeValueAsString(o);
				log4j.info(jsonInString);
				
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/postVoucherWithoutReceipt";
				Map<String, String> params = new HashMap<String, String>();
				HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
						String.class, params);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					ObjectMapper mapper = new ObjectMapper();
					VoucherHeaderDTO response = mapper.readValue(body, VoucherHeaderDTO.class);
					log4j.info("Guardado:" + response.getSYEDOC());
					resp = String.valueOf(response.getSYEDOC());
				}
				else if (statusCode.value() == 500) {
					loggerJEdwars.putInitial(url, jsonInString, "Error 500:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);
					this.sendErrorMail(addressBook, vinv, false);
				}
				else {
					loggerJEdwars.putInitial(url, jsonInString, "Error estatus:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);			
					this.sendErrorMail(addressBook, vinv, false);
				}
			}
			return resp;
		} catch (HttpServerErrorException e500) {
			log4j.error("HttpServerErrorException" , e500);
			loggerJEdwars.putInitial(url, jsonInString, "Error [HttpServerErrorException]:"+e500.getMessage()+">>>"+ StringUtils.getString(e500), AppConstants.LOGGER_JEDWARS_ERROR);
			e500.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return null;
		} catch (ResourceAccessException eRE) {
			log4j.error("ResourceAccessException" , eRE);
			loggerJEdwars.putInitial(url, jsonInString, "Error [ResourceAccessException]:"+eRE.getMessage()+">>>"+ StringUtils.getString(eRE), AppConstants.LOGGER_JEDWARS_ERROR);
			eRE.printStackTrace();
			this.sendErrorMail(addressBook, vinv, true);
			return null;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			loggerJEdwars.putInitial(url, jsonInString, "Error [General]:"+e.getMessage()+">>>"+ StringUtils.getString(e), AppConstants.LOGGER_JEDWARS_ERROR);
			e.printStackTrace();
			this.sendErrorMail(addressBook, vinv, false);
			return null;
		}

	}

  public String sendJournalEntries(BatchJournalDTO o) {
    String resp = "";
	String jsonInString="";
    String url="";
    String addressBook = "";
    String vinv = "";
    
    try {
		if (o != null) {
			try {
				addressBook = String.valueOf(o.getVoucherEntries().get(0).getVLAN8());
				vinv = o.getVoucherEntries().get(0).getVLVINV();
			} catch (Exception e) {
				log4j.error("Exception" , e);
			}
			
			ObjectMapper jsonMapper = new ObjectMapper();
			 jsonInString = jsonMapper.writeValueAsString(o);
			 log4j.info(jsonInString);
			
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			 url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/postJournalEntries";
			Map<String, String> params = new HashMap<String, String>();
			HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
					String.class, params);
			HttpStatus statusCode = responseEntity.getStatusCode();

			if (statusCode.value() == 200) {
				String body = responseEntity.getBody();
				ObjectMapper mapper = new ObjectMapper();
				BatchJournalDTO response = mapper.readValue(body, BatchJournalDTO.class);
				log4j.info("Guardado:" + response.getJournalEntries().get(0).getVNEDBT());
				resp = String.valueOf(response.getJournalEntries().get(0).getVNEDBT());
				
			} else if (statusCode.value() == 500) {
				loggerJEdwars.putInitial(url, jsonInString, "Error 500:"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);
				this.sendErrorMail(addressBook, vinv, false);
			}
			else {
				loggerJEdwars.putInitial(url, jsonInString, "Error estatus :"+statusCode.value()+" >>> "+responseEntity.getBody(), AppConstants.LOGGER_JEDWARS_ERROR);
				this.sendErrorMail(addressBook, vinv, false);
			}
		}
		return resp;
		
	} catch (HttpServerErrorException e500) {
		  log4j.error("HttpServerErrorException" , e500);
	  	  loggerJEdwars.putInitial(url, jsonInString, "Error [HttpServerErrorException]:"+e500.getMessage()+">>>"+ StringUtils.getString(e500), AppConstants.LOGGER_JEDWARS_ERROR);
	  	  e500.printStackTrace();
	  	  this.sendErrorMail(addressBook, vinv, false);
	      return null;		
	} catch (ResourceAccessException eRE) {
		  log4j.error("ResourceAccessException" , eRE);
	  	  loggerJEdwars.putInitial(url, jsonInString, "Error [ResourceAccessException]:"+eRE.getMessage()+">>>"+ StringUtils.getString(eRE), AppConstants.LOGGER_JEDWARS_ERROR);
	  	  eRE.printStackTrace();	  	  
	  	  this.sendErrorMail(addressBook, vinv, true);
	  	  return null;
	} catch (Exception e) {
		  log4j.error("Exception" , e);
    	  loggerJEdwars.putInitial(url, jsonInString, "Error [General]:"+e.getMessage()+">>>"+ StringUtils.getString(e), AppConstants.LOGGER_JEDWARS_ERROR);
          e.printStackTrace();          
          this.sendErrorMail(addressBook, vinv, false);
          return null;
      }
  }
  
  public void sendErrorMail(String addressBook, String vinv, boolean isHameAlert) {
	  try {
		  UDC udcAlert = udcService.searchBySystemAndKey("JDESENDALERT", "ENABLED");
		  if(udcAlert != null && "TRUE".equals(udcAlert.getStrValue1())) {
			  
			  String emailAlert = "";
			  UDC udcSupportMail = udcService.searchBySystemAndKey("JDESENDALERT", "SUPPORTMAIL");
			  if(udcSupportMail != null && !udcSupportMail.getStrValue1().trim().isEmpty()) {
				  emailAlert = udcSupportMail.getStrValue1().trim();
			  }
			  
			  if(isHameAlert) {
				  UDC udcHameMail = udcService.searchBySystemAndKey("JDESENDALERT", "SEPASAMAIL");
				  if(udcHameMail != null && !udcHameMail.getStrValue1().trim().isEmpty()) {
					  emailAlert = udcHameMail.getStrValue1().trim() + "," + emailAlert;  
				  }
			  }
			  
		  	  EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
		  	  emailAsyncSup.setProperties(AppConstants.EMAIL_INV_JDE_ERROR_SUBJECT,
		  			  stringUtils.prepareEmailContent(AppConstants.EMAIL_INV_JDE_ERROR_CONTENT
		  					  .replace("_VINV_", vinv)
		  					  .replace("_SUPPLIER_", addressBook)),
		  			  emailAlert);
		  	  emailAsyncSup.setMailSender(mailSenderObj);
		  	  Thread emailThreadSup = new Thread(emailAsyncSup);
		  	  emailThreadSup.start();
		  }		 
	} catch (Exception e) {
		log4j.error("Exception" , e);
		e.printStackTrace();
	}
  }
  
  @SuppressWarnings("unused")
public SupplierJdeDTO sendAddressBook(SupplierJdeDTO o) {
	  final SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
		String resp = "";
		try {
			if (o != null) {
				ObjectMapper jsonMapper = new ObjectMapper();
				String jsonInString = jsonMapper.writeValueAsString(o);
				log4j.info(jsonInString);

				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/postAddressBook";
				Map<String, String> params = new HashMap<String, String>();
				HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
						String.class, params);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					ObjectMapper mapper = new ObjectMapper();
					SupplierJdeDTO response = mapper.readValue(body, SupplierJdeDTO.class);
					//log4j.info("Guardado:" + response.getAddresNumber());
					System.out.println("Guardado:" + response.getAddresNumber());
					return response;
				}else {
					return null;
				}
			}
			return null;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
  }
  
  @SuppressWarnings("unused")
public SupplierJdeDTO disableSupplier(SupplierJdeDTO o) {
		String resp = "";
		try {
			if (o != null) {
				ObjectMapper jsonMapper = new ObjectMapper();
				String jsonInString = jsonMapper.writeValueAsString(o);
				log4j.info(jsonInString);

				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/disableSupplier";
				Map<String, String> params = new HashMap<String, String>();
				HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
				RestTemplate restTemplate = new RestTemplate();
				restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
				
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
						String.class, params);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					ObjectMapper mapper = new ObjectMapper();
					SupplierJdeDTO response = mapper.readValue(body, SupplierJdeDTO.class);
					//log4j.info("Guardado:" + response.getAddresNumber());
					System.out.println("Guardado:" + response.getAddresNumber());
					return response;
				}else {
					return null;
				}
			}
			return null;
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return null;
		}
  }

  public void sendTestEmail() {
	log4j.info("Inicio prueba email:");
    List<PurchaseOrderPayment> all = this.purchaseOrderService.getAll();
    for (PurchaseOrderPayment o : all)
      this.emailService.sendEmailPagos("TEST", "PRUEBA", "javila@smartech.com.mx", o); 
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
public String sendHttpRequest(String url, String jsonInString, HttpMethod method) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Accept", "application/json");
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.setAcceptCharset(Arrays.asList(new Charset[] { Charset.forName("UTF-8") }));
    Map<String, String> params = new HashMap<>();
    HttpEntity<?> httpEntity = null;
    if ("".equals(jsonInString)) {
      httpEntity = new HttpEntity((MultiValueMap)httpHeaders);
    } else {
      httpEntity = new HttpEntity(jsonInString, (MultiValueMap)httpHeaders);
    } 
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    ResponseEntity<String> responseEntity = restTemplate.exchange(url, method, httpEntity, String.class, params);
    HttpStatus statusCode = responseEntity.getStatusCode();
    if (statusCode.value() == 200)
      return (String)responseEntity.getBody(); 
    return null;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public void getNewAddressBook() {
	log4j.info("Inicio carga nuevos Proveedores:" + new Date());
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Accept", "application/json");
      String url = "http://localhost:8081/supplierWebPortalRestHame/newAddressBook";
      Map<String, String> params = new HashMap<>();
      HttpEntity<?> httpEntity = new HttpEntity((MultiValueMap)httpHeaders);
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081/supplierWebPortalRestHame/newAddressBook", HttpMethod.GET, httpEntity, String.class, 
          params);
      HttpStatus statusCode = responseEntity.getStatusCode();
      if (statusCode.value() == 200) {
        String body = (String)responseEntity.getBody();
        if (body != null) {
          ObjectMapper mapper = new ObjectMapper();
          AddressBookLayot[] response = (AddressBookLayot[])mapper.readValue(body, AddressBookLayot[].class);
          List<AddressBookLayot> objList = Arrays.asList(response);
          List<Supplier> saveList = new ArrayList<>();
          List<Users> users = new ArrayList<>();
          UDC userRole = this.udcService.searchBySystemAndKey("ROLES", "SUPPLIER");
          UDC userType = this.udcService.searchBySystemAndKey("USERTYPE", "SUPPLIER");
          List<String> updateList = new ArrayList<>();
          if (!objList.isEmpty()) {
            for (AddressBookLayot o : objList) {
              Supplier s = null;
              String supplierAddressNumber = String.valueOf(o.getABAN8());
              Supplier currentSup = this.supplierService.searchByAddressNumber(supplierAddressNumber);
              if (currentSup != null) {
                s = currentSup;
                s.setApprovalNotes("ACTUALIZACION");
              } else {
                s = new Supplier();
                s.setApprovalNotes("REPLICACION");
              } 
              s.setAddresNumber(String.valueOf(o.getABAN8()));
              s.setName(o.getABALPH().trim());
              s.setRazonSocial(o.getABALPH().trim());
              s.setCalleNumero(o.getALADD1().trim());
              s.setCodigoPostal(o.getALADDZ().trim());
              s.setColonia(o.getALADD2().trim());
              s.setDelegacionMnicipio(o.getALADD3().trim());
              s.setEstado(o.getALADD4().trim());
              s.setCountry(o.getALCTR().trim());
              s.setCategoriaJDE(o.getABAT1().trim());
              s.setCategorias(o.getA6APC().trim());
              s.setEmailComprador(o.getPPANBY().trim());
              s.setCurrencyCode(o.getA6CRRP().trim());
              s.setCuentaClabe(o.getAYRLN().trim());
              s.setDiasCredito(o.getA6TRAP().trim());
              String tps = o.getABAC12().trim();
              s.setEmailContactoCalidad(o.getWWMLN7().trim());
              s.setEmailContactoCxC(o.getWWMLN5().trim());
              s.setEmailContactoVentas(o.getWWMLN3().trim());
              s.setEmailContactoPedidos(o.getWWMLN1().trim());
              if ("PUB".equals(tps) || "ROY".equals(tps)) {
                if (o.getWWMLN5() != null && !"".equals(o.getWWMLN5().trim()))
                  s.setEmailContactoPedidos(String.valueOf(s.getEmailSupplier()) + "," + o.getWWMLN5().trim()); 
                if (o.getWWMLN3() != null && !"".equals(o.getWWMLN3().trim()))
                  s.setEmailContactoPedidos(String.valueOf(s.getEmailSupplier()) + "," + o.getWWMLN3().trim()); 
                if (o.getWWMLN7() != null && !"".equals(o.getWWMLN7().trim()))
                  s.setEmailContactoPedidos(String.valueOf(s.getEmailSupplier()) + "," + o.getWWMLN7().trim()); 
              } 
              s.setFormaPago(o.getA6PYIN().trim());
              s.setNombreBanco(o.getAYSWFT().trim());
              s.setNombreContactoCalidad(o.getWWMLN8().trim());
              s.setNombreContactoCxC(o.getWWMLN6());
              s.setNombreContactoPedidos(o.getWWMLN2().trim());
              s.setNombreContactoVentas(o.getWWMLN4().trim());
              s.setFisicaMoral(o.getABTAXC().trim());
              s.setRfc(o.getABTAX().trim());
              s.setTaxId(o.getABTX2().trim());
              s.setTaxRate(o.getA6TXA2().trim());
              s.setExplCode1(o.getA6EXR2().trim());
              s.setTasaIva(String.valueOf(o.getPPIVA()).trim());
              s.setTelefonoContactoCalidad(o.getWPPH4().trim());
              s.setTelefonoContactoCxC(o.getWPPH3());
              s.setTelefonoContactoPedidos(o.getWPPH1());
              s.setTelefonoContactoVentas(o.getWPPH2());
              s.setPaymentMethod(o.getPPPYMT().trim());
              s.setTipoProductoServicio(o.getABAC12().trim());
              s.setEmail(o.getWWMLN1().trim());
              s.setAcceptOpenOrder(false);
              s.setApprovalStatus("APROBADO");
              s.setApprovalStep("");
              s.setAutomaticEmail("");
              s.setCuentaBancaria("");
              s.setCurrentApprover("");
              s.setDiasCreditoActual("");
              s.setDiasCreditoAnterior("");
              s.setDireccionCentroDistribucion("");
              s.setDireccionPlanta("");
              s.setFechaAprobacion(new Date());
              s.setFechaSolicitud(new Date());
              s.setFileList("");
              s.setInvException("N");
              s.setNextApprover("");
              s.setObservaciones("");
              s.setPuestoCalidad("");
              s.setRegiones("");
              s.setRejectNotes("");
              s.setRiesgoCategoria("");
              s.setSteps(0);
              s.setTicketId(Long.valueOf(0L));
              s.setTipoMovimiento("A");
              s.setSupplierType("");
              s.setCompradorAsignado("");
              s.setDiasCreditoActual("0");
              s.setDiasCreditoAnterior("0");
              if (currentSup != null) {
                Users usr = this.usersService.getByUserName(supplierAddressNumber);
                if (usr != null) {
                  if (s.getCategoriaJDE().contains("X")) {
                    usr.setEnabled(false);
                  } else {
                    usr.setEnabled(true);
                  } 
                  usr.setEmail(s.getEmail());
                  users.add(usr);
                } 
              } else {
                Users usr = new Users();
                usr.setId(0);
                usr.setUserName(s.getAddresNumber());
                usr.setEnabled(true);
                usr.setEmail(s.getEmail());
                usr.setName(s.getName());
                usr.setRole(userRole.getStrValue1());
                usr.setUserRole(userRole);
                usr.setUserType(userType);
                String tempPass = getAlphaNumericString(8);
                String encodePass = Base64.getEncoder().encodeToString(tempPass.trim().getBytes());
                encodePass = "==a20$" + encodePass;
                usr.setPassword(encodePass);
                users.add(usr);
              } 
              saveList.add(s);
              updateList.add(String.valueOf(o.getABAN8()));
            } 
            this.supplierService.saveSuppliers(saveList);
            for (Users s : users) {
              String emailRecipient = s.getEmail();
              if (s.getId() == 0) {
                String pass = s.getPassword();
                pass = pass.replace("==a20$", "");
                byte[] decodedBytes = Base64.getDecoder().decode(pass);
                String decodedPass = new String(decodedBytes);
                String credentials = "Usuario: " + s.getUserName() + "<br />Contrase" + decodedPass + "<br />&nbsp; url: " + "http://localhost:8081/supplierWebPortalSepasa/";
                EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
                emailAsyncSup.setProperties("SEPASA - Notificacidel Portal de Proveedores. No Responder.", this.stringUtils.prepareEmailContent("Estimado Proveedor. <br /><br />Usted ha sido aprobado para utilizar el Portal de Proveedores de la compaUMG. A continuaciencontrarsus credenciales temporales de acceso.<br /><br />" + credentials), emailRecipient);
                emailAsyncSup.setMailSender(this.mailSenderObj);
                Thread emailThreadSup = new Thread(emailAsyncSup);
                emailThreadSup.start();
              } 
            } 
            this.usersService.saveUsersList(users);
            if (updateList.size() > 0) {
              ObjectMapper jsonMapper = new ObjectMapper();
              String jsonInString = jsonMapper.writeValueAsString(updateList);
              String resp = sendHttpRequest("http://localhost:8081/supplierWebPortalRestHame/updateNewAddressBook", jsonInString, HttpMethod.POST);
              log4j.info("Actualizado:" + resp);
            } 
          } 
        } 
      } 
    } catch (Exception e) {
      log4j.error("Exception" , e);	
      e.printStackTrace();
    } 
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public int getAddressBookNextNumber() {
	 log4j.info("Busca addressBook:");
    try {
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Accept", "application/json");
      String url = "http://localhost:8081/supplierWebPortalRestHame/getAddressBookNextNumber";
      Map<String, String> params = new HashMap<>();
      HttpEntity<?> httpEntity = new HttpEntity((MultiValueMap)httpHeaders);
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> responseEntity = restTemplate.exchange("http://localhost:8081/supplierWebPortalRestHame/getAddressBookNextNumber", HttpMethod.GET, httpEntity, String.class, 
          params);
      HttpStatus statusCode = responseEntity.getStatusCode();
      if (statusCode.value() == 200) {
        String body = (String)responseEntity.getBody();
        if (body != null) {
          ObjectMapper mapper = new ObjectMapper();
          Integer result = (Integer)mapper.readValue(body, Integer.class);
          if (result != null)
            return 1000; 
        } 
      } 
    } catch (Exception e) {
      log4j.error("Exception" , e);
      e.printStackTrace();
    } 
    return 0;
  }
  
  
  private String getAlphaNumericString(int n) {
    String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      int index = 
        (int)(AlphaNumericString.length() * 
        Math.random());
      sb.append(AlphaNumericString
          .charAt(index));
    } 
    return sb.toString();
  }
  
	//@Scheduled(fixedDelay = 4200000, initialDelay = 10000)
	//@Scheduled(cron = "0 45 6,9,12,16,19 * * ?")
	public void getDisableOrderReceipts() {
		try {
			boolean isProcessOk = true;
			List<Receipt> allReceiptList = new ArrayList<Receipt>();
			List<Receipt> allJdeReceiptList = new ArrayList<Receipt>();
			log4j.info("DESHABILITA RECIBOS...");
			
			  for(int i=0; i<=200; i++) {
				  int start = i*500;
				  List<Receipt> receiptList = purchaseOrderService.getOpenOrderReceipts(start, 500);
				  
					if (receiptList != null) {
						if (!receiptList.isEmpty()) {
							allReceiptList.addAll(receiptList);
							
							List<String> coList = new ArrayList<String>();
							List<String> supList = new ArrayList<String>();
							List<String> poList = new ArrayList<String>();
							List<String> otList = new ArrayList<String>();
							List<String> rList = new ArrayList<String>();
							List<String> rtList = new ArrayList<String>();
							
							for(Receipt r : receiptList) {
								String company = "'"+ r.getOrderCompany().trim() + "'";
								String supplier = r.getAddressNumber().trim();
								String orderNumber = String.valueOf(r.getOrderNumber());
								String orderType = "'" + r.getOrderType().trim() + "'";
								String receiptNumber = String.valueOf(r.getDocumentNumber());
								String receiptType = "'" + r.getDocumentType() + "'";
								
								if(!coList.contains(company)) {
									coList.add(company);
								}

								if(!supList.contains(supplier)) {
									supList.add(supplier);
								}
								
								if(!poList.contains(orderNumber)) {
									poList.add(orderNumber);
								}
								
								if(!otList.contains(orderType)) {
									otList.add(orderType);
								}
								
								if(!rList.contains(receiptNumber)) {
									rList.add(receiptNumber);
								}
								
								if(!rtList.contains(receiptType)) {
									rtList.add(receiptType);
								}
							}
							
							String coStringList = String.join(",", coList);
							String supStringList = String.join(",", supList);
							String poStringList = String.join(",", poList);
							String otStringList = String.join(",", otList);
							String rStringList = String.join(",", rList);
							String rtStringList = String.join(",", rtList);

			 				HttpHeaders httpHeaders = new HttpHeaders();
							httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			 			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			 			    map.add("coList", coStringList);
							map.add("supList", supStringList);
							map.add("poList", poStringList);
							map.add("otList", otStringList);
							map.add("rList", rStringList);
							map.add("rtList", rtStringList);
							
			 				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/poEnableReceipts";						
							HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);			 				
			 				RestTemplate restTemplate = new RestTemplate();
							ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
							HttpStatus statusCode = responseEntity.getStatusCode();

			 				if (statusCode.value() == 200) {
			 					String body = responseEntity.getBody();
			 					if (body != null) {
			 						ObjectMapper mapper = new ObjectMapper();
			 						Receipt[] response = mapper.readValue(body, Receipt[].class);
			 						List<Receipt> objList = Arrays.asList(response);

			 						if (objList != null && !objList.isEmpty()) {			 							
			 							allJdeReceiptList.addAll(objList);
			 						}
			 					}
			 				} else {
			 					log4j.info("CONSULTA NO EXITOSA.");
			 					isProcessOk = false;
			 					break;
			 				}
						}
					}
			  }
			  
			  log4j.info("REGISTROS PORTAL:" + allReceiptList.size());
			  log4j.info("REGISTROS JDE:" + allJdeReceiptList.size());
			  
				if(isProcessOk) {
					List<Receipt> notFoundList = new ArrayList<Receipt>();					
					for(Receipt receipt : allReceiptList) {
						boolean isReceiptExists = false;
						for(Receipt receiptJDE : allJdeReceiptList) {
							if(receipt.getOrderCompany().trim().equals(receiptJDE.getOrderCompany().trim()) 
									&& receipt.getAddressNumber().trim().equals(receiptJDE.getAddressNumber().trim())
									&& receipt.getOrderNumber() == receiptJDE.getOrderNumber()
									&& receipt.getOrderType().trim().equals(receiptJDE.getOrderType().trim())
									&& receipt.getDocumentNumber() == receiptJDE.getDocumentNumber()
									&& receipt.getDocumentType().trim().equals(receiptJDE.getDocumentType().trim())) {
								isReceiptExists = true;
								break;
							}
						}
						
						if(!isReceiptExists) {
							receipt.setStatus(AppConstants.STATUS_OC_CANCEL);
							notFoundList.add(receipt);
						}
					}
					
					if(!notFoundList.isEmpty()) {
						purchaseOrderService.updateReceipts(notFoundList);		 							 							
						log4j.info("Recibos deshabilitados:" + notFoundList.size());
					}	
				}

		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
	}
 	
	//@Scheduled(fixedDelay = 4200000, initialDelay = 10000)
	//@Scheduled(cron = "0 30 7 * * ?")
	public void getUpdateExchangeRate() {
		try {
			log4j.info("ACTUALIZACIÓN DE TIPO DE CAMBIO...");
			String currencyCode = "";
			int startDate = 0;
			
			UDC erUDC = udcDao.searchBySystemAndKey("SCHEDULER", "EXCHANGERATE");			
			if(erUDC != null) {
				currencyCode = erUDC.getStrValue1();
				if(erUDC.getDateValue() != null) {	
					try {
						startDate = Integer.valueOf(JdeJavaJulianDateTools.Methods.getJulianDate(erUDC.getDateValue()));
					} catch (Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();						
					}					
				}
				
				if(startDate == 0) {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.DATE, -1);
					startDate = Integer.valueOf(JdeJavaJulianDateTools.Methods.getJulianDate(c.getTime()));
				}
				
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			    MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			    map.add("currencyCode", currencyCode);
				map.add("startDate", String.valueOf(startDate));
				
				String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/getExchangeRate";						
				HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(map, httpHeaders);			 				
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,String.class);
				HttpStatus statusCode = responseEntity.getStatusCode();

				if (statusCode.value() == 200) {
					String body = responseEntity.getBody();
					if (body != null) {
						ObjectMapper mapper = new ObjectMapper();
						ExchangeRate[] response = mapper.readValue(body, ExchangeRate[].class);
						List<ExchangeRate> objList = Arrays.asList(response);

						if (objList != null && !objList.isEmpty()) {
							List<ExchangeRate> updatedRecords = exchangeRateService.saveMultipleExchangeRate(objList);
							if(updatedRecords != null) {
								log4j.info("Nuevos registros Tipo de Cambio:" + updatedRecords.size());
							}							
						}
					}
					
					erUDC.setDateValue(new Date());
					udcService.update(erUDC, new Date(), "system");
				} else {
					log4j.info("TIPO DE CAMBIO - CONSULTA NO EXITOSA.");
				}
			}
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
	}
	
    //@Scheduled(fixedDelay = 6000000, initialDelay = 10000)
	public void restoreInvoice() {
		List<InvoiceRequestDTO> list = new ArrayList<InvoiceRequestDTO>();
		InvoiceRequestDTO dto = null;
		String[] array = new String[]{};
//		String[] array = new String[]{
//		"84577,	OP,	2157619, 3036B345-A840-4DED-B33B-6BC2DF7E0D68"};		
//				"31926,	OP,	1987276, 73a93d9f-bf2d-453e-a019-ce237119298b",
//				"31926,	OP,	1987276, 73101C31-EB4A-463A-938D-BB1CF7F5CAA3",
//				"32173,	OP,	1987276, 8C681CEB-ABE9-4F92-81A3-B45F41A280FD",
//				"31681,	OP,	1987276, ba156a5f-7d15-4a02-add1-1b8d98da5a35"};		

		for(String record : array) {
			dto = new InvoiceRequestDTO();
			String[] items = record.split(",");
			dto.setDocumentNumber(Integer.valueOf(items[0].trim()).intValue());
			dto.setDocumentType(items[1].trim());
			dto.setAddressBook(items[2].trim());
			dto.setUuid(items[3].trim());
			list.add(dto);
		}

		documentsService.restoreInvoice(list);
	}
	
    //@Scheduled(fixedDelay = 6000000, initialDelay = 3000)
	public void restoreInvoiceFile() {
		String[] array = new String[]{};
		/*
		String[] array = new String[]{
				"078B5ACF-C7B2-4319-9A98-99F0CF89B0A3",
				"13e3dedd-466d-4219-ab18-bd4e646896d1",
				"2083C42B-919B-4253-B258-09AD1E96EC90",
				"316DD8FE-058B-4AEC-B54B-281D8747ED0E",
				"32B2282A-D107-48B1-BC76-5620302929F1",
				"34a1fde8-0585-4dc2-8fe2-c2cee68e2819",
				"44322250-C918-4C70-9659-CC76D5723AEA",
				"4e20fa56-463f-417f-9611-d9c0fa6d06bc",
				"51D9170B-66DC-46B8-A322-082A4C8D116D",
				"549E0F72-A99B-4E2D-B217-4013067577A8",
				"6e0c532b-f794-40ca-8dc7-5a9a5ae4a213",
				"704EF12C-D6B6-401F-B040-1ECDA0763331",
				"7E27E3FA-11A5-4ACF-91B1-14FA724739E1",
				"80D02484-E421-49DA-9A11-8F926ADB0B2C",
				"85294019-D62A-4C71-A127-944C98397864",
				"857DCF73-976E-4639-9258-5231A806FEFC",
				"8902A8BC-B157-4F8B-8A63-FCB1F317B18F",
				"8D1894DA-6B64-4C3C-A608-8BD664050731",
				"a1c62bf2-b16c-4527-b0e5-802887141442",
				"A2D11520-DB46-4015-8566-C9046AD4CDCC",
				"A60ED4E1-E7F2-4132-8376-0D1874F0C8E6",
				"B87551C2-E94F-4281-9AA1-A9CDBCC56AB1",
				"B9AA7DB4-FB8E-4C3B-A2C7-D5685DE49005",
				"C73B1F49-69A2-474C-95BF-795CB73EE3A1",
				"DEE6F71D-1D37-4969-8730-C000373DF5A1",
				"1E21169A-07A0-11ED-92E7-D3F684081F61",
				"43bf8895-0163-4130-8a73-caa4030207b7",
				"D4A9FAEE-9E97-4A6D-B79B-4CAC1BBDA9E9"};
		*/
		List<String> list = Arrays.asList(array);
		documentsService.restoreInvoiceFile(list);
	}
	
	//@Scheduled(fixedDelay = 6000000, initialDelay = 3000)
	//@Scheduled(cron = "0 10 1 * * ?")
	public void sendFilePdfToJDE() {
		String[] array = new String[0];
		List<UserDocumentDTO> lista = documentsService.getListFac("");
		List<UserDocumentDTO> listAduana = documentsService.getListFac("aduana");

		ArrayList<String> factToSend = new ArrayList<>();

		HashMap<String, String> listFilesPdf = getListFilesMiddleware();

		for (UserDocumentDTO docfac : lista) {
			if (listFilesPdf.get(docfac.getUuid().trim()) == null) {

				factToSend.add(docfac.getUuid().trim());
			}
		}

		for (UserDocumentDTO docfac : listAduana) {
			if (listFilesPdf.get(docfac.getUuid().trim()) == null) {

				factToSend.add(docfac.getUuid().trim());
			}
		}

		List<String> list =factToSend;
		documentsService.restoreInvoiceFile(list);
	}
	
	
	
	
    //@Scheduled(fixedDelay = 6000000, initialDelay = 10000)
	public void restoreForeignInvoice() {
		List<InvoiceRequestDTO> list = new ArrayList<InvoiceRequestDTO>();
		InvoiceRequestDTO dto = null;
		String[] array = new String[]{};		
//		String[] array = new String[]{"8929,OP, 2193585,02193585-2020-1105-2152-0322200008929"};

		for(String record : array) {
			dto = new InvoiceRequestDTO();
			String[] items = record.split(",");
			dto.setDocumentNumber(Integer.valueOf(items[0].trim()).intValue());
			dto.setDocumentType(items[1].trim());
			dto.setAddressBook(items[2].trim());
			dto.setUuid(items[3].trim());
			list.add(dto);
		}

		documentsService.restoreForeignInvoice(list);
	}
	
	//@Scheduled(cron = "0 0 1 * * ?")
	public void reloadJde() {
		JSONObject json = new JSONObject();
		try{
        	List<LogDataJEdwars> logs=loggerJEdwars.getLogDataToSend();
        	for (LogDataJEdwars logDataJEdwars : logs) {
        		sendJournalEntriesReload(logDataJEdwars);
			}
        
        }catch(Exception e){
        	log4j.error("Exception" , e);
        	e.printStackTrace();
        	json.put("success", true);
            json.put("message", "Ha ocurrido un error inesperado: " +e.getMessage());
        }
//		return json.toString();
		return;
	}
	
	 @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	  public String sendJournalEntriesReload(LogDataJEdwars o) {
	      SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy");
	      String resp = "";
	      String jsonInString="";
	      String url="";
	      try {
	        if (o != null) {
	           jsonInString = o.getDataSend();
	          HttpHeaders httpHeaders = new HttpHeaders();
	          httpHeaders.set("Accept",MediaType.APPLICATION_JSON_VALUE);
	          httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	          url = o.getUrl();
	          Map<String, String> params = new HashMap<>();
	          HttpEntity<?> httpEntity = new HttpEntity(jsonInString, (MultiValueMap)httpHeaders);
	          RestTemplate restTemplate = new RestTemplate();
	          ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,  String.class, params);
	          HttpStatus statusCode = responseEntity.getStatusCode();
	          if (statusCode.value() == 200) {	          	
	            String body = (String)responseEntity.getBody();
	            ObjectMapper mapper = new ObjectMapper();
//	            BatchJournalDTO response = (BatchJournalDTO)mapper.readValue(body, BatchJournalDTO.class);
	            log4j.info("Guardado:");
	            o.setStatus(AppConstants.LOGGER_JEDWARS_SEND);
		    	  o.setMesage(body);
		      	loggerJEdwars.putUpdate(o);
	          } 
	        } 
	        return resp;
	      } catch (Exception e) {
	    	  log4j.error("Exception" , e);
//	      	logger.log(AppConstants.LOGGER_JEDWARS, "Error :"+e.getMessage()+">>>"+ StringUtils.getString(e));
	    	  o.setStatus(AppConstants.LOGGER_JEDWARS_ERROR);
	    	  o.setMesage( "Error :"+e.getMessage()+">>>"+ StringUtils.getString(e));
	      	loggerJEdwars.putUpdate(o);
	        e.printStackTrace();
	        return null;
	      } 
	    }
	 
	 
	 
	 @SuppressWarnings({ "unused", "unchecked" })
	public List<String> getListFactJde(String uuidList,String origen) {
		
			try {
				
					ObjectMapper jsonMapper = new ObjectMapper();
			        String jsonInString = jsonMapper.writeValueAsString(uuidList.split(","));

					HttpHeaders httpHeaders = new HttpHeaders();
					httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
					final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/getInvoiceByList?origen="+origen;
					Map<String, String> params = new HashMap<String, String>();
					HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
					RestTemplate restTemplate = new RestTemplate();
					restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
					
					ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
							String.class, params);
					HttpStatus statusCode = responseEntity.getStatusCode();

					if (statusCode.value() == 200) {
						String body = responseEntity.getBody();
						ObjectMapper mapper = new ObjectMapper();
						List<String> response = mapper.readValue(body, List.class);
						
						return response;
					}else {
						return null;
					}
				
			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return null;
			}
	  }
	 
	 	 @SuppressWarnings({ "unused", "unchecked" })
	public HashMap<String, String> getListFilesMiddleware() { 
		
			try {
				
					ObjectMapper jsonMapper = new ObjectMapper();
			        String jsonInString = jsonMapper.writeValueAsString("{}");

					HttpHeaders httpHeaders = new HttpHeaders();
					httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE); 
					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
					final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/getFilesfromDisk";
					Map<String, String> params = new HashMap<String, String>();
					HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
					RestTemplate restTemplate = new RestTemplate();
					restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
					
					ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
							String.class, params);
					HttpStatus statusCode = responseEntity.getStatusCode();

					if (statusCode.value() == 200) {
						String body = responseEntity.getBody();
						ObjectMapper mapper = new ObjectMapper();
						HashMap<String, String> response = mapper.readValue(body, HashMap.class);
						
						return response;
					}else {
						return null;
					}
				
			} catch (Exception e) {
				log4j.error("Exception" , e);
				e.printStackTrace();
				return null;
			}
	  }
	 
	 	//@Scheduled(cron = "0 40 12 2,14,27,30 * *")//Notifica los días 2, 27 y 30 de cada mes
	 	  public void sendPendingPaymentComplement() {
	 		  log4j.info("==================SendPendingPaymentComplement================");
	 			for(int i=0; i<=100; i++) {
	 				int start = i*100;
	 				int limit = 100;
	 				
	 				try {
	 					List<SupplierDTO> supDtoList = supplierService.getList(start, limit);				
	 					if (!supDtoList.isEmpty()) {
	 						List<String> sList = new ArrayList<String>();
	 						for (SupplierDTO sdto : supDtoList) {
	 							if(sdto.getAddresNumber() != null && !"".equals(sdto.getAddresNumber().trim())){
	 								sList.add(sdto.getAddresNumber().trim());
	 								 
	 								Supplier s = supplierService.searchByAddressNumber(sdto.getAddresNumber().trim());							
	 								if (s != null && s.getCountry() != null && "MX".equals(s.getCountry().trim())) {
	 									
	 									
	 									List<PurchaseOrder> list = purchaseOrderService.getPendingPaymentOrders(0,sdto.getAddresNumber().trim(), "");
	 									if (list != null && !list.isEmpty()) {
	 										List<String> uuidList = new ArrayList<String>();
	 										for (PurchaseOrder p : list) {
	 											String invoiceDetail = "<tr>"
	 													.concat("<td>UUID: ").concat(p.getInvoiceUuid()).concat("</td>")
	 													.concat("<td>Factura: ").concat(p.getInvoiceNumber()).concat("</td>")
	 													.concat("</tr>");

	 											if (!uuidList.contains(invoiceDetail)) {
	 												uuidList.add(invoiceDetail);
	 											}
	 										}

	 										if (!uuidList.isEmpty()) {
	 											//Notificación Proveedor
	 											String emailRecipient = (s.getEmailSupplier());
	 											String esMessage = "<table>" + String.join("", uuidList) + "</table>";
	 											String enMessage = "<table>" + String.join("", uuidList).replace("<td>Factura:", "<td>Invoice:") + "</table>";
	 											String emailContent = AppConstants.EMAIL_PAYMENT_COMPL_NOTIF_CONTENT;
	 											emailContent = emailContent.replace("_INVOICES_ES_", esMessage);
	 											emailContent = emailContent.replace("_INVOICES_EN_", enMessage);										
	 											
	 											EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	 											emailAsyncSup.setProperties(AppConstants.EMAIL_PAYMENT_COMPL_NOTIF,
	 													stringUtils.prepareEmailContent(emailContent), emailRecipient);
	 											emailAsyncSup.setMailSender(mailSenderObj);
	 											emailAsyncSup.setAdditionalReference(udcDao, "");										
	 											Thread emailThreadSup = new Thread(emailAsyncSup);
	 											emailThreadSup.start();
	 										}
	 									}
	 								}
	 							}
	 						}
	 						log4j.info("Proveedores procesados: " + String.join(",", sList)  + ".");
	 					}
	 				} catch (Exception e) {
	 					log4j.error("Exception" , e);
	 					e.printStackTrace();
	 				}
	 			}
	 		  log4j.info("================== Ends SendPendingPaymentComplement ==================");
	 	  }

	 	 //@Scheduled(cron = "0 40 12 2,14,27,30 * *")//Notifica los días 2, 27 y 30 de cada mes
	 	  public void sendPendingReceipInvoice() {
	 		  log4j.info("==================sendPendingReceipInvoice================");
	 			for(int i=0; i<=100; i++) {
	 				int start = i*100;
	 				int limit = 100;
	 				
	 				try {
	 					List<SupplierDTO> supDtoList = supplierService.getList(start, limit);				
	 					if (!supDtoList.isEmpty()) {
	 						List<String> sList = new ArrayList<String>();
	 						for (SupplierDTO sdto : supDtoList) {
	 							if(sdto.getAddresNumber() != null && !"".equals(sdto.getAddresNumber().trim())){
	 								sList.add(sdto.getAddresNumber().trim());
	 								
	 								Supplier s = supplierService.searchByAddressNumber(sdto.getAddresNumber().trim());							
	 								if (s != null && s.getCountry() != null && "MX".equals(s.getCountry().trim())) {
	 									
	 									List<Receipt> list = purchaseOrderService.getOrderReceiptsPendFact(sdto.getAddresNumber().trim());
	 									if (list != null && !list.isEmpty()) {
	 										List<String> uuidList = new ArrayList<String>();
	 										  DecimalFormat formatoPesos = new DecimalFormat("###,###.00");

	 										for (Receipt p : list) {
	 											String invoiceDetail = "<tr>"
	 													.concat("<td>Núm. Orden:  ").concat(p.getOrderNumber()+"").concat("</td>")
	 													.concat("<td>Núm. Recibo: ").concat(p.getDocumentNumber()+"").concat("</td>")
	 													.concat("<td>Importe : $").concat(formatoPesos.format(p.getForeignAmountReceived()>0?p.getForeignAmountReceived():p.getAmountReceived())).concat(" "+p.getCurrencyCode()+"</td>")
	 													.concat("</tr>");

	 											if (!uuidList.contains(invoiceDetail)) {
	 												uuidList.add(invoiceDetail);
	 											}
	 										}

	 										if (!uuidList.isEmpty()) {
	 											//Notificación Proveedor 
	 											String emailRecipient = (s.getEmailSupplier());
	 											String esMessage = "<table>" + String.join("", uuidList) + "</table>";
	 											String enMessage = "<table>" + String.join("", uuidList).replace("Núm. Orden", "Num. Order").replace("Núm. Recibo", "GR number").replace("Importe", "Amount") + "</table>";
	 											String emailContent = AppConstants.EMAIL_FAC_NOTIF_CONTENT.replace("_supplier_", s.getAddresNumber()+" - "+s.getName());
	 											emailContent = emailContent.replace("_INVOICES_ES_", esMessage);
	 											emailContent = emailContent.replace("_INVOICES_EN_", enMessage);										
	 											
	 											EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
	 											emailAsyncSup.setProperties(AppConstants.EMAI_FAC_NOTIF,
	 													stringUtils.prepareEmailContent(emailContent), emailRecipient);
	 											emailAsyncSup.setMailSender(mailSenderObj);
	 											emailAsyncSup.setAdditionalReference(udcDao, "");										
	 											Thread emailThreadSup = new Thread(emailAsyncSup);
	 											emailThreadSup.start();
	 										}
	 									}
	 								}
	 							}
	 						}
	 						log4j.info("Proveedores procesados: " + String.join(",", sList)  + ".");
	 					}
	 				} catch (Exception e) {
	 					log4j.error("Exception" , e);
	 					e.printStackTrace();
	 				}
	 			}
	 		  log4j.info("================== Ends sendPendingReceipInvoice ==================");
	 	  }

	 	  public List<PurchaseOrderGridDTO> getEstPmtDate(List<PurchaseOrderGridDTO> o) {
	 			try {
	 				if (o != null) {
	 					OrderGridWrapper wrapper = new OrderGridWrapper();
	 					wrapper.setOrders(o);
	 					ObjectMapper jsonMapper = new ObjectMapper();
	 					String jsonInString = jsonMapper.writeValueAsString(wrapper);
	 					
	 					HttpHeaders httpHeaders = new HttpHeaders();
	 					httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
	 					httpHeaders.setContentType(MediaType.APPLICATION_JSON);
	 					final String url = AppConstants.URL_HOST + "/supplierWebPortalRestHame/getEstPmtDate";
	 					Map<String, String> params = new HashMap<String, String>();
	 					HttpEntity<?> httpEntity = new HttpEntity<>(jsonInString, httpHeaders);
	 					RestTemplate restTemplate = new RestTemplate();
	 					restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
	 					
	 					ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
	 							String.class, params);
	 					HttpStatus statusCode = responseEntity.getStatusCode();

	 					if (statusCode.value() == 200) {
	 	 					String body = responseEntity.getBody();
	 	 					if (body != null) {
	 	 						ObjectMapper mapper = new ObjectMapper();
	 	 						PurchaseOrderGridDTO[] response = mapper.readValue(body, PurchaseOrderGridDTO[].class);
	 	 						List<PurchaseOrderGridDTO> objList = Arrays.asList(response);
	 	 						if (!objList.isEmpty()) {
	 	 							return objList;
	 	 						}
	 	 					}
	 	 				}else {
	 						return null;
	 					}
	 				}
	 				return null;
	 			} catch (HttpServerErrorException e) {
	 			    System.err.println("HTTP Status: " + e.getStatusCode());
	 			    System.err.println("Response Body: " + e.getResponseBodyAsString());
	 				e.printStackTrace();
	 				return null;
	 			} catch (Exception e) {
	 				log4j.error("Exception" , e);
	 				e.printStackTrace();
	 				return null;
	 			}
	 	  }
	 	  
	 	  
}
