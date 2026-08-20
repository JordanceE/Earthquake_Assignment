package com.example.earthquake_assignment.exceptions;

public class UsgsApiException extends RuntimeException {
    public UsgsApiException(String message) {
        super(message);
    }
    public UsgsApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
