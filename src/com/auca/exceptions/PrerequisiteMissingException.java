package com.auca.exceptions;


public class PrerequisiteMissingException extends RuntimeException {
    public PrerequisiteMissingException(String message) {
        super(message);
    }
}