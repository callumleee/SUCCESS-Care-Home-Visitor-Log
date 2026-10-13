package com.callumlee.visitorlog.common;

// Thrown when a request conflicts with the current state, e.g. signing out twice (returned as 409)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
