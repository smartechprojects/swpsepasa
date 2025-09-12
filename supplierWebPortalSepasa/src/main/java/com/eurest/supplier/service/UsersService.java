package com.eurest.supplier.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eurest.supplier.dao.UsersDao;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.StringUtils;

@Service("usersService")
public class UsersService {
	
	@Autowired
	UsersDao usersDao;
	
	@Autowired
	private JavaMailSender mailSenderObj;
	
	@Autowired
	private UdcService udcService;
	
	@Autowired
	SupplierService supplierService;
	
	@Autowired
	StringUtils stringUtils;
	
	@Autowired
	DataAuditService dataAuditService;
	
	Logger log4j = Logger.getLogger(UsersService.class);
	
	public Users getusersById(int id){
		return usersDao.getUsersById(id);
	}
	
	public List<Users> getUsersList(int start, int limit) {
		return usersDao.getUsersList(start, limit);
	}
	
	public Users getUserByEmail(String email){
		return usersDao.getUserByEmail(email);
	}
	
	public Users getPurchaseRoleByEmail(String email){
		return usersDao.getPurchaseRoleByEmail(email);
	}
	
	public Users getByUserName(String userName){
		return usersDao.getByUserName(userName);
	}
	
	public List<Users> getByUserNameList(String[] userNameArray){
		return usersDao.getByUserNameList(userNameArray);
	}
	
	public List<Users> searchCriteria(String query){
		return usersDao.searchCriteria(query);
	}
	
	public Users searchCriteriaUserName(String query){
		return usersDao.searchCriteriaUserName(query);
	}
	
	public List<Users> searchCriteriaByRole(String query){
		return usersDao.searchCriteriaByRole(query);
	}
	
	public List<Users> searchCriteriaByRoleExclude(String query){
		return usersDao.searchCriteriaByRoleExclude(query);
	}
	
	public void save(Users users, Date date, String user){		
		UDC udc = udcService.getUdcById(users.getUserRole().getId());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
 		
		if(udc != null) users.setRole(udc.getStrValue1());
		
		String pass = users.getPassword();
		pass = pass.replace("==a20$", "");
		if(!isValidBase64(pass))
		{
			String encodedPass = Base64.getEncoder().encodeToString(users.getPassword().trim().getBytes());
		    users.setPassword("==a20$" + encodedPass);
		}
		
		usersDao.saveUsers(users);
		
		dataAuditService.saveDataAudit("SaveUser", null, currentDate, request.getRemoteAddr(),
		userAuth, users.toString(), "save", "Save User Successful" ,null, null, null, 
		null, AppConstants.STATUS_COMPLETE, AppConstants.USERS_MODULE);
	}
	
	public void saveUsersList(List<Users> users){
		usersDao.saveUsersList(users);
	}
	
	public void update(Users users, Date date, String user){
		UDC udc = udcService.getUdcById(users.getUserRole().getId());
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
 		
 		Users userOld = usersDao.getUsersById(users.getId());
 				
		if(udc != null) users.setRole(udc.getStrValue1());
		
		String pass = users.getPassword();
		pass = pass.replace("==a20$", "");
		if(!isValidBase64(pass))
		{
			String encodedPass = Base64.getEncoder().encodeToString(users.getPassword().trim().getBytes());
		    users.setPassword("==a20$" + encodedPass);
		}
		
		usersDao.updateUsers(users);
		
		dataAuditService.saveDataAudit("UpdateUser", null, currentDate, request.getRemoteAddr(),
        userAuth, users.toString(), "update", "User Old - " + userOld.toString() ,null, null, null, 
    	null, AppConstants.STATUS_COMPLETE, AppConstants.USERS_MODULE);																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									
	}
	
	public void updateAgreement(Users users){
		users.setAgreementAccept(true);
		usersDao.updateUsers(users);
	}
	
	public void delete(int id){
		usersDao.deleteUsers(id);
	}
	
	public int getTotalRecords(){
		return usersDao.getTotalRecords();
	}
	
	public boolean isValidBase64( String string ) {
		try {
			@SuppressWarnings("unused")
			byte[] decodedBytes = Base64.getDecoder().decode(string);
			return true;
		}catch(Exception e) {
			log4j.error("Exception" , e);
			return false;
		}
	}
	
