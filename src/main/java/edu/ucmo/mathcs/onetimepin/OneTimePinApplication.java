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
		SpringApplication.run(OneTimePinApplication.class, args);
	}
	
}
