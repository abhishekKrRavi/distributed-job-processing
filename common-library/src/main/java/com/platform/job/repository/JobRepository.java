package com.platform.job.repository;

import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import com.platform.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Job entities.
 * Extends BaseJpaRepository so it inherits common contract and convenience methods (CRUD, paging).
 * Additional convenience queries and modifiers for job-specific operations are provided here.
 */
@Repository
public interface JobRepository extends BaseJpaRepository<Job, UUID> {

    /* --- Pagination (uses named queries) --- */
    @Query(name = "Job.findByStatus")
    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    @Query(name = "Job.findByJobType")
    Page<Job> findByJobType(String jobType, Pageable pageable);

    @Query(name = "Job.findByTenantId")
    Page<Job> findByTenantId(String tenantId, Pageable pageable);

    /* --- Simple queries (named) --- */
    @Query(name = "Job.findByClientReqId")
    Optional<Job> findByClientReqId(String clientReqId);

    @Query(name = "Job.countByStatus")
    long countByStatus(JobStatus status);

    /* --- JSON payload search (named native query) --- */
    @Query(name = "Job.searchByPayloadContains", nativeQuery = true)
    Page<Job> searchByPayloadContains(String keyword, Pageable pageable);

    /* --- Modifying queries (named JPQL update) --- */
    @Modifying
    @Transactional
    @Query(name = "Job.incrementRetryCount")
    int incrementRetryCount(UUID id);
}
