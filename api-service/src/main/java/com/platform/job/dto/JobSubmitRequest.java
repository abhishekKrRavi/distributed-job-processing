package com.platform.job.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for submitting a new Job.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JobSubmitRequest {

    @NotBlank(message = "jobType must be provided")
    private String jobType;

    @NotNull(message = "payload must be provided")
    private JsonNode payload;

    @Size(max = 128, message = "clientReqId must be at most 128 characters")
    private String clientReqId;

    @Size(max = 64, message = "tenantId must be at most 64 characters")
    private String tenantId;
}

