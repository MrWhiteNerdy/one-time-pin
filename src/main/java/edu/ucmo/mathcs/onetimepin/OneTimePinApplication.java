package edu.ucmo.mathcs.onetimepin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Random;

@SpringBootApplication
public class OneTimePinApplication {

	public static void main(String[] args) {
		System.out.println("IP Address: " + getIPAddress());
		System.out.println("PIN: " + getPIN());
		System.out.println("Timestamp: " + getTimestamp());
	}

	public static String getIPAddress() {
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return ip.getHostAddress();
	}

	public static String getPIN() {
		int digit = 0;
		Random rand = new Random();
		String pin = "";

		for (int i = 0; i < 6; i++) {
			digit = rand.nextInt(10);
			pin += digit;
		}

		return pin;
	}

	public static Timestamp getTimestamp() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		return timestamp;
	}
}
