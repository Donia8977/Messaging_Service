package com.example.MessageService.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message)
    {
        super(message);
    }
}
