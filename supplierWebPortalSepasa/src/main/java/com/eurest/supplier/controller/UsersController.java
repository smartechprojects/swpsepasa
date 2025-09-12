package com.eurest.supplier.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eurest.supplier.model.UDC;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.service.UdcService;
import com.eurest.supplier.service.UsersService;


@Controller
public class UsersController {
	
	@Autowired
	private UsersService usersService;

	@Autowired
	private UdcService udcService;
	
	Logger log4j = Logger.getLogger(UsersController.class);
	
	@RequestMapping(value ="/admin/users/view.action")
	public @ResponseBody Map<String, Object> view(@RequestParam int start,
												  @RequestParam int limit,
												  @RequestParam String query,
												  HttpServletRequest request){	
		List<Users> list=null;
		int total=0;
		try{
			if("".equals(query)){
				list = usersService.getUsersList(start, limit);
				total = usersService.getTotalRecords();
			}else{
				list = usersService.searchCriteria(query);
				total = list.size();
			}
			
			for(Users u : list) {
				String pass = u.getPassword().substring(6);
				byte [] barr = Base64.getDecoder().decode(pass); 
				u.setPassword(new String(barr));
			}
			
		    return mapOK(list, total);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
		
	}

	@RequestMapping(value ="/admin/users/viewByRole.action")
	public @ResponseBody Map<String, Object> viewByRole(@RequestParam int start,
												  @RequestParam int limit,
												  @RequestParam String query){	
		List<Users> list=null;
		int total=0;
		try{
				if(!"".equals(query)){
					list = usersService.searchCriteriaByRole(query);
					total = list.size();
					return mapOK(list, total);
				}else{
					return mapOK(list, 0);
				}
		        
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value ="/admin/users/searchByRole.action")
	public @ResponseBody Map<String, Object> searchByRole(@RequestParam String role){	
		List<Users> list=null;
		int total=0;
		try{
				if(!"".equals(role)){
					list = usersService.searchCriteriaByRole(role);
					total = list.size();
					return mapOK(list, total);
				}else{
					return mapOK(list, 0);
				}
		        
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value ="/admin/users/searchByRoleExclude.action")
	public @ResponseBody Map<String, Object> searchByRoleExclude(@RequestParam String role){	
		List<Users> list=null;
		int total=0;
		try{
				if(!"".equals(role)){
					list = usersService.searchCriteriaByRoleExclude(role);
					total = list.size();
					return mapOK(list, total);
				}else{
					return mapOK(list, 0);
				}
		        
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value ="/admin/users/searchByApprovalStep.action")
	public @ResponseBody Map<String, Object> searchByApprovalStep(@RequestParam String step){	
		List<Users> list=null;
		int total=0;
		try{
				if(step != null && !"".equals(step)){
					List<UDC> udcUsrList = udcService.searchBySystemAndKeyList("APPROVER", step.concat("_APPROVER"));
					if(udcUsrList != null && !udcUsrList.isEmpty()) {
						List<String> usrNameList = new ArrayList<String>();
						for(UDC udc : udcUsrList) {
							if(!usrNameList.contains(udc.getStrValue1().trim().toUpperCase())) {
								usrNameList.add(udc.getStrValue1().trim().toUpperCase());
							}
						}
						list = usersService.getByUserNameList(usrNameList.stream().toArray(String[]::new));
						total = list.size();
					}
					return mapOK(list, total);
				}else{
					return mapOK(list, 0);
				}
		        
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}
	
	@RequestMapping(value ="/admin/users/searchUser.action")
	public @ResponseBody Map<String, Object> searchUser(@RequestParam String user){	

		try{
			Users e = usersService.searchCriteriaUserName(user);				
		    return mapOK(e);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
		
	}
		
	@RequestMapping(value ="/admin/users/save.action", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> save(@RequestBody Users obj){
		
		try{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usr = auth.getName();
			usersService.save(obj,new Date(),usr);
			return mapOK(new Users());
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}

	@RequestMapping(value ="/admin/users/update.action")
	public @ResponseBody Map<String, Object> update(@RequestBody Users obj){
		
		try{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String usr = auth.getName();
			usersService.update(obj,new Date(),usr);
			return mapStrOk("El registro se actualizó de forma correcta");
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}

	@RequestMapping(value ="/admin/users/delete.action")
	public @ResponseBody Map<String, Object>  delete(@RequestBody Users obj){

		try{ 
			usersService.delete(obj.getId());
			return mapOK(new Users());
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			return mapError(e.getMessage());
		}
	}


	public Map<String,Object> mapOK(List<Users> list, int total){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", total);
		modelMap.put("data", list);
		modelMap.put("success", true);
		return modelMap;
	}

	public Map<String,Object> mapOK(Users obj){
		Map<String,Object> modelMap = new HashMap<String,Object>(3);
		modelMap.put("total", 1);
		modelMap.put("data", obj);
		modelMap.put("success", true);
		return modelMap;
	}

	public Map<String,Object> mapError(String msg){
		Map<String,Object> modelMap = new HashMap<String,Object>(2);
		modelMap.put("message", msg);
		modelMap.put("success", false);
		return modelMap;
	} 

	public Map<String,Object> mapStrOk(String msg){
		Map<String,Object> modelMap = new HashMap<String,Object>(2);
		modelMap.put("message", msg);
		modelMap.put("success", true);
		return modelMap;
	} 

	}
