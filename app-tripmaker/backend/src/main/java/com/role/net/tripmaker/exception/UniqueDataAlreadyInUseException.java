package com.role.net.tripmaker.exception;

public class UniqueDataAlreadyInUseException extends RuntimeException {
    public UniqueDataAlreadyInUseException(String message) {
        super(message);
    }
}
