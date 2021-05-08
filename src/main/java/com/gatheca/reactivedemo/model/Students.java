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
@Table("students")
public class Students {
    @Id
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("registered_on")
    private Long registeredOn;

    @Column("status")
    private Integer status;
}
