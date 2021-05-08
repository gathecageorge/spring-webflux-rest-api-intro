package com.gatheca.reactivedemo.repository;

import com.gatheca.reactivedemo.model.Students;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StudentsRepository extends ReactiveCrudRepository<Students, Long> {
    @Query(value = "SELECT * FROM students " +
            "WHERE " +
            "(`status`=:status OR :status is null) AND " +
            "(`name` LIKE :name OR :name is null) " +
            "LIMIT :limit OFFSET :offset")
    Flux<Students> findAllByStatusAndName(Long offset, Long limit, String status, String name);
}
