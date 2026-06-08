package com.amalitech.todoApi.exceptions;

public class UserExists extends RuntimeException {
    public UserExists(String message) {
        super(message);
    }
}
