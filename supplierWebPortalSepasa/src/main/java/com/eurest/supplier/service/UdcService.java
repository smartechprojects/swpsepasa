package com.eurest.supplier.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eurest.supplier.dao.UDCDao;
import com.eurest.supplier.model.UDC;
import com.eurest.supplier.util.AppConstants;

@Service("udcService")
public class UdcService {
	
	@Autowired
	private UDCDao udcDao;
	
	@Autowired
	DataAuditService dataAuditService;
	
	public UDC getUdcById(int id){
		return udcDao.getUDCById(id);
	}
	
	public List<UDC> getUDCList(int start, int limit) {
		return udcDao.getUDCList(start, limit);
	}
	
	public List<UDC> searchCriteria(String query){
		return udcDao.searchCriteria(query);
	}
	
	public UDC searchBySystemAndKey(String udcSystem, String udcKey){
		return udcDao.searchBySystemAndKey(udcSystem, udcKey);
	}
	
	public List<UDC> searchBySystemAndKeyList(String udcSystem, String udcKey){
		return udcDao.searchBySystemAndKeyList(udcSystem, udcKey);
	}
	
	public List<UDC> advaceSearch(String udcSystem, String udcKey,String systemRef,String keyRef){
		return udcDao.advaceSearch(udcSystem, udcKey, systemRef, keyRef);
	}

	public List<UDC> advaceSearchByEquals(String udcSystem, String udcKey,String systemRef,String keyRef){
		return udcDao.advaceSearchByEquals(udcSystem, udcKey, systemRef, keyRef);
	}
	
	public List<UDC> searchBySystem(String udcSystem){
		return udcDao.searchBySystem(udcSystem);
	}
	
	public UDC searchBySystemAndKeyRef(String udcSystem, String udcKey, String systemRef){
		return udcDao.searchBySystemAndKeyRef(udcSystem, udcKey, systemRef);
	}
	
	public UDC searchBySystemAndStrValue(String udcSystem, String udcKey, String strValue){
		return udcDao.searchBySystemAndStrValue(udcSystem, udcKey, strValue);
	}

	public UDC searchBySystemAndStrValue2(String udcSystem, String udcKey, String strValue1, String strValue2){
		return udcDao.searchBySystemAndStrValue2(udcSystem, udcKey, strValue1, strValue2);
	}

	public List<UDC> searchBySystemAndStrValue2List(String udcSystem, String udcKey, String strValue1, String strValue2){
		return udcDao.searchBySystemAndStrValue2List(udcSystem, udcKey, strValue1, strValue2);
	}
	
	public void save(UDC udc, Date date, String user){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
 		
		udc.setCreationDate(date);
		udc.setCreatedBy(user);
		udc.setUpdatedDate(date);
		udc.setUpdatedBy(user);
		udcDao.saveUDC(udc);
		
		dataAuditService.saveDataAudit("SaveUDC", null, currentDate,  request.getRemoteAddr(),
		userAuth, udc.toString(), "save", "Save UDC Successful" ,null, null, null, 
		null, AppConstants.STATUS_COMPLETE, AppConstants.UDC_MODULE);
				
	}
	
	public void update(UDC udc, Date date, String user){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
 		String userAuth = auth.getName();
 		Date currentDate = new Date();
 		
 		UDC udcOld = udcDao.getUDCById(udc.getId());
 		
		udc.setUpdatedDate(date);
		udc.setUpdatedBy(user);
		udcDao.updateUDC(udc);
		
		dataAuditService.saveDataAudit("UpdateUDC", null, currentDate,  request.getRemoteAddr(),
		userAuth, udc.toString(), "update", "UDC Old - " + udcOld.toString() ,null, null, null, 
		null, AppConstants.STATUS_COMPLETE, AppConstants.UDC_MODULE);
		
	}
	
	public void delete(int id){
		udcDao.deleteUDC(id);
	}
	
	public int getTotalRecords(){
		return udcDao.getTotalRecords();
	}

}
