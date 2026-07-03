package com.platform.job.dto;

import com.platform.job.model.JobStatus;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO returned when querying job status/details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JobStatusResponse {

    private UUID id;

    private String jobType;

    private Map<String, Object> payload;

    private JobStatus status;

    private int retryCount;

    private String errorMessage;

    private String clientReqId;

    private String tenantId;

    private Instant createdAt;

    private Instant updatedAt;
}