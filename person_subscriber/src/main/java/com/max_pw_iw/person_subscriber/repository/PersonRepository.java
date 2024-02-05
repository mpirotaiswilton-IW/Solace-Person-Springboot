package com.max_pw_iw.person_subscriber.repository;

import org.springframework.data.repository.CrudRepository;

import com.max_pw_iw.person_subscriber.entity.Person;

public interface PersonRepository extends CrudRepository<Person, Long>{
}
