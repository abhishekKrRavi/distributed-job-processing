package com.platform.exception;

public class InvalidJobStateException extends ApplicationException {
    public InvalidJobStateException(String message) {
        super("INVALID_STATE", message);
    }
}
