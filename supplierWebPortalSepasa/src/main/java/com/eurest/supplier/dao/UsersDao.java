package com.eurest.supplier.dao;

import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.eurest.supplier.model.Users;
import com.eurest.supplier.util.AppConstants;

@Repository("usersDao")
@Transactional
public class UsersDao {
	
	@Autowired
	SessionFactory sessionFactory;
	
	public Users getUsersById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (Users) session.get(Users.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> getUsersList(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Users");
	    q.setFirstResult(start); // modify this to adjust paging
	    q.setMaxResults(limit);
		return (List<Users>) q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> searchCriteria(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("name", "%" + query + "%"))
				.add(Restrictions.like("userName", "%" + query + "%"))
				);
		return (List<Users>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public Users searchCriteriaUserName(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.eq("userName", query))
				);
		List<Users> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Users getUserByEmail(String email){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(Restrictions.eq("email", email).ignoreCase());
		List<Users> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Users getPurchaseRoleByEmail(String email){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(Restrictions.eq("email", email).ignoreCase());		
		criteria.add(Restrictions.or(Restrictions.eq("role", AppConstants.ROLE_PURCHASE).ignoreCase(),
				Restrictions.eq("role", AppConstants.ROLE_ADMIN_PURCHASE).ignoreCase(),
				Restrictions.eq("role", AppConstants.ROLE_ADMIN).ignoreCase(),
				Restrictions.eq("role", AppConstants.ROLE_PURCHASE_IMPORT).ignoreCase())
				);		
		//criteria.add(Restrictions.eq("role", AppConstants.ROLE_PURCHASE).ignoreCase());
		
		List<Users> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Users getByUserName(String userName){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(Restrictions.eq("userName", userName));
		List<Users> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> getByUserNameList(String[] userNameArray){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		criteria.add(Restrictions.in("userName", userNameArray));
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> searchCriteriaByRole(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		
		criteria.createAlias("userRole", "r");  // here i changed 
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("r.strValue1", "%" + query.trim() + "%"))
				);
		
		criteria.addOrder(Order.asc("email"));
		
		return (List<Users>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Users> searchCriteriaByRoleExclude(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Users.class);
		
		criteria.createAlias("userRole", "r");  // here i changed 
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.ne("r.strValue1", query.trim()))
				);
		
		criteria.addOrder(Order.asc("email"));
		
		return (List<Users>) criteria.list();
	}
	
	public void saveUsers(Users o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(o);
	}
	
	public void saveUsersList(List<Users> list) {
		Session session = this.sessionFactory.getCurrentSession();
		int i=0;
		session.setCacheMode(CacheMode.IGNORE);
		session.setFlushMode(FlushMode.COMMIT);
		for(Users o : list) {
		   session.saveOrUpdate(o);
		   if( i % 50 == 0 ) {
			      session.flush();
			      session.clear();
			   }
		   i++;
		}
	}

	public void updateUsers(Users o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(o);
	}

	public void deleteUsers(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		Users p = (Users) session.load(Users.class, new Integer(id));
		if(null != p){
			session.delete(p);
		}
	}
	
	public int getTotalRecords(){
		Session session = this.sessionFactory.getCurrentSession();
		Long count = (Long) session.createQuery("select count(*) from  Users").uniqueResult();
		return count.intValue();
		
	}


}
