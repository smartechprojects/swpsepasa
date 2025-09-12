package com.eurest.supplier.security;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import net.sf.json.JSONException;

public class VerifyRecaptcha {

	//TEST
	public static final String url = "https://www.google.com/recaptcha/api/siteverify";
	public static final String secret = "6LedD8giAAAAAOp6UWxs_zMyKi_IfVZAgSLT1pfy";
	private final static String USER_AGENT = "Mozilla/5.0";

	public static boolean verify(String gRecaptchaResponse) throws IOException {
		if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
			return false;
		}
		
		try{
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		try {
		     JSONObject jsonObject = new JSONObject(response.toString());
		     return jsonObject.getBoolean("success");
		}catch (JSONException err){
		    return false;
		}

		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
