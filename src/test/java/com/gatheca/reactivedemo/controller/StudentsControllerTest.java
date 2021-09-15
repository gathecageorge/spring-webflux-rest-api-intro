package com.gatheca.reactivedemo.controller;

import com.gatheca.reactivedemo.dto.GeneralResponse;
import com.gatheca.reactivedemo.model.Students;
import com.gatheca.reactivedemo.repository.CourseWorkRepository;
import com.gatheca.reactivedemo.repository.StudentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class StudentsControllerTest {
    StudentsRepository studentsRepository;
    CourseWorkRepository courseWorkRepository;

    StudentsController studentsController;

    @BeforeEach
    void setUp() {
        studentsRepository = Mockito.mock(StudentsRepository.class);
        courseWorkRepository = Mockito.mock(CourseWorkRepository.class);

        studentsController = new StudentsController(studentsRepository, courseWorkRepository);
    }

    @Test
    void getStudent() {
        //mock when 1 is passed return a student
        Mockito.when(studentsRepository.findById(1L)).thenReturn(Mono.just(
                Students.builder().id(1L).name("george").registeredOn(System.currentTimeMillis()).status(1).build()
        ));

        //mock when 2 is passed return empty
        Mockito.when(studentsRepository.findById(2L)).thenReturn(Mono.empty());

        // test for passing a correct value
        Mono<ResponseEntity<Students>> response = studentsController.getStudent(1L);
        StepVerifier.create(response).consumeNextWith(studentsResponseEntity -> {
            assertEquals("george", studentsResponseEntity.getBody().getName());
            assertEquals(1, studentsResponseEntity.getBody().getStatus());
        });

        // test passing a value not found
        response = studentsController.getStudent(2L);
        StepVerifier.create(response).expectNextCount(0).verifyComplete();
    }

    @Test
    void getStudents() {
        //mock database call
        Mockito.when(studentsRepository.findAllByStatusAndName(0L, 10L, null, null)).thenReturn(
                Flux.just(
                        Students.builder().id(1L).name("george").registeredOn(System.currentTimeMillis()).status(1).build(),
                        Students.builder().id(2L).name("george2").registeredOn(System.currentTimeMillis()).status(0).build()
                )
        );

        //test when there are results found in the database
        //NB: we are passing empty filters here as mocked above for database call
        Flux<Students> response = studentsController.getStudents(1, 10L, new HashMap<>());
        StepVerifier.create(response).consumeNextWith(student -> {
            assertEquals("george", student.getName());
            assertEquals(1, student.getStatus());
        }).consumeNextWith(student -> {
            assertEquals("george2", student.getName());
            assertEquals(0, student.getStatus());
        });

        //mock database call to return empty;
        //we are using parameters to make sure that this will be called instead of the above mock when these parameters
        //are passed. Also, we know that since name is not empty or null, %% will be added, so we add it to mock
        Mockito.when(studentsRepository.findAllByStatusAndName(0L, 10L, "statusValue", "%nameValue%")).thenReturn(Flux.empty());

        //We are passing parameters to make sure mock with parameters is called
        response = studentsController.getStudents(1, 10L, new HashMap<>() {{
            put("status", "statusValue");
            put("name", "nameValue");
        }});
        StepVerifier.create(response).expectNextCount(0).verifyComplete();
    }

    @Test
    void addStudent() {
        //a new student to add without an id
        Students student = Students.builder().name("george").registeredOn(System.currentTimeMillis()).status(1).build();

        //mock adding student, here we use thenAnswer to make modification on passed object and simulate
        //setting the id after db save. We can then verify that ID exists and is correct after saving and not null
        Mockito.when(studentsRepository.save(student)).thenAnswer(toSave -> {
            Students toSaveStudent = toSave.getArgument(0);
            toSaveStudent.setId(1L);
            return Mono.just(toSaveStudent);
        });

        //call add student
        Mono<ResponseEntity<Students>> response = studentsController.addStudent(student);
        StepVerifier.create(response).consumeNextWith(studentsResponseEntity -> {
            assertEquals("george", studentsResponseEntity.getBody().getName());
            assertEquals(1, studentsResponseEntity.getBody().getStatus());
            assertEquals(1L, studentsResponseEntity.getBody().getId());
        });
    }

    @Test
    void updateStudent() {
        //an old and new student sample
        Students oldStudent = Students.builder().id(1L).name("gatheca").registeredOn(System.currentTimeMillis()).status(0).build();
        Students newStudent = Students.builder().name("george").registeredOn(System.currentTimeMillis()).status(1).build();

        //mock not finding a student. We know that since no record is found an error will be thrown and code
        //execution won't reach saving the new student. So no need to mock save at this point
        Mockito.when(studentsRepository.findById(1L)).thenReturn(Mono.empty());

        Mono<ResponseEntity<GeneralResponse<Students>>> response = studentsController.updateStudent(1L, newStudent);

        // we don't expect any data instead expect a null data and success false as from controller line 83 - 89
        StepVerifier.create(response).consumeNextWith(data -> {
            assertNull(data.getBody().getData());
            assertFalse(data.getBody().isSuccess());
        });

        //mock finding a student, we return an old student with name gatheca. We expect it to be changed by update
        Mockito.when(studentsRepository.findById(2L)).thenReturn(Mono.just(oldStudent));

        //mock saving student after updating by returning itself. We know since a student is found we need to mock
        //saving so as not to get an error
        Mockito.when(studentsRepository.save(Mockito.any())).thenAnswer(toSave -> {
            Students toSaveStudent = toSave.getArgument(0);
            return Mono.just(toSaveStudent);
        });

        response = studentsController.updateStudent(2L, newStudent);

        //we expect save to be successful and thus success true and new name should not be gatheca but george.
        StepVerifier.create(response).consumeNextWith(data -> {
            assertNotNull(data.getBody().getData());
            assertTrue(data.getBody().isSuccess());

            assertEquals("george", data.getBody().getData().get("student").getName());
        });
    }

    @Test
    void deleteStudent() {
        //todo for student to implement
    }
}