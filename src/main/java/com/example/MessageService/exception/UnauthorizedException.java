package com.example.MessageService.exception;

public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message)
    {
        super(message);
    }

}
