package com.max_pw_iw.person_subscriber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.jms.Session;

@SpringBootApplication
public class PersonSubscriberApplication {

	Session session;

	public static void main(String[] args) {
		SpringApplication.run(PersonSubscriberApplication.class, args);
	}
}
