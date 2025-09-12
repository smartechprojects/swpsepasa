package com.eurest.supplier.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.eurest.supplier.dao.CodigosSatDao;
import com.eurest.supplier.model.CodigosSAT;
import com.eurest.supplier.util.AppConstants;
import com.eurest.supplier.util.Logger;

public class BatchProcessService implements Runnable{

	Workbook workbook = null;
	CodigosSatDao codigosSatDao = null;
	Logger logger = null;
	
	private org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(BatchProcessService.class);
	
	@Override
	public void run() {
		Sheet sheet = null;
		List<CodigosSAT> codes = new ArrayList<CodigosSAT>();
		List<String> addedCodes = new ArrayList<String>();
		int currentRow = 0;
		int newRowCount = 0;
		int count = 0;
		
		try {
			sheet = workbook.getSheet("c_ClaveProdServ");
			Iterator<Row> rowIterator = sheet.iterator();
			while(rowIterator.hasNext()) {
				Row row = rowIterator.next();
				
				//No toma en cuenta el primer registro				
				if(row.getRowNum() > 0) {
					currentRow = row.getRowNum() + 1;
					if(row.getCell(0) != null && row.getCell(1) != null && row.getCell(2) != null) {
						CodigosSAT code = new CodigosSAT();
						String codigoSAT = "";
						String descripcion = "";
						String tipo = "";
						
						if(row.getCell(0).getCellType() == 0) {
							log4j.error("ERROR_CODIGOS_SAT: La columna de clave no debe ser de tipo numérico (Agregue apostrofe (') al inicio del texto). Error en la fila: " + String.valueOf(currentRow));
							break;
						} else {
							if(row.getCell(0).getStringCellValue() != null) {
								codigoSAT = String.valueOf(row.getCell(0).getStringCellValue().trim());
							}
						}

						if(row.getCell(1).getCellType() == 0) {
							descripcion = String.valueOf(row.getCell(1).getNumericCellValue());
						} else {
							if(row.getCell(1).getStringCellValue() != null) {
								descripcion = String.valueOf(row.getCell(1).getStringCellValue().trim());
							}							
						}
						
						if(row.getCell(2).getCellType() == 0) {
							tipo = String.valueOf(row.getCell(2).getNumericCellValue());
						} else {
							if(row.getCell(2).getStringCellValue() != null) {
								tipo = String.valueOf(row.getCell(2).getStringCellValue().trim());
							}							
						}
						
						code.setId(0);
						code.setCodigoSAT(codigoSAT);
						code.setDescripcion(descripcion);
						code.setTipoCodigo(tipo);
						codes.add(code);						
						count = count + 1;
						addedCodes.add(codigoSAT);
					} else {
						break;
					}
				}
			}
			
			if(codes != null && !codes.isEmpty()) {
				List<CodigosSAT> newCodes = new ArrayList<CodigosSAT>();
				List<CodigosSAT> existingCodes = new ArrayList<CodigosSAT>();
				List<String> existCodeList = new ArrayList<String>();
				
				existingCodes = codigosSatDao.findCodes(addedCodes);				
				if(existingCodes != null && !existingCodes.isEmpty()) {
					
					//Se genera lista de Códigos que ya existen
					for(CodigosSAT c : existingCodes) {
						existCodeList.add(c.getCodigoSAT().trim());
					}
					
					//Los agrega a los códigos nuevos si aún no existen
					for(CodigosSAT n : codes) {
						if(!existCodeList.contains(n.getCodigoSAT().trim())) {
							newCodes.add(n);
						}
					}
				} else {
					newCodes = codes;
				}

				if(newCodes != null && !newCodes.isEmpty()) {
					newRowCount = newCodes.size();
					codigosSatDao.saveMultiple(codes);
				}				
			}
			
			log4j.info("CODIGOS_SAT: Registros agregados: " + String.valueOf(newRowCount));
			logger.log(AppConstants.LOG_BATCH_PROCESS, AppConstants.LOG_BATCH_PROCESS_CODSAT + newRowCount);
			
		} catch (Exception e) {
			log4j.error("Exception" , e);
			log4j.error("ERROR_CODIGOS_SAT: Error inesperado en la fila: " + String.valueOf(currentRow));
			e.printStackTrace();
		}
		
	}
	
		
	public void setCodigoSatDao(CodigosSatDao codigosSatDao) {
		this.codigosSatDao = codigosSatDao;
	}
	
	public void setFile(Workbook workbook) {
		this.workbook = workbook;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
