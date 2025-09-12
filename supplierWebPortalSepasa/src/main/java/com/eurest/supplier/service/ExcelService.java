package com.eurest.supplier.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eurest.supplier.dao.PurchaseOrderDao;
import com.eurest.supplier.dto.ReportInvoicesDTO;

@Service("excelService")
public class ExcelService {
		
	@Autowired
	PurchaseOrderDao purchaseOrderDao;
	
	Logger log4j = Logger.getLogger(ExcelService.class);
	
	public void exportReportInvoicesToExcel(HttpServletResponse response,String supplierNumber, Date poFromDate, Date poToDate, String rfc, String rfcReceptor) throws IOException {
	     
		String name = "Reporte_Facturas";
		XSSFWorkbook workbook = createExcelWorkBook(name);
		XSSFSheet excelSheet = createExcelWorkSheet(workbook, name);
		int rowNr = populateExcelHeaderReportInvoices(excelSheet);
		  
		List<ReportInvoicesDTO> list = null;// invoiceDao.searchReportInvoicesToExcel(supplierNumber, poFromDate, poToDate, rfc, rfcReceptor);
		     
	     if(list != null) {
	    	 if(list.size() > 0) {
	    		 populdateExcelDataReportInvoices(excelSheet, list, name, rowNr);
	    	 }
	     }
	     setContentType(name, response);
	     exportExceltoResponse(workbook, response, name);
	}
	
	public int populateExcelHeaderReportInvoices(XSSFSheet excelSheet) {
        int rowNr = 0;
        XSSFRow rowHeader = excelSheet.createRow(rowNr);
        rowHeader.createCell(0).setCellValue("Num Proveedor");
        rowHeader.createCell(1).setCellValue("Razon Social");
        rowHeader.createCell(2).setCellValue("Fecha Factura");
        rowHeader.createCell(3).setCellValue("Tipo Comprobante");
        rowHeader.createCell(4).setCellValue("Serie");
        rowHeader.createCell(5).setCellValue("Folio");
        rowHeader.createCell(6).setCellValue("RFC Emisor");
        rowHeader.createCell(7).setCellValue("RFC Receptor");
        rowHeader.createCell(8).setCellValue("Razon Social Receptor");
        rowHeader.createCell(9).setCellValue("Moneda");
        rowHeader.createCell(10).setCellValue("Subtotal");
        rowHeader.createCell(11).setCellValue("Impuesto");
        rowHeader.createCell(12).setCellValue("Total");
        rowHeader.createCell(13).setCellValue("UUID");
        rowHeader.createCell(14).setCellValue("Metodo Pago");
        rowHeader.createCell(15).setCellValue("Forma Pago");
        rowHeader.createCell(16).setCellValue("Complemento de pago cargado");
        rowHeader.createCell(17).setCellValue("Factura en portal");
        rowHeader.createCell(18).setCellValue("Num Orden");
        
        return rowNr;
	}
	
    public void populdateExcelDataReportInvoices(XSSFSheet excelSheet, List<ReportInvoicesDTO> list, String reportName, int rowNr) {
    	XSSFWorkbook workbook = excelSheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();  
    	DataFormat dateFormat = workbook.createDataFormat();
    	dateCellStyle.setDataFormat(dateFormat.getFormat("dd-mm-yyyy"));

    	for(ReportInvoicesDTO o : list) {
        	XSSFRow row = excelSheet.createRow(++rowNr);
            row.createCell(0).setCellValue(o.getAddressBook());
            row.createCell(1).setCellValue(o.getRazonSocial());
            row.createCell(2).setCellValue(o.getInvoiceDate());
            row.createCell(3).setCellValue(o.getTipoComprobante());
            row.createCell(4).setCellValue(o.getSerie());
            row.createCell(5).setCellValue(o.getFolio());
            row.createCell(6).setCellValue(o.getRfcEmisor());
            row.createCell(7).setCellValue(o.getRfcReceptor());
            row.createCell(8).setCellValue(o.getRazonSocialReceptor());
            row.createCell(9).setCellValue(o.getMoneda());            
            row.createCell(10).setCellValue(o.getSubtotal()); 
            row.createCell(11).setCellValue(o.getImpuestos());
            row.createCell(12).setCellValue(o.getTotal());            
            row.createCell(13).setCellValue(o.getUuid());
            row.createCell(14).setCellValue(o.getPaymentType());
            row.createCell(15).setCellValue(o.getFormaPago());
            row.createCell(16).setCellValue(o.getCompCargado());
            row.createCell(17).setCellValue(o.getFacturaPortal());
            row.createCell(18).setCellValue(o.getOrderNumber());
    	}
    	for(int i = 0; i < 13; i++) {
    		excelSheet.autoSizeColumn(i);
    	}
    }
    
