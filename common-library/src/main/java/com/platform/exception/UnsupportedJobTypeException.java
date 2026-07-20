package com.platform.exception;

public class UnsupportedJobTypeException extends ApplicationException {
    public UnsupportedJobTypeException(String message) {
        super("UNSUPPORTED_JOB_TYPE", message);
    }
}
