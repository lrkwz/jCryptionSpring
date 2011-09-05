/**
 * 
 */
package com.linkwithweb.encryption;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Ashwin Kumar
 * 
 */
public class JCryptionUtil {

	/**
	 * Constructor
	 */
	public JCryptionUtil() {
		java.security.Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Generates the Keypair with the given keyLength.
	 * 
	 * @param keyLength
	 *            length of key
	 * @return KeyPair object
	 * @throws RuntimeException
	 *             if the RSA algorithm not supported
	 */
	public KeyPair generateKeypair(int keyLength) {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keyLength);
			KeyPair keyPair = kpg.generateKeyPair();
			/*
			 * KeyFactory keyFac = KeyFactory.getInstance("RSA");
			 * try {
			 * RSAPrivateCrtKeySpec pkSpec = (RSAPrivateCrtKeySpec) keyFac
			 * .getKeySpec(keyPair.getPrivate(),
			 * RSAPrivateCrtKeySpec.class);
			 * 
			 * System.out.println("Prime exponent p : "
			 * + byteArrayToHexString(pkSpec.getPrimeExponentP().toByteArray()));
			 * System.out.println("Prime exponent q : "
			 * + byteArrayToHexString(pkSpec.getPrimeExponentQ().toByteArray()));
			 * System.out.println("Modulus : " + byteArrayToHexString(pkSpec.getModulus().toByteArray()));
			 * System.out.println("Private exponent : "
			 * + byteArrayToHexString(pkSpec.getPrivateExponent().toByteArray()));
			 * System.out.println("Public exponent : "
			 * + byteArrayToHexString(pkSpec.getPublicExponent().toByteArray()));
			 * 
			 * } catch (InvalidKeySpecException e) {
			 * // TODO Auto-generated catch block
			 * e.printStackTrace();
			 * }
			 */

			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("RSA algorithm not supported", e);
		}
	}

	/**
	 * Decrypts a given string with the RSA keys
	 * 
	 * @param encrypted
	 *            full encrypted text
	 * @param keys
	 *            RSA keys
	 * @return decrypted text
	 * @throws RuntimeException
	 *             if the RSA algorithm not supported or decrypt operation failed
	 */
	public static String decrypt(String encrypted, KeyPair keys) {
		Cipher dec;
		try {
			dec = Cipher.getInstance("RSA/NONE/NoPadding");
			dec.init(Cipher.DECRYPT_MODE, keys.getPrivate());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("RSA algorithm not supported", e);
		}
		String[] blocks = encrypted.split("\\s");
		StringBuffer result = new StringBuffer();
		try {
			for (int i = blocks.length - 1; i >= 0; i--) {
				byte[] data = hexStringToByteArray(blocks[i]);
				byte[] decryptedBlock = dec.doFinal(data);
				result.append(new String(decryptedBlock));
			}
		} catch (GeneralSecurityException e) {
			throw new RuntimeException("Decrypt error", e);
		}
		/**
		 * Some code is getting added in first 2 digits with Jcryption need to investigate
		 */
		return result.reverse().toString().substring(2);
	}

	/**
	 * Parse url string (Todo - better parsing algorithm)
	 * 
	 * @param url
	 *            value to parse
	 * @param encoding
	 *            encoding value
	 * @return Map with param name, value pairs
	 */
	public static Map<String, String> parse(String url, String encoding) {
		try {
			String urlToParse = URLDecoder.decode(url, encoding);
			String[] params = urlToParse.split("&");
			Map<String, String> parsed = new HashMap<String, String>();
			for (int i = 0; i < params.length; i++) {
				String[] p = params[i].split("=");
				String name = p[0];
				String value = (p.length == 2) ? p[1] : null;
				parsed.put(name, value);
			}
			return parsed;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unknown encoding.", e);
		}
	}

	/**
	 * Return public RSA key modulus
	 * 
	 * @param keyPair
	 *            RSA keys
	 * @return modulus value as hex string
	 */
	public static String getPublicKeyModulus(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		return publicKey.getModulus().toString(16);
	}

	/**
	 * Return public RSA key exponent
	 * 
	 * @param keyPair
	 *            RSA keys
	 * @return public exponent value as hex string
	 */
	public static String getPublicKeyExponent(KeyPair keyPair) {
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		return publicKey.getPublicExponent().toString(16);
	}

	/**
	 * Max block size with given key length
	 * 
	 * @param keyLength
	 *            length of key
	 * @return numeber of digits
	 */
	public static int getMaxDigits(int keyLength) {
		return ((keyLength * 2) / 16) + 3;
	}

	/**
	 * Convert byte array to hex string
	 * 
	 * @param bytes
	 *            input byte array
	 * @return Hex string representation
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			result.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return result.toString();
	}

	/**
	 * Convert hex string to byte array
	 * 
	 * @param data
	 *            input string data
	 * @return bytes
	 */
	public static byte[] hexStringToByteArray(String data) {
		int k = 0;
		byte[] results = new byte[data.length() / 2];
		for (int i = 0; i < data.length();) {
			results[k] = (byte) (Character.digit(data.charAt(i++), 16) << 4);
			results[k] += (byte) (Character.digit(data.charAt(i++), 16));
			k++;
		}
		return results;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JCryptionUtil jCryption = new JCryptionUtil();
		System.out.println(jCryption.toPublicKeyString());
	}

	/**
	 * @return
	 */
	public String toPublicKeyString() {
		KeyPair keys = generateKeypair(512);
		StringBuffer out = new StringBuffer();

		String e = getPublicKeyExponent(keys);
		String n = getPublicKeyModulus(keys);
		String md = String.valueOf(getMaxDigits(512));

		out.append("{\"e\":\"");
		out.append(e);
		out.append("\",\"n\":\"");
		out.append(n);
		out.append("\",\"maxdigits\":\"");
		out.append(md);
		out.append("\"}");

		return out.toString();
	}

}