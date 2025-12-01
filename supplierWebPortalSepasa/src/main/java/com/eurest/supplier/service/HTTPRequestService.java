package com.eurest.supplier.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.eurest.supplier.util.AppConstants;

@Service("HTTPRequestService")
public class HTTPRequestService {

	private Logger log4j = Logger.getLogger(HTTPRequestService.class);
			
	public String httpPost(String destUrl, String postData) throws Exception {
		URL url = new URL(destUrl);
		String response = "";
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn == null) {
			return null;
		}
		conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setAllowUserInteraction(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("User-Agent", "Java Client");

		OutputStream out = conn.getOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		writer.write(postData);
		writer.close();
		out.close();

		int state = conn.getResponseCode();
        if (state == HttpURLConnection.HTTP_OK) {
    		InputStream in = conn.getInputStream();
    		InputStreamReader iReader = new InputStreamReader(in);
    		BufferedReader bReader = new BufferedReader(iReader);

    		String line;
    		log4j.info("==================Service response: ================ ");
    		while ((line = bReader.readLine()) != null) {
    			log4j.info(line);
    			response += line;
    		}
    		iReader.close();
    		bReader.close();
    		in.close();
    		conn.disconnect();
    		return response;
        }else {
        	return"ERROR";
        }
	}

	public Map<String, byte[]> httpPostFileDownload(String destUrl, String postData, String usr, String pwd) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
		Map<String, byte[]> mapResult = new HashMap<String, byte[]>();

		try {
	        URL url = new URL(destUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Accept", "application/octet-stream"); // o application/pdf
	        conn.setRequestProperty("Content-Type", "application/json");
	        
	        String auth = usr + ":" + pwd;
	        String encodedAuth = javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes("UTF-8"));
	        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

	        // Enviar JSON
	        try (OutputStream os = conn.getOutputStream()) {
	            os.write(postData.getBytes("UTF-8"));
	        }
	        
	        int status = conn.getResponseCode();

	        if (status == HttpURLConnection.HTTP_OK) {
	            // Leer Binario
	        	String contentDisposition = conn.getHeaderField("Content-Disposition");
	        	String fileName = AppConstants.DEFAULT_NO_NAME_FILE + "_" + sdf.format(new Date()) + ".pdf"; // Valor por defecto

	        	if (contentDisposition != null && contentDisposition.contains("filename=")) {
	        	    fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9).replace("\"", "");
	        	}

	        	log4j.info("Archivo Recibido: " + fileName + " Request: " + postData);
	        	
	        	//Obtener Arreglo de Bytes
	        	byte[] byteArrayFile;
	        	try (InputStream is = conn.getInputStream();
	        	     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

	        	    byte[] buffer = new byte[4096];
	        	    int len;

	        	    while ((len = is.read(buffer)) != -1) {
	        	        baos.write(buffer, 0, len);
	        	    }

	        	    byteArrayFile = baos.toByteArray();
	        	    if(byteArrayFile != null) {
	        	    	mapResult.put(fileName, byteArrayFile);
	        	    }
	        	}
	        } else {
	        	//Obtener Error
	            InputStream es = conn.getErrorStream();
	            if (es != null) {
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                byte[] buffer = new byte[4096];
	                int len;

	                while ((len = es.read(buffer)) != -1) {
	                    baos.write(buffer, 0, len);
	                }

	                String error = new String(baos.toByteArray(), "UTF-8");
	                log4j.error("Request: " + postData + " Error: " + error);
	                es.close();
	            }
	        }

	        conn.disconnect();

	    } catch (Exception e) {
        	log4j.error("Exception" , e);
            e.printStackTrace();
	    }
		
		return mapResult;
	}
	
    public String performSoapCall(String xmlContent) {
        try {
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(xmlContent, AppConstants.CFDI_VALIDATION_ACTION), AppConstants.CFDI_VALIDATION_URL);
            soapConnection.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            return new String(out.toByteArray(),"UTF-8");
        } catch (Exception e) {
        	log4j.error("Exception" , e);
            e.printStackTrace();
            return "ERROR";
        }
    }
    
    private static SOAPMessage createSOAPRequest(String payload, String soapAction) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        createSoapEnvelope(payload, soapMessage);
        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", soapAction);
        soapMessage.saveChanges();
        return soapMessage;
    }
    
    private static void createSoapEnvelope(String payload, SOAPMessage soapMessage) throws SOAPException {
    	SOAPPart soapPart = soapMessage.getSOAPPart();
        String myNamespace = "tem";
        String myNamespaceURI = "http://tempuri.org/";
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("Consulta", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("expresionImpresa", myNamespace);
        soapBodyElem1.addTextNode(payload);
    }
}
