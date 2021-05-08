package com.gatheca.reactivedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GeneralResponse<T> {
    boolean success;
    String message;
    Map<String, T> data;
}
