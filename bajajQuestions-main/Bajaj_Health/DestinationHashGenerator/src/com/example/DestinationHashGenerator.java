package com.example;

import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DestinationHashGenerator {

	private static String findDestinationValue(Object json) {
		if (json instanceof JSONObject) {
			JSONObject jsonObject = (JSONObject) json;
			for (Object key : jsonObject.keySet()) {
				if ("destination".equals(key)) {
					return jsonObject.get(key).toString();
				}

				Object value = jsonObject.get(key);
				String result = findDestinationValue(value);
				if (result != null) {
					return result;
				}
			}
		} else if (json instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) json;
			for (Object value : jsonArray) {
				String result = findDestinationValue(value);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private static String generateRandomString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(characters.charAt(random.nextInt(characters.length())));
		}
		return sb.toString();
	}

	private static String generateMD5Hash(String toHash) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest = md.digest(toHash.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : digest) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java DestinationHashGenerator <PRN Number> <path to JSON file>");
			System.exit(1);
		}
		String prnNum = args[0];
		String filePath = args[1];

		try {
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(new FileReader(filePath));
			JSONObject jsonObject = (JSONObject) obj;

			String destinationValue = findDestinationValue(jsonObject);
			if (destinationValue == null) {
				System.err.println("Key 'destination' not found in the JSON file");
				System.exit(1);
			}

			String randomStr = generateRandomString(8);

			String toHash = prnNum + destinationValue + randomStr;

			String md5Hash = generateMD5Hash(toHash);

			System.out.println(md5Hash + ";" + randomStr);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
