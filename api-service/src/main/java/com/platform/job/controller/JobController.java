package com.platform.job.controller;

import com.platform.job.dto.JobCreateResponse;
import com.platform.job.dto.JobStatusResponse;
import com.platform.job.dto.PagedResponse;
import com.platform.job.dto.JobSubmitRequest;
import com.platform.job.model.JobStatus;
import com.platform.job.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for Job APIs.
 * Delegates business logic to JobService.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Validated
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobCreateResponse> submit(@Valid @RequestBody JobSubmitRequest request,
                                                    @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                    HttpServletRequest servletRequest) {
        JobStatusResponse created = jobService.submitJob(request, idempotencyKey);
        URI location = URI.create(servletRequest.getRequestURI() + "/" + created.getId());

        JobCreateResponse resp = JobCreateResponse.builder()
                .jobId(created.getId())
                .status(created.getStatus())
                .message("Job accepted for asynchronous processing.")
                .links(Map.of("self", "/api/v1/jobs/" + created.getId()))
                .build();

        return ResponseEntity.accepted().location(location).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobStatusResponse> getJob(@PathVariable("id") UUID id) {
        JobStatusResponse resp = jobService.getJob(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<JobStatusResponse>> listJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String jobType) {

        Pageable pageable = PageRequest.of(page, size);
        Page<JobStatusResponse> results = jobService.listJobs(pageable, Optional.ofNullable(status), Optional.ofNullable(jobType));
        PagedResponse<JobStatusResponse> resp = PagedResponse.fromPage(results);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable("id") UUID id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}

