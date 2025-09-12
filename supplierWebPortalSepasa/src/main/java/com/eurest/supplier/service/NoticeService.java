package com.eurest.supplier.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dao.NoticeDao;
import com.eurest.supplier.dto.NoticeDTO;
import com.eurest.supplier.dto.NoticeDetailDTO;
import com.eurest.supplier.model.Notice;
import com.eurest.supplier.model.NoticeDetail;
import com.eurest.supplier.model.NoticeDocument;
import com.eurest.supplier.model.Supplier;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.FileUploadBean;

@Service("noticeService")
public class NoticeService {
	
	@Autowired
	NoticeDao noticeDao;
	
	@Autowired
	SupplierService supplierService;
	
	@Autowired
	private JavaMailSender mailSenderObj;
	
	@Autowired
	UdcService udcService;

	public String save(Notice o) {
		try {
			String[] suppliers = o.getSuppliersNotice();  
			
			List<NoticeDetail> detail = new ArrayList<NoticeDetail>();
			
			File file = new File("Notice_"+o.getIdNotice()+".txt");

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
 
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(o.getNoticeTitle());
			bw.newLine();

            // Write in file
			
			for (int i = 0; i< suppliers.length; i++){  
				NoticeDetail x = new NoticeDetail();
				
				x.setIdNotice(o.getIdNotice());
				x.setAddresNumber(suppliers[i]);
				x.setStatus("PENDIENTE");
				x.setRequired(o.getRequired());
				x.setAttachment(o.getDocSupplier());
				x.setEnabled(true);
				
				Supplier sup = supplierService.searchByAddressNumber(x.getAddresNumber());
				
				if(sup!=null) {
					x.setRazonSocial(sup.getRazonSocial());
					
					if(o.getEmailNotif()) {
						NoticeDocument doc = noticeDao.noticeDocByIdNotice(o.getIdNotice());
						
						if(doc != null) {
							bw.write(sup.getAddresNumber() + " | " + sup.getRazonSocial() + " | " + sup.getEmailSupplier());
							bw.newLine();
							this.sendEmailWithAttach(sup.getEmailSupplier(), doc, o.getNoticeTitle());
						}
					}
				}
				
				detail.add(x);
			}  
			
			o.setFechaCreacion(new Date());

            // Close connection
            bw.close();
			
            /*byte[] bytes = Files.readAllBytes(file.toPath());
            
            mailSenderObj.send(new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper mimeMsgHelperObj = new MimeMessageHelper(mimeMessage, true, "UTF-8");				
					mimeMsgHelperObj.setTo("mgarcia@smartech.com.mx");
					mimeMsgHelperObj.setFrom(AppConstants.EMAIL_FROM);				
					mimeMsgHelperObj.setText("");
					mimeMsgHelperObj.setSubject("SEPASA | Aviso");
					
					mimeMsgHelperObj.addAttachment(file.getName(), new ByteArrayResource(bytes), "text/plain");
				}
			});*/
            
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.setTime(o.getNoticeFromDate());
			calendarFrom.add(Calendar.DAY_OF_MONTH, 1);
			o.setNoticeFromDate(calendarFrom.getTime());
			/*
			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(o.getNoticeToDate());
			calendarTo.add(Calendar.DAY_OF_MONTH, 1);
			o.setNoticeToDate(calendarTo.getTime());*/
		    
			noticeDao.saveNotice(o);
			noticeDao.saveNoticeDetail(detail);
			
			this.sendEmailWithAttachToUDC(file, o.getNoticeTitle());
			
			return "Succ";
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
public List<String> exportExcelSupplier(FileUploadBean uploadItem) {
		
		Workbook workbook = null;
		Sheet sheet = null;
		List<String> suppList = new ArrayList<String>();
		
		try {
		    workbook = WorkbookFactory.create(uploadItem.getFile().getInputStream());
			sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			int rowNumber = 1;
			while(rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if(row.getRowNum() > 0) {
					rowNumber= rowNumber + 1;
					try {
						if(row.getCell(0)!= null) {
							int valueAN = row.getCell(0).getCellType();
							 
							if(valueAN == 0) {
								int addNum = (int) row.getCell(0).getNumericCellValue();
								addNum = (addNum*100)/100;
								suppList.add(String.valueOf(addNum));
							}else {
								if("".equals(row.getCell(0).getStringCellValue())) {
									continue;
								}
								int addNum = Integer.valueOf(row.getCell(0).getStringCellValue());
								addNum = (addNum*100)/100;
								suppList.add(String.valueOf(addNum));
							}
						}else {
							continue;
						}
					}catch(Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return suppList;
	}
	
	public JSONObject validateExcelSupplier(List<String> suppsExcel) {
		JSONObject json = new JSONObject();
		ArrayList<String> suppCount = new ArrayList<String>();
		try {
			for (int i=0;i<suppsExcel.size();i++) {
				Supplier o = supplierService.searchByAddressNumber(suppsExcel.get(i));
				
				if(o == null) {
					suppCount.add(suppsExcel.get(i));
				}
			}
			
			if(suppCount.size()>0) {
				String suppError = "";
				//String[] suppError = new String[suppCount.size()];
				
				for (int i=0;i<suppCount.size();i++) {
					suppError = suppError + suppCount.get(i) + ", ";
					//suppError[i] =suppCount.get(i);
				}
				
				json.put("success", true);
				json.put("suppliers", suppError);
				return json;
				
			}else {
				json.put("success", false); 
				json.put("suppliers", "");
				return json;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
	
	public NoticeDTO noticeActivesBySupp (String supp){
		try {
			List<NoticeDetail> notices = noticeDao.noticeActivesBySupp(supp);
			NoticeDTO notice = new NoticeDTO();
			Notice noticeHeader = new Notice();
			if(notices.size()>0) {
				for(NoticeDetail o : notices) {
					notice.setIdNotice(o.getIdNotice());
					break;
				}
			}else return null;
			
			noticeHeader = noticeDao.getNoticeById(notice.getIdNotice());
			
			if(noticeHeader != null) {
				notice.setId(String.valueOf(noticeHeader.getId()));
				notice.setNoticeTitle(noticeHeader.getNoticeTitle());
				notice.setDocSupplier(noticeHeader.getDocSupplier());
				notice.setRequired(noticeHeader.getRequired());
				
				NoticeDocument doc = noticeDao.noticeDocByIdNotice(notice.getIdNotice());
				
				if(doc != null) {
					notice.setDocNotice(Base64.getEncoder().encodeToString(doc.getContent()));
					return notice;
				}
			}
			return null;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<NoticeDetailDTO> statusSuppsNotice(String idNotice){
		try {
			List<NoticeDetail> suppliers = noticeDao.listSuppsOnNotice(idNotice);
			List<NoticeDetailDTO> sups = new ArrayList<NoticeDetailDTO>();
			
			for(NoticeDetail o : suppliers) {
				NoticeDetailDTO x = new NoticeDetailDTO();
				x.setAddresNumber(o.getAddresNumber());
				x.setRazonSocial(o.getRazonSocial());
				x.setStatus(o.getStatus());

				if("ACEPTADO".equals(x.getStatus())) {
					if(o.getAttachment()) {
						NoticeDocument doc = noticeDao.noticeDocByIdNoticeAndSupplier(idNotice,x.getAddresNumber());
						
						if(doc != null) {
							x.setAttachment(true);
							x.setDoc("_FILE:" + doc.getId() + "_:_" + doc.getName());
						}
					}
				}
				
				sups.add(x);
			}
			
			
			return sups;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public NoticeDetail noticeDetailByIdNoticeAndAddresNumber (String idNotice, String addNum) {
		return noticeDao.noticeDetailByIdNoticeAndAddresNumber(idNotice,addNum);
	}
	
	public  void updateNoticeDetail (List<NoticeDetail> notice) {
		noticeDao.saveNoticeDetail(notice);
	}
	
	public NoticeDocument noticeDocByIdNotice (int idNotice) {
		return noticeDao.getDocumentById(idNotice);
	}
	
	public List<Notice> getNoticesList(int start, int limit) {
		return noticeDao.getNoticesList(start, limit);
	}
	
	public List<Notice> searchCriteria(String query){
		return noticeDao.searchCriteria(query);
	}
	
	public int getTotalRecords(){
		return noticeDao.getTotalRecords();
	}
	
	public void sendEmailWithAttachToUDC(File file, String title) throws IOException{
		
		String emailSubject = "SEPASA | " + title;
		String emailMessage = "";
		byte[] bytes = Files.readAllBytes(file.toPath());
		
		try {
			List<UDC> udc =  udcService.searchBySystem("EMAILNOTIFNOTICE");
			
			for(UDC x : udc) {
				InternetAddress[] parseRecipients = InternetAddress.parse(x.getStrValue1() , true);
				
				mailSenderObj.send(new MimeMessagePreparator() {
					public void prepare(MimeMessage mimeMessage) throws Exception {
						MimeMessageHelper mimeMsgHelperObj = new MimeMessageHelper(mimeMessage, true, "UTF-8");				
						mimeMsgHelperObj.setTo(parseRecipients);
						mimeMsgHelperObj.setFrom(AppConstants.EMAIL_FROM);				
						mimeMsgHelperObj.setText(emailMessage);
						mimeMsgHelperObj.setSubject(emailSubject);
						
						mimeMsgHelperObj.addAttachment(file.getName(), new ByteArrayResource(bytes), "text/plain");
					}
				});
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendEmailWithAttach(String recipient, NoticeDocument o, String title){
		
		String emailSubject = "SEPASA | " + title;
		String emailMessage = "Estimado Proveedor:\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"Reciba un saludo por parte de SEPASA.\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"Usted tiene un aviso pendiente de leer y confirmar en nuestro portal, mismo que se encuentra anexo. Lo invitamos en iniciar sesión en nuestro portal de proveedores para su aceptación\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"Gracias y Saludos,\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"*******************************\r\n" + 
				"\r\n" + 
				"Dear Supplier:\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"Greetings from SEPASA,\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"You have a pending notice to read and confirm on our portal, which is attached. Please log in to our supplier portal to confirm it.\r\n" + 
				"\r\n" + 
				" \r\n" + 
				"\r\n" + 
				"Best regards";
		
		try {
			InternetAddress[] parseRecipients = InternetAddress.parse(recipient , true);
			mailSenderObj.send(new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper mimeMsgHelperObj = new MimeMessageHelper(mimeMessage, true, "UTF-8");				
					mimeMsgHelperObj.setTo(parseRecipients);
					mimeMsgHelperObj.setFrom(AppConstants.EMAIL_FROM);				
					mimeMsgHelperObj.setText(emailMessage);
					mimeMsgHelperObj.setSubject(emailSubject);
					
					mimeMsgHelperObj.addAttachment(o.getName(), new ByteArrayResource(o.getContent()), o.getType());
				}
			});
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//@Scheduled(cron = "0 0 1 * * ?")
	//@Scheduled(fixedDelay = 420000, initialDelay = 3000)
	public void validateNoticeFrequency() {
		try {
			List<Notice> notices = noticeDao.getNoticesListActive();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
			
			Date today = new Date();	
			String strToday = dateFormat.format(today);  
			Date hoy = formato.parse(strToday);
			
			if(notices.size()>0) {
				for(Notice x : notices) {
					String strFromDate = dateFormat.format(x.getNoticeFromDate());
					String strToDate = dateFormat.format(x.getNoticeToDate());
					Date from = formato.parse(strFromDate);
					Date to = formato.parse(strToDate);
					/*System.out.println(x.getNoticeTitle()+" "+x.getNoticeFromDate() + " | " + x.getNoticeToDate());
					
					if(hoy.after(from)) System.out.println("Mayor a creacion");
					
					if(hoy.equals(from)) System.out.println("Igual a creacion");
					
					if(hoy.before(to)) System.out.println("Menor a final");
					
					if(hoy.equals(to)) System.out.println("Igual a final");*/
					
					if((hoy.after(from) || hoy.equals(from)) &&
						(hoy.before(to) || hoy.equals(to))){
							
							Boolean dateMatch = false;
							switch (x.getFrequency()) { 
						    case "ANNUAL":
						    	if(hoy.equals(from)){
						    		dateMatch =true;
						    	}else {
						    		LocalDate fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		LocalDate toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		
						    		long numberOFDays = ChronoUnit.YEARS.between(fromDate,toDate);
							    	
							    	if(numberOFDays>0) {
							    		for (int  i = 0; i< numberOFDays; i++){
								    		 Calendar calendarFrom = Calendar.getInstance();
											 calendarFrom.setTime(from);
											 calendarFrom.add(Calendar.YEAR, 1);
											 from = calendarFrom.getTime();
											 if(hoy.equals(from)){
												 dateMatch =true;
										    	 break;
										    	}
								    	 }
							    	}
						    	}
						     break;
						    case "MONTHLY":
						    	if(hoy.equals(from)){
						    		dateMatch =true;
						    	}else {
						    		
						    		LocalDate fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		LocalDate toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		
						    		long numberOFDays = ChronoUnit.MONTHS.between(fromDate,toDate);
							    	
							    	if(numberOFDays>0) {
							    		for (int  i = 0; i< numberOFDays; i++){
								    		 Calendar calendarFrom = Calendar.getInstance();
											 calendarFrom.setTime(from);
											 calendarFrom.add(Calendar.MONTH, 1);
											 from = calendarFrom.getTime();
											 if(hoy.equals(from)){
												 dateMatch =true;
										    	 break;
										    	}
								    	 }
							    	}
						    	}
						     break;
						    case "WEEKLY" :
						    	if(hoy.equals(from)){
						    		dateMatch =true;
						    	}else {
						    		
						    		LocalDate fromDate = from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		LocalDate toDate = to.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						    		
						    		long numberOFDays = ChronoUnit.WEEKS.between(fromDate,toDate);
							    	
							    	if(numberOFDays>0) {
							    		for (int  i = 0; i< numberOFDays; i++){
								    		 Calendar calendarFrom = Calendar.getInstance();
											 calendarFrom.setTime(from);
											 calendarFrom.add(Calendar.WEEK_OF_YEAR, 1);
											 from = calendarFrom.getTime();
											 if(hoy.equals(from)){
												 dateMatch =true;
										    	 break;
										    	}
								    	 }
							    	}
						    	}
								//o.setNoticeFromDate(calendarFrom.getTime());
						     break;
						    default:
					     // Default secuencia de sentencias.
					  }
							
							List<NoticeDetail> supps = noticeDao.listSuppsOnNotice(x.getIdNotice());
					    	if(dateMatch == true) {
					    		//System.out.println("Fecha coincide");
					    		if(supps != null) {
					    			for(NoticeDetail sup : supps) {
						    			sup.setEnabled(true);
						    		}
					    		}
					    	}else {
					    		if(supps != null) {
					    			for(NoticeDetail sup : supps) {
						    			sup.setEnabled(false);
						    		}
					    		}
					    	}
					    	noticeDao.saveNoticeDetail(supps);
					}
					else x.setEnabled(false);
				}
				
				for(Notice x : notices) {
					if(x.getEnabled()==false) {
						List<NoticeDetail> supps = noticeDao.listSuppsOnNotice(x.getIdNotice());
						
						if(supps!=null) {
							for(NoticeDetail sup : supps) {
								sup.setEnabled(false);
							}
							noticeDao.saveNoticeDetail(supps);
						}
					}
				}
				noticeDao.updateNotice(notices);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
