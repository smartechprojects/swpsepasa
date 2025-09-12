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

import com.eurest.supplier.model.Notice;
import com.eurest.supplier.model.NoticeDetail;
import com.eurest.supplier.model.NoticeDocument;

@Repository("noticeDao")
@Transactional
public class NoticeDao {
	
	@Autowired
	SessionFactory sessionFactory;

	public void saveNotice(Notice o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(o);
	}
	
	public void updateNotice(List<Notice> list) {
		Session session = this.sessionFactory.getCurrentSession();
		int i=0;
		session.setCacheMode(CacheMode.IGNORE);
		session.setFlushMode(FlushMode.COMMIT);
		for(Notice o : list) {
		   session.saveOrUpdate(o);
		   if( i % 50 == 0 ) {
			      session.flush();
			      session.clear();
			   }
		   i++;
		}
	}
	
	public void saveNoticeDetail(List<NoticeDetail> list) {
		Session session = this.sessionFactory.getCurrentSession();
		int i=0;
		session.setCacheMode(CacheMode.IGNORE);
		session.setFlushMode(FlushMode.COMMIT);
		for(NoticeDetail o : list) {
		   session.saveOrUpdate(o);
		   if( i % 50 == 0 ) {
			      session.flush();
			      session.clear();
			   }
		   i++;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<NoticeDetail> noticeActivesBySupp(String supp){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(NoticeDetail.class);
		criteria.add(Restrictions.eq("status", "PENDIENTE"));
		criteria.add(Restrictions.eq("addresNumber", supp));
		criteria.add(Restrictions.eq("enabled", new Boolean(true)));
		List<NoticeDetail> list =  criteria.list();
				
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public NoticeDocument noticeDocByIdNotice(String supp){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(NoticeDocument.class);
		criteria.add(Restrictions.eq("idNotice", supp));
		
		List<NoticeDocument> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public NoticeDocument noticeDocByIdNoticeAndSupplier(String idNotice,String supp){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(NoticeDocument.class);
		criteria.add(Restrictions.eq("idNotice", idNotice));
		criteria.add(Restrictions.eq("createdBy", supp));
		
		List<NoticeDocument> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public NoticeDetail noticeDetailByIdNoticeAndAddresNumber(String idNotice,String supp){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(NoticeDetail.class);
		criteria.add(Restrictions.eq("idNotice", idNotice));
		criteria.add(Restrictions.eq("addresNumber", supp));
		
		List<NoticeDetail> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	}
	
	public Notice getNoticeById(String id) {
	    Session session = this.sessionFactory.getCurrentSession();
	    Criteria criteria = session.createCriteria(Notice.class);
		criteria.add(Restrictions.eq("idNotice", id));
		
		List<Notice> list =  criteria.list();
		if(!list.isEmpty()){
			return list.get(0);
		}		
		return null;
	  }
	
	@SuppressWarnings("unchecked")
	public List<NoticeDetail> listSuppsOnNotice(String noticeId){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(NoticeDetail.class);
		criteria.add(Restrictions.eq("idNotice", noticeId));
		criteria.addOrder(Order.asc("razonSocial"));
		List<NoticeDetail> list =  criteria.list();
				
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Notice> getNoticesList(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Notice");
	    q.setFirstResult(start); // modify this to adjust paging
	    q.setMaxResults(limit);
		return (List<Notice>) q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Notice> searchCriteria(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Notice.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("noticeTitle", "%" + query + "%"))
				//.add(Restrictions.like("userName", "%" + query + "%"))
				);
		return (List<Notice>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<Notice> getNoticesListActive(){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Notice.class);
		criteria.add(Restrictions.eq("enabled", true));
		return (List<Notice>) criteria.list();
	}
	
	public NoticeDocument getDocumentById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (NoticeDocument) session.get(NoticeDocument.class, id);
	}
	
	public int getTotalRecords(){
		Session session = this.sessionFactory.getCurrentSession();
		Long count = (Long) session.createQuery("select count(*) from  Notice").uniqueResult();
		return count.intValue();
		
	}
}
