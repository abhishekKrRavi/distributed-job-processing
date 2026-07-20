package com.platform.exception;

import java.util.UUID;

public class JobNotFoundWorkerException extends WorkerException {

    public JobNotFoundWorkerException(UUID jobId) {
        super("WORKER_JOB_NOT_FOUND", "Job not found in worker database: " + jobId);
    }
}
