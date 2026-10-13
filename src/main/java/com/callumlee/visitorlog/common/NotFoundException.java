package com.callumlee.visitorlog.common;

// Thrown when a requested record does not exist (returned as 404)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
