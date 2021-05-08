package com.gatheca.reactivedemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("coursework")
public class CourseWork {
    @Id
    @Column("id")
    private Long id;

    @Column("student_id")
    private Long studentID;

    @Column("course_id")
    private Long courseID;

    @Column("marks")
    private Integer marks;
}
