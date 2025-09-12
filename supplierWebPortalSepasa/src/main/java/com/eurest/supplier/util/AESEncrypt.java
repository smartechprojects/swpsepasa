package com.eurest.supplier.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt {

	public static String AES_CUSTOM_KEY = "SAAVISECRET1443";
	private static SecretKeySpec secretKey;
	private static byte[] key;
	
	private static org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(AESEncrypt.class);

	public static void setKey(final String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			log4j.error("Exception" , e);
			e.printStackTrace();
		}
	}

	public static String encrypt(final String strToEncrypt, final String secret) throws Exception{
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			log4j.error("Exception" , e);
			throw new Exception(e.getMessage());
		}
	}

	public static String decrypt(final String strToDecrypt, final String secret) throws Exception {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			log4j.error("Exception" , e);
			throw new Exception(e.getMessage());
		}
	}

}