    /*public void exportReportPOToExcel(HttpServletResponse response,String supplierNumber) throws IOException {
	     
		String name = "Reporte_OrdenesCompra";
		XSSFWorkbook workbook = createExcelWorkBook(name);
		XSSFSheet excelSheet = createExcelWorkSheet(workbook, name);
		int rowNr = populateExcelHeaderReportPO(excelSheet);
		  
		List<ReportPurchaseOrderDTO> list =  null;//purchaseOrderDao.searchReportPOToExcel(supplierNumber);
		     
	     if(list != null) {
	    	 if(list.size() > 0) {
	    		 populateExcelDataReportPO(excelSheet, list, name, rowNr);
	    	 }
	     }
	     setContentType(name, response);
	     exportExceltoResponse(workbook, response, name);
	}*/
    
    public int populateExcelHeaderReportPO(XSSFSheet excelSheet) {
        int rowNr = 0;
        XSSFRow rowHeader = excelSheet.createRow(rowNr);
        rowHeader.createCell(0).setCellValue("Num Proveedor");
        rowHeader.createCell(1).setCellValue("Num Orden");
        rowHeader.createCell(2).setCellValue("Num Recibo");
        rowHeader.createCell(3).setCellValue("Moneda");
        rowHeader.createCell(4).setCellValue("Monto OC");
        rowHeader.createCell(5).setCellValue("Monto Recibo");
        rowHeader.createCell(6).setCellValue("UUID");
        rowHeader.createCell(7).setCellValue("Monto Factura");
        
        return rowNr;
	}
    
   /* public void populateExcelDataReportPO(XSSFSheet excelSheet, List<ReportPurchaseOrderDTO> list, String reportName, int rowNr) {
    	XSSFWorkbook workbook = excelSheet.getWorkbook();
        CellStyle dateCellStyle = workbook.createCellStyle();  
    	DataFormat dateFormat = workbook.createDataFormat();
    	dateCellStyle.setDataFormat(dateFormat.getFormat("dd-mm-yyyy"));

    	for(ReportPurchaseOrderDTO o : list) {
        	XSSFRow row = excelSheet.createRow(++rowNr);
            row.createCell(0).setCellValue(o.getAddressNumber());
            row.createCell(1).setCellValue(o.getOrderNumber());
            row.createCell(2).setCellValue(o.getDocumentNumber());
            row.createCell(3).setCellValue(o.getCurrecyCode());
            row.createCell(4).setCellValue(o.getOrderAmount());
            row.createCell(5).setCellValue(o.getReceiptAmount());
            row.createCell(6).setCellValue(o.getUuid());
            row.createCell(7).setCellValue(o.getInvoiceAmount());
            
    	}
    	for(int i = 0; i < 13; i++) {
    		excelSheet.autoSizeColumn(i);
    	}
    }*/
	
	private void exportExceltoResponse(XSSFWorkbook workbook , HttpServletResponse response, String reportName){
        ServletOutputStream out;
        try {
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
        	log4j.error("Exception" , e);
            e.printStackTrace();
        }
    }
	    
	private XSSFSheet createExcelWorkSheet(XSSFWorkbook workbook,  String excelName){
	        XSSFSheet excelSheet = workbook.createSheet(excelName);  
	        return excelSheet;
	}
	
	private XSSFWorkbook  createExcelWorkBook(String excelName){
	        XSSFWorkbook workbook = new XSSFWorkbook();
	        return workbook;
	 }
		
    private void setContentType(String excelName, HttpServletResponse response){
        if(excelName != null && excelName.trim().contains(" ")){
            excelName = excelName.trim().replaceAll(" ", "_");
        }
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename="+excelName+".xlsx");    
    }

}
