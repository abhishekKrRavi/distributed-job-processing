package com.platform.exception;

/**
 * Base application exception for the Job platform. Placed in common-library so all modules can reuse it.
 */
public class ApplicationException extends RuntimeException {
    private final String errorCode;

    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
