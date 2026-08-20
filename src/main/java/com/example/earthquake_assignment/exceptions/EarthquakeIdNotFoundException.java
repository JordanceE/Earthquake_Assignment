package com.example.earthquake_assignment.exceptions;

public class EarthquakeIdNotFoundException extends RuntimeException {
  public EarthquakeIdNotFoundException(String message) {
    super(message);
  }
}
