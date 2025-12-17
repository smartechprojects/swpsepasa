package com.eurest.supplier.dao;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.eurest.supplier.dto.UserDocumentDTO;
import com.eurest.supplier.model.CodigosSAT;
import com.eurest.supplier.model.NoticeDocument;
import com.eurest.supplier.model.SupplierDocument;
import com.eurest.supplier.model.UserDocument;
import com.eurest.supplier.util.StringUtils;


@Repository("documentsDao")
@Transactional
public class DocumentsDao{

	@Autowired
	SessionFactory sessionFactory;
	
	Logger log4j = Logger.getLogger(DocumentsDao.class);
	
	@SuppressWarnings("unchecked")
	public List<SupplierDocument> searchSupplierDocument(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(SupplierDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressBook", query))
				//.add(Restrictions.like("documentType", "load%"))
				);
		return (List<SupplierDocument>) criteria.list();
	}
	
	public void updateSupplierDocument(SupplierDocument o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(o);
	}
	
	public SupplierDocument getDocumentSuppById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (SupplierDocument) session.get(SupplierDocument.class, id);
	}
	
	public SupplierDocument saveSupplierDocuments(SupplierDocument o) {
		if(o != null) {
			o.setName(StringUtils.takeOffSpecialChars(o.getName()));
		}
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(o);
	    return o;	
	}
	
	public UserDocument getDocumentById(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		return (UserDocument) session.get(UserDocument.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> getDocumentsList(int start, int limit) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from UserDocument");
	    q.setFirstResult(start); // modify this to adjust paging
	    q.setMaxResults(limit);
		return (List<UserDocument>) q.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> listDocuments(String addressNumber){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("addressBook", "%" + addressNumber + "%"))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteria(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.disjunction()
				.add(Restrictions.like("name", "%" + query + "%"))
				.add(Restrictions.like("fiscalType", "%" + query + "%"))
				.add(Restrictions.like("uuid", "%" + query + "%"))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchByAddressNumber(String query){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressBook", query))
				.add(Restrictions.like("documentType", "load%"))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByOrderNumber(int orderNumber, 
			                                              String orderType, 
			                                              String addressNumber){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("documentNumber", orderNumber))
				.add(Restrictions.eq("documentType", orderType))
				.add(Restrictions.eq("addressBook", addressNumber))
				);
		criteria.addOrder(Order.desc("id"));
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByUuidOnly(String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("uuid", uuid))
				);

		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public UserDocument searchInvXMLByUuidOnly(String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("uuid", uuid))
				.add(Restrictions.eq("type", "text/xml"))
				.add(Restrictions.eq("fiscalType", "Factura"))
				);

		List<UserDocument> list = (List<UserDocument>) criteria.list();
		if(list != null) {
			if(list.size() > 0) {
				return list.get(0);
			}
		}else {
			return null;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public UserDocument searchCriteriaByOrderNumberFiscalType(int orderNumber, 
			                                              String orderType, 
			                                              String addressNumber,
			                                              String type){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("documentNumber", orderNumber))
				.add(Restrictions.eq("documentType", orderType))
				.add(Restrictions.eq("addressBook", addressNumber))
				.add(Restrictions.eq("fiscalType", type))
				);
		
		List<UserDocument> list = (List<UserDocument>) criteria.list();
		if(list != null) {
			if(list.size() > 0) {
				return list.get(0);
			}
		}else {
			return null;
		}
		
		return null;

	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByType( String orderType){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("documentType", orderType))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByIdList(List<Integer> idList){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(Restrictions.in("id", idList));
		return (List<UserDocument>) criteria.list();
	}
	
	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByRefFiscal(String addressNumber,
			   String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressBook", addressNumber))
				.add(Restrictions.eq("uuid", uuid))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	public List<UserDocument> searchCriteriaByOrderAndUuid(String addressNumber, int orderNumber, String orderType, String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressBook", addressNumber))
				.add(Restrictions.eq("documentNumber", orderNumber))
				.add(Restrictions.eq("documentType", orderType))
				.add(Restrictions.eq("uuid", uuid))
				);
		return (List<UserDocument>) criteria.list();
	}

	@SuppressWarnings("unchecked")
	public List<UserDocument> searchCriteriaByDescription(String addressNumber, String description){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("addressBook", addressNumber))
				.add(Restrictions.eq("description", description))
				);
		return (List<UserDocument>) criteria.list();
	}
	
	public UserDocument saveDocuments(UserDocument o) {
		if(o != null) {
			o.setName(StringUtils.takeOffSpecialChars(o.getName()));
		}
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(o);
	    return o;	
	}
	
	public NoticeDocument saveNotice(NoticeDocument o) {
		if(o != null) {
			o.setName(StringUtils.takeOffSpecialChars(o.getName()));
		}
		Session session = this.sessionFactory.getCurrentSession();
		session.persist(o);
	    return o;	
	}

	public void updateDocuments(UserDocument o) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(o);
	}

	public void updateDocumentList(List<UserDocument> list) {
		Session session = this.sessionFactory.getCurrentSession();
		for(UserDocument o : list) {
		    session.update(o);
		}
	}
	
	public void deleteDocuments(int id) {
		Session session = this.sessionFactory.getCurrentSession();
		UserDocument p = (UserDocument) session.load(UserDocument.class, new Integer(id));
		if(null != p){
			session.delete(p);
		}
	}
	
	public int getTotalRecords(){
		Session session = this.sessionFactory.getCurrentSession();
		Long count = (Long) session.createQuery("select count(*) from  UserDocument").uniqueResult();
		return count.intValue();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<CodigosSAT> getCodesFromArray(String[] array){
			Session session = this.sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(CodigosSAT.class);
			criteria.add(Restrictions.in("codigoSAT", array));
			return (List<CodigosSAT>) criteria.list();
		}

	@SuppressWarnings("unchecked")
	public UserDocument searchCriteriaByUuidAndPayroll(String uuid){
		Session session = this.sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(UserDocument.class);
		criteria.add(
				Restrictions.conjunction()
				.add(Restrictions.eq("uuid", uuid))
				.add(Restrictions.eq("fiscalType", "REC_NOMINA"))
				);

		List<UserDocument> list = (List<UserDocument>) criteria.list();
		if(list != null) {
			if(list.size() > 0) {
				return list.get(0);
			}
		}else {
			return null;
		}
		return null;
	}
@SuppressWarnings("unchecked")
	public List<UserDocumentDTO> searchCriteriaByDate(Date poFromDate, Date poToDate,String modo) {
	
			Session session = this.sessionFactory.getCurrentSession();
			List<UserDocumentDTO> lista=new ArrayList<UserDocumentDTO>();
			
			 SimpleDateFormat sdfDestination = new SimpleDateFormat("yyyy-MM-dd");
			 
			 Query q=null;
			 
			 
			 if(modo.equals("aduana")) {
				  q = session.createQuery("SELECT distinct\n" + 
				  		"documentType,\n" + 
				  		"addressBook as addressBook, \n" + 
				  		"documentNumber as documentNumber , \n" + 
				  		"concat(ifnull(CASE WHEN UPPER(TRIM(serie)) = 'NULL' THEN '' ELSE serie END, ''), ifnull(folio, '')) as serie,\n" + 
				  		" uuid ,\n" + 
				  		"uploadDate \n" +
				  		"FROM UserDocument\n" + 
				  		"where uuid is not null and uuid <> '' and uuid not like '%-REJECTED%'\n" + 
				  		"and fiscalType in ('Factura', 'NotaCredito', 'Otros')\n"
				  		+ " and uuid in (select  uuidFactura from FiscalDocuments \n" + 
				  		"where  status in ('APROBADO','PAGADO','COMPLEMENTO')) " + 
				  		"and uploadDate BETWEEN '"+sdfDestination.format(poFromDate)+"' AND '"+sdfDestination.format(poToDate)+"'\n" +
				  		"and documentNumber = 0\n" + 
				  		"order by uuid");
				 
			 }else {
				  q = session.createQuery(" SELECT distinct\n" + 
							"CASE WHEN fiscalType = 'Otros' THEN 'Factura' ELSE fiscalType END as fiscalType,\n" + 
							"addressBook as addressBook, documentNumber as documentNumber, concat(ifnull(CASE WHEN UPPER(TRIM(serie)) = 'NULL' THEN '' ELSE serie END, ''), ifnull(folio, '')) as serie, uuid as UUID,\n" + 
							"uploadDate as uploadDate\n" + 
							"FROM UserDocument\n" +  
							"where uuid is not null and uuid <> '' and uuid not like '%-REJECTED%'\n" + 
							"and fiscalType in ('Factura', 'NotaCredito', 'Otros')\n"
							+ " and uuid not in (select uuid from Receipt\n" + 
							"where paymentStatus like 'PENDING' ) " + 
							"and uploadDate BETWEEN '"+sdfDestination.format(poFromDate)+"' AND '"+sdfDestination.format(poToDate)+"'\n" + 
							"and documentNumber > 0\n" + 
							"order by uuid "); 
				 
			 }
			 
		
			
			 List<UserDocument> list = q.list();
			 
				for (Object userDocument : list) {
					ObjectMapper jsomap=new ObjectMapper();
					String json="";
					UserDocumentDTO doc=null;
					try {
						 json=jsomap.writeValueAsString(userDocument);
						 ArrayList<?> js=jsomap.readValue(json, ArrayList.class);
						 doc=new UserDocumentDTO();
						   doc.setDocumentType(js.get(0)+"");
						   doc.setAddressBook(js.get(1)+"");
						   doc.setDocumentNumber((Integer)js.get(2));
						   doc.setSerie(js.get(3)+"");
						   doc.setUuid(js.get(4)+"");
						   doc.setName(js.get(5)+"");
						 
						  lista.add(doc);	
					} catch (JsonGenerationException e) {
						log4j.error("Exception" , e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						log4j.error("Exception" , e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						log4j.error("Exception" , e);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   

				
				}
				
				
				
		return lista;
		
	}

@SuppressWarnings("unchecked")
public List<UserDocumentDTO> searchCriteriaListFac(String modo) {

		Session session = this.sessionFactory.getCurrentSession();
		List<UserDocumentDTO> lista=new ArrayList<UserDocumentDTO>();
		
		 SimpleDateFormat sdfDestination = new SimpleDateFormat("yyyy-MM-dd");
		 
		 Query q=null;
		 
		 String fecha =LocalDate.now().minusMonths(3).toString();
		 if(modo.equals("aduana")) {
			  q = session.createQuery("SELECT distinct\n" + 
			  		"documentType,\n" + 
			  		"addressBook as addressBook, \n" + 
			  		"documentNumber as documentNumber , \n" + 
			  		"concat(ifnull(CASE WHEN UPPER(TRIM(serie)) = 'NULL' THEN '' ELSE serie END, ''), ifnull(folio, '')) as serie,\n" + 
			  		" uuid ,\n" + 
			  		"uploadDate \n" +
			  		"FROM UserDocument\n" + 
			  		"where uuid is not null and uuid <> '' and uuid not like '%-REJECTED%'\n" + 
			  		"and fiscalType in ('Factura', 'NotaCredito', 'Otros')\n"
			  		+ " and uuid in (select  uuidFactura from FiscalDocuments \n" + 
			  		"where  status in ('APROBADO','PAGADO','COMPLEMENTO')) "  
			  		+ "and uploadDate >=(STR_TO_DATE('"+fecha+"','%Y-%m-%d')) " + 
			  		"and documentNumber = 0\n" + 
			  		"order by uuid");
			 
		 }else {
			  q = session.createQuery(" SELECT distinct\n" + 
						"CASE WHEN fiscalType = 'Otros' THEN 'Factura' ELSE fiscalType END as fiscalType,\n" + 
						"addressBook as addressBook, documentNumber as documentNumber, concat(ifnull(CASE WHEN UPPER(TRIM(serie)) = 'NULL' THEN '' ELSE serie END, ''), ifnull(folio, '')) as serie, uuid as UUID,\n" + 
						"uploadDate as uploadDate\n" + 
						"FROM UserDocument\n" + 
						"where uuid is not null and uuid <> '' and uuid not like '%-REJECTED%'\n" + 
						"and fiscalType in ('Factura', 'NotaCredito', 'Otros')\n"
						+ " and uuid not in (select uuid from Receipt\n" + 
						"where paymentStatus like 'PENDING' ) " 
						+ "and uploadDate >=(STR_TO_DATE('"+fecha+"','%Y-%m-%d')) " + 
						"and documentNumber > 0\n" + 
						"order by uuid "); 
			 
		 }
		 
	
		
		 List<UserDocument> list = q.list();
		 
			for (Object userDocument : list) {
				ObjectMapper jsomap=new ObjectMapper();
				String json="";
				UserDocumentDTO doc=null;
				try {
					 json=jsomap.writeValueAsString(userDocument);
					 ArrayList<?> js=jsomap.readValue(json, ArrayList.class);
					 doc=new UserDocumentDTO();
					   doc.setDocumentType(js.get(0)+"");
					   doc.setAddressBook(js.get(1)+"");
					   doc.setDocumentNumber((Integer)js.get(2));
					   doc.setSerie(js.get(3)+"");
					   doc.setUuid(js.get(4)+"");
					   doc.setName(js.get(5)+"");
					 
					  lista.add(doc);	
				} catch (JsonGenerationException e) {
					log4j.error("Exception" , e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					log4j.error("Exception" , e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					log4j.error("Exception" , e);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   

			
			}
			
			
			
	return lista;
	
}


}
