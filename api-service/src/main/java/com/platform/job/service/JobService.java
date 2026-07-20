package com.platform.job.service;

import com.platform.job.dto.JobSubmitRequest;
import com.platform.job.dto.JobStatusResponse;
import com.platform.job.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface JobService {
    JobStatusResponse submitJob(JobSubmitRequest request, String idempotencyKey);
    JobStatusResponse getJob(UUID id);
    Page<JobStatusResponse> listJobs(Pageable pageable, Optional<JobStatus> status, Optional<String> jobType);
    void deleteJob(UUID id);
}

