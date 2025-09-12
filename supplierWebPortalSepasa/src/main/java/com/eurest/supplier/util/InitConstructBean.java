package com.eurest.supplier.util;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eurest.supplier.dao.CodigosSatDao;

@Component
public class InitConstructBean {
    
	@SuppressWarnings("unused")
	@Autowired
	private CodigosSatDao codigosSatDao;
	
	public static List<String> listSat;
 
    @PostConstruct
    public void init() {

    	//listSat = codigosSatDao.getCodeList();
    }
    
    public static List<String> getCodigosSatList() {
    	return listSat;
    }
    
    public static void setCodigosSatList(List<String> list) {
    	listSat = list;
    }
}
