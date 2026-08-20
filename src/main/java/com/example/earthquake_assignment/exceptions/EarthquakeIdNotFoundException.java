package com.example.earthquake_assignment.exceptions;

public class EarthquakeIdNotFoundException extends RuntimeException {
    public EarthquakeIdNotFoundException(Long id) {
        super("Earthquake with id " + id + " not found");
    }
}
