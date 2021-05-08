package com.gatheca.reactivedemo.repository;

import com.gatheca.reactivedemo.model.CourseWork;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CourseWorkRepository extends ReactiveCrudRepository<CourseWork, Long> {
    Mono<Void> deleteByStudentID(Long studentID);
}
