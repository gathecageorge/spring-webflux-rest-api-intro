package com.gatheca.reactivedemo.repository;

import com.gatheca.reactivedemo.model.Students;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentsRepository extends ReactiveCrudRepository<Students, Long> {
}
