package edu.ucmo.mathcs.onetimepin;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Calendar;
import java.util.Date;

@Configuration
@PropertySource("classpath:application.properties")
public class Utils {
	
	private static int expireTime;
	
	@Value("${expire-time}")
	public void setExpireTime(int expireTime) {
		Utils.expireTime = expireTime;
	}
	
	private static Calendar c = Calendar.getInstance();
	
	public static String generatePin() {
		String pin = RandomStringUtils.randomNumeric(5);
		
		return pin + calculateCheckDigit(pin);
	}
	
	public static Date getCurrentDate() {
		Date date = new Date();
		c.setTime(date);
		
		return date;
	}
	
	public static Date getExpireDate() {
		c.add(Calendar.MINUTE, expireTime);
		
		return c.getTime();
	}
	
	public static boolean luhnCheck(String pin) {
		if (pin == null) return false;
		
		char checkDigit = pin.charAt(pin.length() - 1);
		
		String digit = calculateCheckDigit(pin.substring(0, pin.length() - 1));
		
		return checkDigit == digit.charAt(0);
	}
	
	private static String calculateCheckDigit(String pin) {
		if (pin == null) return null;
		
		String digit;
		
		int[] digits = new int[pin.length()];
		
		for (int i = 0; i < pin.length(); i++) {
			digits[i] = Character.getNumericValue(pin.charAt(i));
		}
		
		for (int i = digits.length - 1; i >= 0; i -= 2) {
			digits[i] += digits[i];
			
			if (digits[i] >= 10) digits[i] = digits[i] - 9;
		}
		
		int sum = 0;
		
		for (int d : digits) {
			sum += d;
		}
		
		sum = sum * 9;
		
		digit = sum + "";
		
		return digit.substring(digit.length() - 1);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
}
