package com.platform.job.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.job.model.JobStatus;
import lombok.*;

import java.time.Instant;
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

    private JsonNode payload;

    private JobStatus status;

    private int retryCount;

    private String errorMessage;

    private String clientReqId;

    private String tenantId;

    private Instant createdAt;

    private Instant updatedAt;
}

