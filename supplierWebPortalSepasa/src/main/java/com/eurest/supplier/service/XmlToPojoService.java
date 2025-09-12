package com.eurest.supplier.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dto.InvoiceDTO;
import com.eurest.supplier.invoiceXml.Comprobante;
import com.eurest.supplier.invoiceXml.Pagos;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Service("xmlToPojoService")
public class XmlToPojoService {
	
	Logger log4j = Logger.getLogger(XmlToPojoService.class);
	
	public InvoiceDTO convert(String xml){
		
		Comprobante c = new Comprobante();
		InvoiceDTO inv = new InvoiceDTO();
		try {
			
			String source = takeOffBOM(IOUtils.toInputStream(xml,"UTF-8"));
			String xmlString = source.replace("?<?xml", "<?xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(Comprobante.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(xmlString);
			c = (Comprobante) unmarshaller.unmarshal(reader);
			
			inv.setVersion(c.getVersion());
			inv.setEmisor(c.getEmisor());
			inv.setReceptor(c.getReceptor());			
			inv.setFolio(c.getFolio());
			inv.setSerie(c.getSerie() + "");
			inv.setTotal(Double.parseDouble(c.getTotal()));
			inv.setSubTotal(Double.parseDouble(c.getSubTotal()));
			if(c.getDescuento() != null)
			       inv.setDescuento(Double.parseDouble(c.getDescuento()));
			
			if(c.getTipoCambio() != null)
				inv.setTipoCambio(Double.parseDouble(c.getTipoCambio()));
			
			inv.setTipoComprobante(c.getTipoDeComprobante());
			inv.setMoneda(c.getMoneda());
			inv.setMetodoPago(c.getMetodoPago());
			inv.setFormaPago(c.getFormaPago());
			inv.setRfcEmisor(c.getEmisor().getRfc());
			inv.setRfcReceptor(c.getReceptor().getRfc());
			inv.setComplemento(c.getComplemento());
			if(c.getCfdiRelacionados() != null) {
				if(c.getCfdiRelacionados().getCfdiRelacionado()!= null) {
					inv.setCfdiRelacionado(c.getCfdiRelacionados().getCfdiRelacionado().getCfdiUUID());
				}
			}
			
			if(c.getImpuestos() != null) {
				if(c.getImpuestos().getTotalImpuestosTrasladados() != null) {
					inv.setTotalImpuestos(Double.parseDouble(c.getImpuestos().getTotalImpuestosTrasladados()));
				} else {
					inv.setTotalImpuestos(0);
				}
								
				if(c.getImpuestos().getTotalImpuestosRetenidos() != null) {
					inv.setTotalRetenidos(Double.parseDouble(c.getImpuestos().getTotalImpuestosRetenidos()));
				} else {
					inv.setTotalRetenidos(0);
				}
			} else {
				inv.setTotalImpuestos(0);
				inv.setTotalRetenidos(0);
			}
			
			if(c.getComplemento() != null){
				inv.setUuid(c.getComplemento().getTimbreFiscalDigital().getUUID());
				inv.setFechaTimbrado(c.getComplemento().getTimbreFiscalDigital().getFechaTimbrado());
				
				//Impuestos Locales
				if(c.getComplemento().getImpuestosLocales() != null){
					if(c.getComplemento().getImpuestosLocales().getTotaldeTraslados() != null) {
						inv.setTotalImpLocTraslados(Double.parseDouble(c.getComplemento().getImpuestosLocales().getTotaldeTraslados()));
					}
					
					if(c.getComplemento().getImpuestosLocales().getTotaldeRetenciones() != null) {
						inv.setTotalImpLocRetenidos(Double.parseDouble(c.getComplemento().getImpuestosLocales().getTotaldeRetenciones()));
					}
				}
			}

			inv.setFecha(c.getFecha());
			inv.setConcepto(c.getConceptos().getConcepto());
			if(c.getImpuestos() != null){
				if(c.getImpuestos().getTotalImpuestosTrasladados() != null) {
					inv.setImpuestos(Double.parseDouble(c.getImpuestos().getTotalImpuestosTrasladados()));
				} else {
					inv.setImpuestos(0);
				}				
			}
			
			inv.setSello(c.getSello());
			inv.setCertificado(c.getCertificado());
			inv.setLugarExpedicion(c.getLugarExpedicion());
			
			inv.setMessage("La factura " + inv.getFolio() + " ha sido validada de forma exitosa.");
			return inv;
			
		} catch (JAXBException e) {
			log4j.error("Exception" , e);
			inv.setFolio("");
			e.printStackTrace();
			inv.setMessage("La factura tiene errores de validación. Verifique que el archivo cargadose auna factura válida respecto a la versión SAT 3.3");
			return null;
		}catch(Exception e){
			log4j.error("Exception" , e);
			inv.setFolio("");
			e.printStackTrace();
			inv.setMessage("La factura tiene errores de validación. Verifique que el archivo cargadose auna factura válida respecto a la versión SAT 3.3");
			return null;
		}
	}
	
	public InvoiceDTO convertV4(String xml){
		//Comprobante V4.0 
		com.eurest.supplier.invoiceXml4.Comprobante c4 = new com.eurest.supplier.invoiceXml4.Comprobante();
		Comprobante c = new Comprobante();		
		InvoiceDTO inv = new InvoiceDTO();
		
		try {

			String source = takeOffBOM(IOUtils.toInputStream(xml,"UTF-8"));
			String xmlString = source.replace("?<?xml", "<?xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(com.eurest.supplier.invoiceXml4.Comprobante.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(xmlString);
			c4 = (com.eurest.supplier.invoiceXml4.Comprobante) unmarshaller.unmarshal(reader);			
			c = objectCastV4(c4);
			
			inv.setVersion(c.getVersion());
			inv.setEmisor(c.getEmisor());
			inv.setReceptor(c.getReceptor());			
			inv.setFolio(c.getFolio());
			inv.setSerie(c.getSerie() + "");
			inv.setTotal(Double.parseDouble(c.getTotal()));
			inv.setSubTotal(Double.parseDouble(c.getSubTotal()));
			if(c.getDescuento() != null)
			       inv.setDescuento(Double.parseDouble(c.getDescuento()));
			
			if(c.getTipoCambio() != null)
				inv.setTipoCambio(Double.parseDouble(c.getTipoCambio()));
			
			inv.setTipoComprobante(c.getTipoDeComprobante());
			inv.setMoneda(c.getMoneda());
			inv.setMetodoPago(c.getMetodoPago());
			inv.setFormaPago(c.getFormaPago());
			inv.setRfcEmisor(c.getEmisor().getRfc());
			inv.setRfcReceptor(c.getReceptor().getRfc());
			inv.setComplemento(c.getComplemento());
			if(c.getCfdiRelacionados() != null) {
				if(c.getCfdiRelacionados().getCfdiRelacionado()!= null) {
					inv.setCfdiRelacionado(c.getCfdiRelacionados().getCfdiRelacionado().getCfdiUUID());
				}
			}
			
			if(c.getImpuestos() != null) {
				if(c.getImpuestos().getTotalImpuestosTrasladados() != null) {
					inv.setTotalImpuestos(Double.parseDouble(c.getImpuestos().getTotalImpuestosTrasladados()));
				} else {
					inv.setTotalImpuestos(0);
				}
								
				if(c.getImpuestos().getTotalImpuestosRetenidos() != null) {
					inv.setTotalRetenidos(Double.parseDouble(c.getImpuestos().getTotalImpuestosRetenidos()));
				} else {
					inv.setTotalRetenidos(0);
				}
			} else {
				inv.setTotalImpuestos(0);
				inv.setTotalRetenidos(0);
			}				
				
			if(c.getComplemento() != null){
				inv.setUuid(c.getComplemento().getTimbreFiscalDigital().getUUID());
				inv.setFechaTimbrado(c.getComplemento().getTimbreFiscalDigital().getFechaTimbrado());
				
				//Impuestos Locales
				if(c.getComplemento().getImpuestosLocales() != null){
					if(c.getComplemento().getImpuestosLocales().getTotaldeTraslados() != null) {
						inv.setTotalImpLocTraslados(Double.parseDouble(c.getComplemento().getImpuestosLocales().getTotaldeTraslados()));
					}
					
					if(c.getComplemento().getImpuestosLocales().getTotaldeRetenciones() != null) {
						inv.setTotalImpLocRetenidos(Double.parseDouble(c.getComplemento().getImpuestosLocales().getTotaldeRetenciones()));
					}
				}
				
				//Complemento de Pago V2.0
				if(c4.getComplemento() != null && c4.getComplemento().getPago() != null) {
					Pagos p = objectCastPaymentV20(c4.getComplemento().getPago());
					c.getComplemento().setPago(p);
				}
			}

			inv.setFecha(c.getFecha());
			inv.setConcepto(c.getConceptos().getConcepto());
			if(c.getImpuestos() != null){
				if(c.getImpuestos().getTotalImpuestosTrasladados() != null) {
					inv.setImpuestos(Double.parseDouble(c.getImpuestos().getTotalImpuestosTrasladados()));
				} else {
					inv.setImpuestos(0);
				}				
			}
			
			inv.setSello(c.getSello());
			inv.setCertificado(c.getCertificado());
			inv.setLugarExpedicion(c.getLugarExpedicion());
			
			//Campos nuevos V4.0
			inv.setDomicilioFiscalReceptor(c4.getReceptor().getDomicilioFiscalReceptor());
			inv.setRegimenFiscalReceptor(c4.getReceptor().getRegimenFiscalReceptor());
			inv.setMessage("La factura " + inv.getFolio() + " ha sido validada de forma exitosa.");
			return inv;
			
		} catch (JAXBException e) {
			log4j.error("Exception" , e);
			inv.setFolio("");
			e.printStackTrace();
			inv.setMessage("La factura tiene errores de validación. Verifique que el archivo cargadose auna factura válida respecto a la versión SAT 3.3");
			return null;
		}catch(Exception e){
			log4j.error("Exception" , e);
			inv.setFolio("");
			e.printStackTrace();
			inv.setMessage("La factura tiene errores de validación. Verifique que el archivo cargadose auna factura válida respecto a la versión SAT 3.3");
			return null;
		}
	}
	
	public static String takeOffBOM(InputStream inputStream) throws IOException {
	    BOMInputStream bomInputStream = new BOMInputStream(inputStream);
	    return IOUtils.toString(bomInputStream, "UTF-8");
	}

	public Comprobante objectCastV4(com.eurest.supplier.invoiceXml4.Comprobante c4) {
		
		Comprobante c = null;		
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setSerializationInclusion(Include.NON_NULL);
			String jsonInString = jsonMapper.writeValueAsString(c4);
			Gson gson = new Gson();
			c =  gson.fromJson(jsonInString, Comprobante.class);
			
			//Variables que no se obtienen con Gson (seteo manual)
			c.getComplemento().getTimbreFiscalDigital().setUUID(c4.getComplemento().getTimbreFiscalDigital().getUUID());			
			if(c4.getCfdiRelacionados() != null && c4.getCfdiRelacionados().getCfdiRelacionado()!= null) {
				c.getCfdiRelacionados().getCfdiRelacionado().setUUID(c4.getCfdiRelacionados().getCfdiRelacionado().getCfdiUUID());
			}
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			c = null;
		}
		return c;
	}
	
	public Pagos objectCastPaymentV20(com.eurest.supplier.invoiceXml4.Pagos p4) {
		
		Pagos p = null;		
		try {
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.setSerializationInclusion(Include.NON_NULL);
			String jsonInString = jsonMapper.writeValueAsString(p4);
			Gson gson = new Gson();
			p =  gson.fromJson(jsonInString, Pagos.class);
		} catch (Exception e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
			p = null;
		}
		return p;
	}
}
