package com.platform.job.service.impl;

import com.platform.job.dto.JobSubmitRequest;
import com.platform.job.dto.JobStatusResponse;
import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import com.platform.job.repository.JobRepository;
import com.platform.job.service.JobService;
import com.platform.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic:jobs}")
    private String defaultTopic;

    @Override
    @Transactional
    public JobStatusResponse submitJob(JobSubmitRequest request, String idempotencyKey) {
        // Idempotency: if clientReqId provided or header idempotencyKey provided, return existing job
        String clientReqId = request.getClientReqId();
        if ((clientReqId == null || clientReqId.isBlank()) && idempotencyKey != null && !idempotencyKey.isBlank()) {
            clientReqId = idempotencyKey;
        }

        if (clientReqId != null && !clientReqId.isBlank()) {
            Optional<Job> existing = jobRepository.findByClientReqId(clientReqId);
            if (existing.isPresent()) {
                return mapToResponse(existing.get());
            }
        }

        Job j = Job.builder()
                .jobType(request.getJobType())
                .payload(request.getPayload())
                .status(JobStatus.PENDING)
                .retryCount(0)
                .clientReqId(clientReqId)
                .tenantId(request.getTenantId())
                .build();

        Job saved = jobRepository.save(j);

        // Publish event with only the data workers need (do not publish the entity)
        JobCreatedEvent event = JobCreatedEvent.builder()
                .jobId(saved.getId())
                .jobType(saved.getJobType())
                .payload(saved.getPayload())
                .tenantId(saved.getTenantId())
                .clientReqId(saved.getClientReqId())
                .build();

        kafkaTemplate.send(defaultTopic, saved.getId().toString(), event);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobStatusResponse getJob(UUID id) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
        return mapToResponse(j);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobStatusResponse> listJobs(Pageable pageable, Optional<JobStatus> status, Optional<String> jobType) {
        Page<Job> page;
        if (status.isPresent() && jobType.isPresent()) {
            // no combined repository method exists; fetch by status then filter by jobType in-memory
            Page<Job> byStatus = jobRepository.findByStatus(status.get(), pageable);
            List<Job> filtered = byStatus.getContent().stream()
                    .filter(j -> jobType.get().equals(j.getJobType()))
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered.stream().map(this::mapToResponse).collect(Collectors.toList()), pageable, filtered.size());
        } else if (status.isPresent()) {
            page = jobRepository.findByStatus(status.get(), pageable);
        } else if (jobType.isPresent()) {
            page = jobRepository.findByJobType(jobType.get(), pageable);
        } else {
            page = jobRepository.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteJob(UUID id) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new JobNotFoundException(id));
        // Only allow deletion for terminal states: SUCCEEDED, FAILED, CANCELLED
        JobStatus status = j.getStatus();
        if (!(status == JobStatus.SUCCEEDED || status == JobStatus.FAILED || status == JobStatus.CANCELLED)) {
            throw new InvalidJobStateException("Only SUCCEEDED, FAILED or CANCELLED jobs can be deleted.");
        }
        jobRepository.deleteById(id);
    }

    private JobStatusResponse mapToResponse(Job j) {
        return JobStatusResponse.builder()
                .id(j.getId())
                .jobType(j.getJobType())
                .payload(j.getPayload())
                .status(j.getStatus())
                .retryCount(j.getRetryCount())
                .errorMessage(j.getErrorMessage())
                .clientReqId(j.getClientReqId())
                .tenantId(j.getTenantId())
                .createdAt(j.getCreatedAt())
                .updatedAt(j.getUpdatedAt())
                .build();
    }
}

