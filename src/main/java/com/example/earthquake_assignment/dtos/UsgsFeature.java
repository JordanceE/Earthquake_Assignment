package com.example.earthquake_assignment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsFeature(String id, UsgsProperties properties, UsgsGeometry geometry) {

}
