package com.max_pw_iw.person_publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.max_pw_iw.person_publisher.entity.EventPerson;

import jakarta.annotation.PostConstruct;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.Session;


@SpringBootApplication
@EnableScheduling
public class PersonPublisherApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(PersonPublisherApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		List<EventPerson> people = new ArrayList<EventPerson>();

		// ClassLoader classLoader = getClass().getClassLoader();

		try (Scanner scanner = new Scanner(new File("/opt/app/data/Mock_data.csv"))) {
			while (scanner.hasNextLine()) {
				getPersonFromLine(scanner.nextLine());
			}
		}

		for (EventPerson person : people) {
			boolean eventSent = false;
			while(!eventSent){
				try{
					sendEvent(person);
					eventSent = true;
				} catch (UncategorizedJmsException e){

				}
			}
		}
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

	private EventPerson getPersonFromLine(String line){
		EventPerson value = new EventPerson();
		String[] values = line.split(",");
		// value.setId(Long.parseLong(values[0]));
		value.setFirstName(values[1]);
		value.setLastName(values[2]);
		value.setSex(values[4]);
		value.setAge(Integer.parseInt(values[3]));
		
		return value;
	}

	@Value("people/add")
	private String topicName;

	// @Scheduled(fixedRate = 5000)
	// public void sendEventToQueue() throws Exception {
	// 	EventPerson eventPerson = new EventPerson("Bob","Steve",20,"Male");
	// 	System.out.println("==========SENDING PERSON TO TOPIC: \"" + topicName + "\"========== " + eventPerson.getFirstName());
	// 	sendEvent(eventPerson);
	// }

	public void sendEvent(EventPerson eventPerson){

        // Map<String, Object> map = new HashMap<>(); 
        
        jmsTopicTemplate.send(topicName, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				System.out.println("==========SENDING PERSON TO TOPIC: \"" + topicName + "\"========== " + eventPerson.getFirstName());
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("firstName", eventPerson.getFirstName()); 
				mapMessage.setString("lastName", eventPerson.getLastName()); 
				mapMessage.setString("sex", eventPerson.getSex()); 
				mapMessage.setInt("age", eventPerson.getAge()); 
				return mapMessage;
			}
		});
	}

}
