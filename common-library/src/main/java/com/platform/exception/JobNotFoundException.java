package com.platform.exception;

import java.util.UUID;

public class JobNotFoundException extends ApplicationException {
    public JobNotFoundException(String id) {
        super("JOB_NOT_FOUND", "No job with id: " + id);
    }

    public JobNotFoundException(UUID id) { this(id.toString()); }
}
