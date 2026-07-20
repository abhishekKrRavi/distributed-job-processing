package com.platform.job.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity mapping for the "job" table.
 * Contains named JPQL and native queries for reuse across the application.
 */
@NamedQueries({
        @NamedQuery(name = "Job.findByStatus", query = "SELECT j FROM Job j WHERE j.status = :status"),
        @NamedQuery(name = "Job.findByJobType", query = "SELECT j FROM Job j WHERE j.jobType = :jobType"),
        @NamedQuery(name = "Job.findByTenantId", query = "SELECT j FROM Job j WHERE j.tenantId = :tenantId"),
        @NamedQuery(name = "Job.findByClientReqId", query = "SELECT j FROM Job j WHERE j.clientReqId = :clientReqId"),
        @NamedQuery(name = "Job.countByStatus", query = "SELECT COUNT(j) FROM Job j WHERE j.status = :status"),
        @NamedQuery(name = "Job.incrementRetryCount", query = "UPDATE Job j SET j.retryCount = j.retryCount + 1, j.updatedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "Job.searchByPayloadContains",
                query = "SELECT * FROM job WHERE payload::text ILIKE '%' || :keyword || '%'",
                resultClass = Job.class)
})
@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "job_type", length = 100, nullable = false)
    private String jobType;

    // JSONB payload stored as Postgres jsonb column. Requires Hibernate 6 support.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private JobStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "client_req_id", length = 128)
    private String clientReqId;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
