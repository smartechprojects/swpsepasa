package com.eurest.supplier.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.eurest.supplier.model.UDC;

@Repository("udcDao")
@Transactional
public class UDCDao{

	@Autowired
	SessionFactory sessionFactory;

	public UDC getUDCById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (UDC) session.get(UDC.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> getUDCList(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from UDC");
	    q.setFirstResult(start); // modify this to adjust paging
	    q.setMaxResults(limit);
		return (List<UDC>) q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> searchCriteria(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("udcSystem", "%" + query + "%"))
				.add(Restrictions.like("udcKey", "%" + query + "%"))
				.add(Restrictions.like("systemRef", "%" + query + "%"))
				.add(Restrictions.like("keyRef", "%" + query + "%"))
				.add(Restrictions.like("strValue1", "%" + query + "%"))
				.add(Restrictions.like("strValue2", "%" + query + "%"))
				);
		criteria.addOrder(Order.asc("strValue1"));
		return (List<UDC>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public UDC searchBySystemAndKey(String udcSystem, String udcKey){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.like("udcKey", udcKey.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   List<UDC> list =  criteria.list();
	   if(list != null){
		   if(!list.isEmpty())
			   return list.get(0);
		   else 
			   return null;
	   }else{
		   return null;
	   }
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> searchBySystemAndKeyList(String udcSystem, String udcKey){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.eq("udcKey", udcKey.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public UDC searchBySystemAndKeyRef(String udcSystem, String udcKey, String systemRef){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.eq("udcKey", udcKey.trim()))
				.add(Restrictions.eq("keyRef", systemRef.trim()).ignoreCase())
				);
		criteria.addOrder(Order.asc("strValue1"));
	   List<UDC> list =  criteria.list();
	   if(list != null){
		   if(!list.isEmpty())
			   return list.get(0);
		   else 
			   return null;
	   }else{
		   return null;
	   }
	}
	
	@SuppressWarnings("unchecked")
	public UDC searchBySystemAndStrValue(String udcSystem, String udcKey, String strValue){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.like("udcKey", udcKey.trim()))
				.add(Restrictions.like("strValue1", strValue.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   List<UDC> list =  criteria.list();
	   if(list != null){
		   if(!list.isEmpty())
			   return list.get(0);
		   else 
			   return null;
	   }else{
		   return null;
	   }
	}

	@SuppressWarnings("unchecked")
	public UDC searchBySystemAndStrValue2(String udcSystem, String udcKey, String strValue1, String strValue2){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.like("udcKey", udcKey.trim()))
				.add(Restrictions.like("strValue1", strValue1.trim()))
				.add(Restrictions.like("strValue2", strValue2.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   List<UDC> list =  criteria.list();
	   if(list != null){
		   if(!list.isEmpty())
			   return list.get(0);
		   else 
			   return null;
	   }else{
		   return null;
	   }
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> searchBySystemAndStrValue2List(String udcSystem, String udcKey, String strValue1, String strValue2){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				.add(Restrictions.like("udcKey", udcKey.trim()))
				.add(Restrictions.like("strValue1", strValue1.trim()))
				.add(Restrictions.like("strValue2", strValue2.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> searchBySystem(String udcSystem){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("udcSystem", udcSystem.trim()))
				);
		criteria.addOrder(Order.asc("strValue1"));
	   return  criteria.list();	   
	}

	@SuppressWarnings("unchecked")
	public List<UDC> advaceSearch(String udcSystem, String udcKey,String systemRef,String keyRef){
		
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		criteria.add(Restrictions.like("udcSystem", "%" + udcSystem + "%"));			
		criteria.add(Restrictions.like("udcKey", "%" + udcKey + "%"));				
		criteria.add(Restrictions.like("systemRef", "%" + systemRef + "%"));			
		criteria.add(Restrictions.like("keyRef", "%" + keyRef + "%"));	
		criteria.addOrder(Order.asc("strValue1"));
		return (List<UDC>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UDC> advaceSearchByEquals(String udcSystem, String udcKey, String systemRef, String keyRef) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UDC.class);
		
		if(!"".equals(udcSystem)) {
			criteria.add(Restrictions.eq("udcSystem", udcSystem));
		}
		
		if(!"".equals(udcKey)) {
			criteria.add(Restrictions.eq("udcKey", udcKey));
		}
		
		if(!"".equals(systemRef)) {
			criteria.add(Restrictions.eq("systemRef", systemRef));
		}

		if(!"".equals(keyRef)) {
			criteria.add(Restrictions.eq("keyRef", keyRef));
		}

		criteria.addOrder(Order.asc("strValue1"));
		return (List<UDC>) criteria.list();
	}
	
	public void saveUDC(UDC udc) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(udc);
	}

	public void updateUDC(UDC udc) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(udc);
	}

	public void deleteUDC(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		UDC p = (UDC) session.load(UDC.class, new Integer(id));
		if(null != p){
			session.delete(p);
		}
	}
	
	public int getTotalRecords(){
		Session session = this.sessionFactory.getCurrentSession();
		Long count = (Long) session.createQuery("select count(*) from  UDC").uniqueResult();
		return count.intValue();
	}

}
