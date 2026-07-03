package com.role.net.roomiesapp.exception;

public class UniqueDataAlreadyInUseException extends RuntimeException {
    public UniqueDataAlreadyInUseException(String message) {
        super(message);
    }
}
