package com.max_pw_iw.person_publisher;

import java.io.File;
import java.io.FileNotFoundException;
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
public class PersonPublisherApplication{

	public static void main(String[] args) {
		SpringApplication.run(PersonPublisherApplication.class, args);
	}

	@Autowired
	private JmsTemplate jmsTopicTemplate;

	@PostConstruct
	private void customizeJmsTemplate() {

		System.out.println("Running @PostConsctruct annotated method...");

		// Update the jmsTopicTemplate's connection factory to cache the connection
		CachingConnectionFactory tccf = new CachingConnectionFactory();
		tccf.setTargetConnectionFactory(jmsTopicTemplate.getConnectionFactory());
		jmsTopicTemplate.setConnectionFactory(tccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination

		jmsTopicTemplate.setPubSubDomain(true);
	}

	@PostConstruct
	private void sendPeopleFromCsv() {
		
		List<EventPerson> people = new ArrayList<EventPerson>();

		//ClassLoader classLoader = getClass().getClassLoader();

		File mockData = new File("/opt/app/data/Mock_data.csv");

		try (Scanner scanner = new Scanner(mockData)) {
			while (scanner.hasNextLine()) {
				people.add(getPersonFromLine(scanner.nextLine()));
			}
		} catch (FileNotFoundException e){
			System.out.println("Source file not found");
		}

		int eventRetriesMax = 20;
		int eventRetries = 0;

		for (EventPerson person : people) {
			boolean eventSent = false;
			while(!eventSent){
				try{
					sendEvent(person);
					eventSent = true;
					//System.out.println("Person sent as event: " + person.getFirstName());
				} catch (UncategorizedJmsException e){
					eventRetries++;
					if(eventRetries >= eventRetriesMax){
						System.out.println("Process has been stopped due to a series of UncategorizedJmsException (max retries reached on an event: " + Integer.toString(eventRetries)+ "). Make sure connection parameters are correctly set and that a Solace event broker is running");
						return;
					}
				}
			}
		}
	}


	private EventPerson getPersonFromLine(String line){
		EventPerson value = new EventPerson();
		String[] values = line.split(",");
		value.setFirstName(values[1]);
		value.setLastName(values[2]);
		value.setSex(values[4]);
		value.setAge(Integer.parseInt(values[3]));

		return value;
	}

	@Value("people/add")
	private String topicName;

	public void sendEvent(EventPerson eventPerson){

        // Map<String, Object> map = new HashMap<>(); 
        
        jmsTopicTemplate.send(topicName, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				// System.out.println("==========SENDING PERSON " + eventPerson.getFirstName() + " TO TOPIC: \"" + topicName + " \"========== ");
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
