package com.platform.job.dto;

import com.platform.job.model.JobStatus;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JobCreateResponse {
    private UUID jobId;
    private JobStatus status;
    private String message;
    private Map<String, String> links;
}

