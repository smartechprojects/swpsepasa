package com.eurest.supplier.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class TestString {

	public static void main(String[] args) {

		  String myDriver = "com.mysql.jdbc.Driver";
	      String myUrl = "jdbc:mysql://sm-db-test.crvhin3ktmx2.us-east-1.rds.amazonaws.com:3306/portalsaavidb";
	      try {
			Class.forName(myDriver);
		      Connection conn = DriverManager.getConnection(myUrl, "smartechdbtst", "smart$dbT5t");
		      File file = new File("C:/temp/2021-11-12_ComunicadoApCierreDeAño_EN.pdf");
		      FileInputStream fis = new FileInputStream(file);
		      String query = " insert into userdocument (accept, addressBook, content, description, documentNumber,  documentType, fiscalRef,"
		      		+ "fiscalType, folio, name, serie, size, status, type, uuid)"
		    	        + " values (?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?)";
		      
		      PreparedStatement preparedStmt = conn.prepareStatement(query);
		      
		      preparedStmt.setBoolean(1, true);
		      preparedStmt.setString (2, "12345678");
		      preparedStmt.setBinaryStream(3, fis, (int) file.length());
		      preparedStmt.setString (4, "");
		      preparedStmt.setInt    (5, 0);
		      preparedStmt.setString (6, "EOYNOTIF");
		      preparedStmt.setInt (7, 0);
		      preparedStmt.setString (8, "");
		      preparedStmt.setString (9, "");
		      preparedStmt.setString (10, "2021-11-12_ComunicadoApCierreDeAño_EN.pdf");
		      preparedStmt.setString (11, "");
		      preparedStmt.setInt(12, (int) file.length());
		      preparedStmt.setBoolean(13, true);
		      preparedStmt.setString (14, "application/pdf");
		      preparedStmt.setString (15, "");
		      
		      preparedStmt.execute();
		      conn.close();

		      
		      @SuppressWarnings("unused")
			int i = 0;
		      
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
		
	}
}
