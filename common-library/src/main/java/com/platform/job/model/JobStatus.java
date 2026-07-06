package com.platform.job.model;

public enum JobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    QUEUED,
    RETRYING,
    DLQ
}
