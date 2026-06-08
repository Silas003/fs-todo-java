package com.amalitech.todoApi.exceptions;

public class TodoCreationException extends RuntimeException {
    public TodoCreationException(String message) {
        super(message);
    }
}
