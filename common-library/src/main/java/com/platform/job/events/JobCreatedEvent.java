package com.platform.job.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

/**
 * Event published when a Job is created. Contains only the data workers need.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobCreatedEvent {
    private UUID jobId;
    private String jobType;
    private Map<String, Object> payload;
    private String tenantId;
    private String clientReqId;
}
