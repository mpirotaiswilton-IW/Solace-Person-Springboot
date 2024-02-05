package com.max_pw_iw.person_subscriber.services;

import org.springframework.stereotype.Service;

import com.max_pw_iw.person_subscriber.entity.Person;
import com.max_pw_iw.person_subscriber.repository.PersonRepository;

import lombok.AllArgsConstructor;

@Service(value = "personService")
@AllArgsConstructor
public class PersonServiceImpl implements PersonService{
    
    private PersonRepository personRepository;

    @Override
    public void AddPerson(Person person) {
        personRepository.save(person);
    }

}
