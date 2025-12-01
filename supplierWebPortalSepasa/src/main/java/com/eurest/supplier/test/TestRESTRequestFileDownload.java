package com.eurest.supplier.test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestRESTRequestFileDownload {

	public static void main(String[] args) {
		
		String destUrl = "https://hamejdeorchpy.endtoend.com.mx/jderest/orchestrator/JDE_ORCH_55_OCPPMexico";

        // JSON de ejemplo
		String postData = "{\r\n" + 
				"  \"nit_proveedor\": \"1550288-1\",\r\n" +
				"  \"empresa_oc\": \"00074\",\r\n" + 
				"  \"tipo_oc\": \"OP\",\r\n" + 
				"  \"numero_oc\": \"23193\"\r\n" + 
				"}";
		
		try {
	        URL url = new URL(destUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Accept", "application/octet-stream"); // o application/pdf
	        conn.setRequestProperty("Content-Type", "application/json");
	        
	        String user = "Sconsultin";
	        String pass = "Hame2025*";
	        String auth = user + ":" + pass;
	        String encodedAuth = javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes("UTF-8"));
	        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

	        // Enviar JSON
	        try (OutputStream os = conn.getOutputStream()) {
	            os.write(postData.getBytes("UTF-8"));
	        }

	        int status = conn.getResponseCode();
	        System.out.println("Response code: " + status);

	        if (status == HttpURLConnection.HTTP_OK) {

	            // Leer Binario
	        	String contentDisposition = conn.getHeaderField("Content-Disposition");
	        	String fileName = "archivo.pdf"; // valor por defecto

	        	if (contentDisposition != null && contentDisposition.contains("filename=")) {
	        	    fileName = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9).replace("\"", "");
	        	}

	        	System.out.println("Nombre recibido: " + fileName);
	        	
	        	//Obtener Arreglo de Bytes
	        	byte[] pdfBytes;
	        	try (InputStream is = conn.getInputStream();
	        	     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

	        	    byte[] buffer = new byte[4096];
	        	    int len;

	        	    while ((len = is.read(buffer)) != -1) {
	        	        baos.write(buffer, 0, len);
	        	    }

	        	    pdfBytes = baos.toByteArray();
	        	}
	        	
	        	/*
	        	//Guardar el archivo
	            try (InputStream is = conn.getInputStream();
	                 FileOutputStream fos = new FileOutputStream(fileName)) {

	                byte[] buffer = new byte[4096];
	                int bytesRead;

	                while ((bytesRead = is.read(buffer)) != -1) {
	                    fos.write(buffer, 0, bytesRead);
	                }

	                System.out.println("PDF guardado como " + fileName);//Se guarda en la ruta del proyecto
	            }
	            */
	        } else {
	            System.out.println("Error: " + status);
	            InputStream es = conn.getErrorStream();
	            if (es != null) {
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                byte[] buffer = new byte[4096];
	                int len;

	                while ((len = es.read(buffer)) != -1) {
	                    baos.write(buffer, 0, len);
	                }

	                String error = new String(baos.toByteArray(), "UTF-8");
	                System.out.println("Error: " + error);

	                es.close();
	            }
	        }

	        conn.disconnect();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}
