package com.example.MessageService.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TenantNotFoundException extends RuntimeException{
    public TenantNotFoundException(String message) {
        super(message);
    }
}
