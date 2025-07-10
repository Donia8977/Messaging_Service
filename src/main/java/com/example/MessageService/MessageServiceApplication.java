package com.example.MessageService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MessageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageServiceApplication.class, args);

//		String rawPassword = "Admin@123.";
//		String hashed = new BCryptPasswordEncoder().encode(rawPassword);
//		System.out.println("BCrypt hash for Admin password: " + hashed);
	}
	
}
