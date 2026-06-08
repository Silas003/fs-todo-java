package com.amalitech.todoApi.exceptions;

public class InvalidTodoException extends RuntimeException {
    public InvalidTodoException(String message) {
        super(message);
    }
}
