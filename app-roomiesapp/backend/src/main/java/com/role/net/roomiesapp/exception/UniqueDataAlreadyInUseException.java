package com.role.net.roomiesapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UniqueDataAlreadyInUseException extends RuntimeException {
    public UniqueDataAlreadyInUseException(String message) {
        super(message);
    }
}
