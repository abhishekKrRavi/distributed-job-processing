package com.platform.exception;

public class UnsupportedJobTypeWorkerException extends WorkerException {

    public UnsupportedJobTypeWorkerException(String jobType) {
        super("WORKER_UNSUPPORTED_JOB_TYPE", "No processor registered for job type: " + jobType);
    }
}
