package com.max_pw_iw.person_publisher;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.max_pw_iw.person_publisher.entity.EventPerson;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class PersonPublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonPublisherApplication.class, args);
	}

	@Autowired
	private JmsTemplate jmsTopicTemplate;

	@PostConstruct
	private void customizeJmsTemplate() {

		// Update the jmsTopicTemplate's connection factory to cache the connection
		CachingConnectionFactory tccf = new CachingConnectionFactory();
		tccf.setTargetConnectionFactory(jmsTopicTemplate.getConnectionFactory());
		jmsTopicTemplate.setConnectionFactory(tccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination

		jmsTopicTemplate.setPubSubDomain(true);
	}

	@Value("person/add")
	private String topicName;

	@Scheduled(fixedRate = 5000)
	public void sendEventToQueue() throws Exception {
		EventPerson eventPerson = new EventPerson("Bob","Steve",20,"Male");
		System.out.println("==========SENDING PERSON TO TOPIC========== " + eventPerson.getFirstName());
		sendEvent(eventPerson);
	}

	public void sendEvent(EventPerson eventPerson){

		System.out.println("Jms Message Sender : " + eventPerson); 
        Map<String, Object> map = new HashMap<>(); 
        map.put("firstName", eventPerson.getFirstName()); 
		map.put("lastName", eventPerson.getLastName()); 
        map.put("sex", eventPerson.getSex()); 
		map.put("age", eventPerson.getAge()); 
        jmsTopicTemplate.convertAndSend(topicName,map); 

	}

}
