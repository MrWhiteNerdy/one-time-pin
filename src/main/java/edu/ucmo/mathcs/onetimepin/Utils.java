package edu.ucmo.mathcs.onetimepin;

import org.apache.commons.lang.RandomStringUtils;

public class Utils {
	
	public static String generatePin() {
		return RandomStringUtils.randomNumeric(6);
	}
	
}
