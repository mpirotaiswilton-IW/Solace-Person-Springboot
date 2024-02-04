package com.max_pw_iw.person_subscriber;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

@SpringBootApplication
public class PersonSubscriberApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonSubscriberApplication.class, args);
	}

	@JmsListener(destination = "PeopleQ")
	public void handle(Message message) {

		Date receiveTime = new Date();

		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				System.out.println(
						"Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
								+ " with message content of: " + tm.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(message.toString());
		}
	}
	
}