	public String resetPassword(String username){

			Users u = usersDao.searchCriteriaUserName(username);
			if(u != null){
				String tempPass = getAlphaNumericString(8);;
				String encodePass = Base64.getEncoder().encodeToString(tempPass.trim().getBytes());
				u.setPassword("==a20$" + encodePass); 
				u.setLogged(false);
				usersDao.updateUsers(u);

				String emailRecipient = (u.getEmail());
				String credentials = "<br /><br />Usuario: " + u.getUserName() + "<br />Contraseña: " + tempPass + " <br /> url: " + AppConstants.EMAIL_PORTAL_LINK ;
				EmailServiceAsync emailAsyncSup = new EmailServiceAsync();
				emailAsyncSup.setProperties(AppConstants.PASS_RESET, stringUtils.prepareEmailContent(AppConstants.EMAIL_PASS_RESET_NOTIFICATION + credentials), emailRecipient);
				emailAsyncSup.setMailSender(mailSenderObj);
				Thread emailThreadSup = new Thread(emailAsyncSup);
				emailThreadSup.start();	

				return "";
			}else {
				return "La cuenta de usuario no existe";
			}	
		
	}
	
	private String getAlphaNumericString(int n) 
	{ 

		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
									+ "0123456789"
									+ "abcdefghijklmnopqrstuvxyz"; 

		StringBuilder sb = new StringBuilder(n); 

		for (int i = 0; i < n; i++) { 
			int index 
				= (int)(AlphaNumericString.length() 
						* Math.random()); 
			sb.append(AlphaNumericString 
						.charAt(index)); 
		} 
		return sb.toString(); 
	}
		  
 	//@Scheduled(fixedDelay = 4200000, initialDelay = 30000)
	//@Scheduled(cron = "0 20 1 23-31 * *")//Deshabilita las cuentas de los proveedores a partir del 23
	//@Scheduled(cron = "0 20 1 14-31 * *")//Deshabilita las cuentas de los proveedores a partir del 14 (SOLO DICIEMBRE 2024)
	@Scheduled(cron = "0 20 1 * * *")
	public void disableSupplier1() {	
		List<UDC> datesList = udcService.searchBySystemAndKeyList("CALENDARIOFACTURAS", "BLOQUEOPROVEEDOR");
		boolean execute = false;
		Date today = new Date(); 
        LocalDate todayLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		for(UDC date : datesList) {
			 LocalDate localDate = LocalDate.parse(date.getStrValue1());
			//LocalDate udcDateLocatDate = date.getDateValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if(todayLocalDate.equals(localDate)) {
				execute = true;
				System.out.println(execute);
				break;
			}
		}
		
		if(execute) {
		
		List<Users> list=null;
		String role = "ROLE_SUPPLIER";			
		if(!"".equals(role)){
			//list = usersService.searchCriteriaByRole(role);
			list = searchCriteriaByRole(role);
			if(list != null) {
				for(Users u : list) {
					u.setEnabled(false);
					
					try{
						//usersService.update(u, new Date(), usr);	
						//update(u, null, null);
						//return mapStrOk("El registro se actualizó de forma correcta");
						System.out.println("username: " + u.getUserName());
						usersDao.updateUsers(u);
						System.out.println("El registro se actualizó de forma correcta");
					} catch (Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();
						//return mapError(e.getMessage());
					}						
					
				}
			}				
		}		
	  }
	}
	

	//@Scheduled(fixedDelay = 4200000, initialDelay = 30000)
	@Scheduled(cron = "0 35 1 1 * *")//Habilita las cuentas de los proveedores a partir del 23
	public void enableSupplier1() {		
		List<Users> list=null;
		String role = "ROLE_SUPPLIER";			
		if(!"".equals(role)){
			//list = usersService.searchCriteriaByRole(role);
			list = searchCriteriaByRole(role);
			if(list != null) {
				for(Users u : list) {
					u.setEnabled(true);
					
					try{
						//usersService.update(u, new Date(), usr);	
						//update(u, null, null);
						//return mapStrOk("El registro se actualizó de forma correcta");
						System.out.println("username: " + u.getUserName());
						usersDao.updateUsers(u);
						System.out.println("El registro se actualizó de forma correcta");
					} catch (Exception e) {
						log4j.error("Exception" , e);
						e.printStackTrace();
						//return mapError(e.getMessage());
					}						
					
				}
			}				
		}					    
	}
	
	
}
