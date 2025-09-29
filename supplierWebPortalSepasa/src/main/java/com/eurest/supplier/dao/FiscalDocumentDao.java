package com.eurest.supplier.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.eurest.supplier.model.FiscalDocuments;
import com.eurest.supplier.model.Receipt;
import com.eurest.supplier.util.AppConstants;

@Repository("fiscalDocumentDao")
@Transactional
public class FiscalDocumentDao {
	
	@Autowired
	SessionFactory sessionFactory;
	
	public FiscalDocuments getById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (FiscalDocuments) session.get(FiscalDocuments.class, id);
	}
			
	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getFiscalDocuments(String addressNumber,
			                                      String status,
			                                      int start,
			                                      int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
	            .setFetchMode("FiscalDocumentsConcept", FetchMode.SELECT);
		criteria.setFirstResult(start);
		criteria.setMaxResults(limit);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.like("addressNumber", "%" + addressNumber + "%"))
				//.add(Restrictions.eq("status", status))
				);
		return criteria.list();
	}
	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getFiscalDocuments(String addressNumber,
			                                      String status,
			                                      String uuid,
			                                      String documentType,
			                                      int start,
			                                      int limit, String pFolio) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
	            .setFetchMode("FiscalDocumentsConcept", FetchMode.SELECT);
		criteria.setFirstResult(start);
		criteria.setMaxResults(limit);
		
		if(!"".equals(uuid) || !"".equals(status) || !"".equals(documentType) || !"".equals(addressNumber)|| !"".equals(pFolio)) {
			if(!"".equals(uuid)) criteria.add(Restrictions.eq("uuidFactura", uuid));
			if(!"".equals(documentType)) criteria.add(Restrictions.eq("type", documentType));
			if(!"".equals(addressNumber)) criteria.add(Restrictions.like("addressNumber", "" + addressNumber + ""));
			if(!"".equals(pFolio)) criteria.add(Restrictions.like("folio", "%" + pFolio + "%"));
			if(!"".equals(status)) {
				criteria.add(Restrictions.eq("status", status));
			} else {
				criteria.add(Restrictions.ne("status", "RECHAZADO"));
			}
			criteria.addOrder(Order.desc("id"));
			return criteria.list();
		} else {
			return null;
		}
		
		/*
		if("FACTURA".equals(documentType) || "FACT EXTRANJERO".equals(documentType)) {			
			if(!"".equals(uuid)) criteria.add(Restrictions.like("uuidFactura", "%" + uuid + "%"));			
			if(!"".equals(status)) criteria.add(Restrictions.eq("status", status));
			if(!"".equals(documentType)) criteria.add(Restrictions.eq("type", documentType));
			if(!"".equals(addressNumber)) criteria.add(Restrictions.like("addressNumber", "%" + addressNumber + "%"));
			
			return criteria.list();
			//criteria.add(
					//Restrictions.conjunction()
					//.add(Restrictions.like("addressNumber", "%" + addressNumber + "%"))
					//.add(Restrictions.like("uuidFactura", "%" + uuid + "%"))
					////.add(Restrictions.eq("status", status))
					//);
		}else if("NOTACREDITO".equals(documentType)) {
			if(!"".equals(status)) criteria.add(Restrictions.eq("status", status));
			if(!"".equals(addressNumber)) criteria.add(Restrictions.like("addressNumber", "%" + addressNumber + "%"));
			if(!"".equals(documentType)) criteria.add(Restrictions.eq("type", documentType));
			
			return criteria.list();			
		}		
		return null;
		*/
	}
	
	@SuppressWarnings("rawtypes")
	public int getTotalRecords(String addressNumber, String status, String uuid, String documentType, int start, int limit, String pFolio) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
	            .setFetchMode("FiscalDocumentsConcept", FetchMode.SELECT);
		criteria.setFirstResult(start);
		criteria.setMaxResults(limit);
		
		if(!"".equals(uuid) || !"".equals(status) || !"".equals(documentType) || !"".equals(addressNumber)|| !"".equals(pFolio)) {
			if(!"".equals(uuid)) criteria.add(Restrictions.eq("uuidFactura", uuid));
			if(!"".equals(documentType)) criteria.add(Restrictions.eq("type", documentType));
			if(!"".equals(addressNumber)) criteria.add(Restrictions.like("addressNumber", "" + addressNumber + ""));
			if(!"".equals(pFolio)) criteria.add(Restrictions.like("folio", "%" + pFolio + "%"));
			if(!"".equals(status)) {
				criteria.add(Restrictions.eq("status", status));
			} else {
				criteria.add(Restrictions.ne("status", "RECHAZADO"));
			}
			criteria.setProjection(Projections.rowCount());

            List result = criteria.list();
            if (!result.isEmpty()) {
                Long rowCount = (Long) result.get(0);
                return rowCount.intValue();
            } else {
            	return 0;
            }
		} else {
			return 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getPendingPaymentInvoices(String addressNumber,
					                                       String folio) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
	            .setFetchMode("FiscalDocumentsConcept", FetchMode.SELECT);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.like("addressNumber", "%" + addressNumber + "%"))
				.add(Restrictions.eq("uuidPago", ""))
				.add(Restrictions.ne("folio", folio.trim()))
				);
		return criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getComplPendingInvoice(String addressBook) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressNumber",addressBook))
				.add(Restrictions.ne("paymentAmount",0d)));

		criteria.add(Restrictions.or(Restrictions.isNotNull("uuidFactura"), Restrictions.ne("uuidFactura", "")));
		criteria.add(Restrictions.eq("status", AppConstants.STATUS_PAID));
		criteria.addOrder(Order.asc("invoiceUploadDate"));
		return criteria.list();
	}
	
	public void saveDocument(FiscalDocuments o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(o);
	}
	
	public void updateDocument(FiscalDocuments o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(o);
	}
	
	public void deleteFiscalDocument(FiscalDocuments o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.delete(o);
	}

	public FiscalDocuments getFiscalDocumentsByUuid(String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.eq("uuidFactura", uuid))
				);
		@SuppressWarnings("unchecked")
		List<FiscalDocuments> list =  criteria.list();
		if(!list.isEmpty()) {
			return list.get(0);
		}else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getPaymentPendingFiscalDocuments(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		criteria.add(Restrictions.isNotNull("uuidFactura"));
		criteria.add(Restrictions.ne("uuidFactura", ""));
		criteria.add(Restrictions.eq("status", AppConstants.FISCAL_DOC_APPROVED));
		criteria.setFirstResult(start);
		criteria.setMaxResults(limit);
		List<FiscalDocuments> list = criteria.list();
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<FiscalDocuments> getPaymentPendingReceiptsSOC(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		criteria.add(Restrictions.isNotNull("uuidFactura"));
		criteria.add(Restrictions.ne("uuidFactura", ""));
		criteria.add(Restrictions.eq("status", AppConstants.FISCAL_DOC_APPROVED));
		criteria.add(Restrictions.eq("orderType", "PUO"));
		criteria.setFirstResult(start);
		criteria.setMaxResults(limit);
		List<FiscalDocuments> list = criteria.list();
		return list;
	}
	
	public void updateReceiptPaymentList(List<FiscalDocuments> list) {
	Session session = this.sessionFactory.getCurrentSession();
	int i=0;
	session.setCacheMode(CacheMode.IGNORE);
	session.setFlushMode(FlushMode.COMMIT);
	for(FiscalDocuments o : list) {
//		PurchaseOrder ao = searchbyOrderAndAddressBookAndType(o.getOrderNumber(), o.getAddressNumber(), o.getOrderType());
//		ao.setOrderStauts(AppConstants.STATUS_OC_PAID);
//		ao.setRelatedStatus(AppConstants.STATUS_COMPLETE);
	    session.saveOrUpdate(o);
//	    session.saveOrUpdate(ao);
	    if( i % 50 == 0 ) {
		      session.flush();
		      session.clear();
		   }
	    i++; 
	  }

      session.flush();
      session.clear();
}	
	
	public List<FiscalDocuments> searchFiscalDocsByQuery(String uuid, String supplierNumber, int start, int limit, Date poFromDate, Date poToDate, String pFolio){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		
		criteria.add(
			    Restrictions.conjunction()
			        .add(Restrictions.isNotNull("uuidFactura"))
			        .add(Restrictions.ne("uuidFactura", ""))
			);

		
		if(!"".equals(supplierNumber)) {
			criteria.add(Restrictions.eq("addressNumber", supplierNumber));
		}		

		if(!"".equals(uuid)) {
			criteria.add(Restrictions.eq("uuidFactura", uuid));
		}
		
		if(!"".equals(pFolio)) {
			 criteria.add(Restrictions.eq("folio", pFolio));
		}
				
		if(poFromDate != null) {
			criteria.add(Restrictions.ge("invDate", poFromDate));
		}
		
		if(poToDate != null) {
			criteria.add(Restrictions.le("invDate", poToDate));
		}
		
		criteria.setFirstResult(start); 
		criteria.setMaxResults(limit);
		
		criteria.addOrder(Order.desc("invoiceUploadDate"));
				
		return (List<FiscalDocuments>) criteria.list();

		
	}
	
	public int searchFiscalDocsByQueryCount(String uuid, String supplierNumber, Date poFromDate, Date poToDate, String pFolio){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(FiscalDocuments.class);
		
		criteria.add(
			    Restrictions.conjunction()
			        .add(Restrictions.isNotNull("uuidFactura"))
			        .add(Restrictions.ne("uuidFactura", ""))
			);

		
		if(!"".equals(supplierNumber)) {
			criteria.add(Restrictions.eq("addressNumber", supplierNumber));
		}		

		if(!"".equals(uuid)) {
			criteria.add(Restrictions.eq("uuidFactura", uuid));
		}
		
		if(!"".equals(pFolio)) {
			 criteria.add(Restrictions.eq("folio", pFolio));
		}
				
		if(poFromDate != null) {
			criteria.add(Restrictions.ge("invDate", poFromDate));
		}
		
		if(poToDate != null) {
			criteria.add(Restrictions.le("invDate", poToDate));
		}
		
		Long count = (Long)criteria.setProjection(Projections.rowCount()).uniqueResult();
		return count.intValue();

		
	}

	
}
