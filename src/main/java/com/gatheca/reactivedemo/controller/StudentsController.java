package com.gatheca.reactivedemo.controller;

import com.gatheca.reactivedemo.dto.GeneralResponse;
import com.gatheca.reactivedemo.model.CourseWork;
import com.gatheca.reactivedemo.model.Students;
import com.gatheca.reactivedemo.repository.CourseWorkRepository;
import com.gatheca.reactivedemo.repository.StudentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@AllArgsConstructor
public class StudentsController {
    private final StudentsRepository studentsRepository;
    private final CourseWorkRepository courseWorkRepository;

    @GetMapping("/students/{studentID}")
    Mono<ResponseEntity<Students>> getStudent(@PathVariable Long studentID) {
        return studentsRepository.findById(studentID).map(student -> {
            return new ResponseEntity<>(student, HttpStatus.OK);
        });
    }

    @GetMapping(value = "/students", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    Flux<Students> getStudents(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Long limit,
            @RequestParam Map<String, String> filterParams
    ) {
        String status = filterParams.getOrDefault("status", null);
        String name = filterParams.getOrDefault("name", null);
        if (name != null) {
            name = "%" + name + "%";
        }

        long offset = (page - 1) * limit;

        return studentsRepository.findAllByStatusAndName(offset, limit, status, name).delayElements(Duration.ofSeconds(2L));
    }

    @PostMapping("/students")
    Mono<ResponseEntity<Students>> addStudent(@RequestBody Students studentAdd) {
        studentAdd.setRegisteredOn(System.currentTimeMillis());
        studentAdd.setStatus(1);
        return studentsRepository.save(studentAdd).map(student -> {
            return new ResponseEntity<>(student, HttpStatus.CREATED);
        });
    }

    @PutMapping("/students/{studentID}")
    Mono<ResponseEntity<GeneralResponse<Students>>> updateStudent(@PathVariable Long studentID, @RequestBody Students newStudentData) {

        return studentsRepository.findById(studentID)
                .switchIfEmpty(Mono.error(new Exception(String.format("Student with ID %d not found", studentID))))
                .flatMap(foundStudent -> {
                    //here we are just updating the name. You can add others
                    foundStudent.setName(newStudentData.getName());

                    return studentsRepository.save(foundStudent);
                }).map(student -> {
                    HashMap<String, Students> data = new HashMap<>();
                    data.put("student", student);

                    return new ResponseEntity<>(
                            GeneralResponse.<Students>builder()
                                    .success(true)
                                    .message("Student updated successfully")
                                    .data(data)
                                    .build(),
                            HttpStatus.ACCEPTED
                    );
                }).onErrorResume(e -> {
                    return Mono.just(
                            new ResponseEntity<>(
                                    GeneralResponse.<Students>builder()
                                            .success(false)
                                            .message(e.getMessage())
                                            .build(),
                                    HttpStatus.NOT_FOUND
                            )
                    );
                });

    }

    @DeleteMapping("/students/{studentID}")
    @Transactional
    Mono<ResponseEntity<GeneralResponse<Students>>> deleteStudent(@PathVariable Long studentID) {
        return studentsRepository.findById(studentID)
                .switchIfEmpty(Mono.error(new Exception(String.format("Student with ID %d not found", studentID))))
                .flatMap(foundStudent -> {
                    return courseWorkRepository.deleteByStudentID(studentID)
                            .then(studentsRepository.deleteById(studentID))
                            .thenReturn(foundStudent);
                })
                .map(deletedStudent -> {
                    HashMap<String, Students> data = new HashMap<>();
                    data.put("student", deletedStudent);

                    return new ResponseEntity<>(
                            GeneralResponse.<Students>builder()
                                    .success(true)
                                    .message("Student deleted successfully")
                                    .data(data)
                                    .build(),
                            HttpStatus.ACCEPTED
                    );
                })
                .onErrorResume(e -> {
                    return Mono.just(
                            new ResponseEntity<>(
                                    GeneralResponse.<Students>builder()
                                            .success(false)
                                            .message(e.getMessage())
                                            .build(),
                                    HttpStatus.NOT_FOUND
                            )
                    );
                });
    }

    private CourseWork getCoursework(Long studentID, Long courseID) {
        return CourseWork.builder()
                .courseID(courseID)
                .marks(20)
                .studentID(studentID)
                .build();
    }

    @GetMapping("/students/chain-course/{studentID}/{courseID}")
    Mono<CourseWork> addNewCourseChain(@PathVariable Long studentID, @PathVariable Long courseID) {
        return studentsRepository.findById(studentID)
                .flatMap(students -> {
                    //Update student details
                    students.setUpdated_on(System.currentTimeMillis());
                    return studentsRepository.save(students)
                            .flatMap(updatedStudent -> {
                                //Create a new course for student
                                CourseWork courseWork = getCoursework(updatedStudent.getId(), courseID);
                                return courseWorkRepository.save(courseWork);
                            });
                });
    }

    @GetMapping("/students/course/{studentID}/{courseID}")
    Mono<CourseWork> addNewCourseNoChain(Long studentID, @PathVariable Long courseID) {
        return studentsRepository.findById(studentID)
                .flatMap(students -> {
                    //Update student details
                    students.setUpdated_on(System.currentTimeMillis());
                    return studentsRepository.save(students);
                }).flatMap(updatedStudent -> {
                    //Create a new course for student
                    CourseWork courseWork = getCoursework(updatedStudent.getId(), courseID);
                    return courseWorkRepository.save(courseWork);
                });
    }
}
