package com.example.earthquake_assignment.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsgsFeed(List<UsgsFeature> features) {
}
