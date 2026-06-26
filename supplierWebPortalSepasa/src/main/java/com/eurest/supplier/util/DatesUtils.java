package com.eurest.supplier.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

@Component
public class DatesUtils {

	public static Date convertToGMT6(Date oldDate) {

		Date newDate = oldDate;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfGMT6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdfGMT6.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));

			newDate = sdf.parse(sdfGMT6.format(oldDate));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newDate;
	}

	public static Date convertJulianToJava(int julianDate) {

		Date newDate = null;
		try {
			if(julianDate > 0) {
				newDate = JdeJavaJulianDateTools.Methods.JulianDateToJavaDate(julianDate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newDate;
	}
	
}

