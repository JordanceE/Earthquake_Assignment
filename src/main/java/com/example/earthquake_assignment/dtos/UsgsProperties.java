package com.example.earthquake_assignment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsProperties(Double mag,
                             String magType,
                             String place,
                             Long time,
                             String title,
                             String type
                             ) {
}
