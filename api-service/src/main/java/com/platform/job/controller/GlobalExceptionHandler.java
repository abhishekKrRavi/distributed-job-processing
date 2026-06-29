package com.platform.job.controller;

import com.platform.job.dto.ApiErrorResponse;
import com.platform.exception.ApplicationException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleApplication(ApplicationException ex, HttpServletRequest req) {
        String code = ex.getErrorCode();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        switch (code) {
            case "JOB_NOT_FOUND": status = HttpStatus.NOT_FOUND; break;
            case "VALIDATION_FAILED": status = HttpStatus.BAD_REQUEST; break;
            case "INVALID_STATE": status = HttpStatus.CONFLICT; break;
            default: status = HttpStatus.INTERNAL_SERVER_ERROR; break;
        }

        ApiErrorResponse err = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(code)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiErrorResponse err = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message(message)
                .path(req.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        // Wrap unknown exceptions into ApplicationException for consistent format
        ApplicationException appEx = new ApplicationException("INTERNAL_ERROR", ex.getMessage(), ex);
        ApiErrorResponse err = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(appEx.getErrorCode())
                .message(appEx.getMessage())
                .path(req.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build();
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}

