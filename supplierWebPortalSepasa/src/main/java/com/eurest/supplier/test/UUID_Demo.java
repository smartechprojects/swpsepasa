package com.eurest.supplier.test;

import java.util.*;

import com.eurest.supplier.util.StringUtils;

public class UUID_Demo {
	public static void main(String[] args)
	{

		  String prefix = StringUtils.randomString(8);
		  String sufix = "/" + StringUtils.randomString(6) + "==";
		  String midStr = StringUtils.randomString(3) + "a/";
		  String str = prefix + "-AINJ730511MS6-" + midStr + "-2343-" + "435342523-" + sufix;
		  
		  String encodedString = Base64.getEncoder().encodeToString(str.getBytes());

		// Displaying the UUID
		System.out.println("Encoded: " + encodedString + "/ad" + StringUtils.randomString(3) + "==");
		
		
		byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
		String decodedString = new String(decodedBytes);
		
		System.out.println("Decoded: " + decodedString);
		
	}
}

