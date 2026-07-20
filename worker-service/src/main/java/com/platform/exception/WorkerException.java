package com.platform.exception;

public class WorkerException extends ApplicationException {

    public WorkerException(String errorCode, String message) {
        super(errorCode, message);
    }

    public WorkerException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
