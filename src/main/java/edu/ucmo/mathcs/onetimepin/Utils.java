package edu.ucmo.mathcs.onetimepin;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.util.Calendar;
import java.util.Date;

@PropertySource("classpath:application.properties")
public class Utils {
	
	@Value("${expire-time}")
	private static final int EXPIRE_TIME = 20;
	
	private static Calendar c = Calendar.getInstance();
	
	public static String generatePin() {
		return RandomStringUtils.randomNumeric(6);
	}
	
	public static Date getCurrentDate() {
		Date date = new Date();
		c.setTime(date);
		
		return date;
	}
	
	public static Date getExpireDate() {
		c.add(Calendar.MINUTE, EXPIRE_TIME);
		
		return c.getTime();
	}
	
}
