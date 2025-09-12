package com.eurest.supplier.test;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.eurest.supplier.util.AppConstants;

public class OtherSOAPTest {

public static void main(String[] args) {
		
		
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001C00.pdf")));
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001CC1.pdf")));
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001CC2.pdf")));
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001CC3.pdf")));
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001CD1.pdf")));
        map.add("files", new FileSystemResource(new File("d://pdfFiles//20200817C034001CD2.pdf")));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.exchange(AppConstants.URL_HOST + "/supplierWebPortalRestSaavi/loadFiles", HttpMethod.POST, requestEntity, String.class);

}

}
