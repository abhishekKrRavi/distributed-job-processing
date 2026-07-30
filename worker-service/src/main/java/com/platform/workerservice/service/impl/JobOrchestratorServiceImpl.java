package com.platform.workerservice.service.impl;

import com.platform.exception.JobNotFoundWorkerException;
import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import com.platform.job.repository.JobRepository;
import com.platform.workerservice.logging.WorkerLog;
import com.platform.workerservice.processor.JobProcessor;
import com.platform.workerservice.processor.JobProcessorRegistry;
import com.platform.workerservice.service.JobOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JobOrchestratorServiceImpl implements JobOrchestratorService {

    private final JobRepository jobRepository;
    private final JobProcessorRegistry processorRegistry;
    private final WorkerLog workerLog;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${worker.job.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${worker.job.retry.initial-interval-ms:2000}")
    private long initialIntervalMs;

    @Value("${worker.job.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${worker.job.retry.max-interval-ms:10000}")
    private long maxIntervalMs;

    @Value("${worker.job.retry.dlq-topic:job.dlq}")
    private String dlqTopic;

    @Value("${worker.job.retry.non-retryable-exceptions}")
    private List<String> nonRetryableExceptions;

    @Override
    public void handle(JobCreatedEvent event) {
        workerLog.stage(event.getJobId(), "RECEIVED", "handling jobType=" + event.getJobType());

        // 1. Update job status to PROCESSING and commit before execution
        Job initialJob = transactionTemplate.execute(status -> {
            Job job = jobRepository.findById(event.getJobId())
                    .orElseThrow(() -> new JobNotFoundWorkerException(event.getJobId()));
            job.setStatus(JobStatus.PROCESSING);
            Job saved = jobRepository.save(job);
            workerLog.stage(saved.getId(), "STATUS_UPDATE", "status=" + saved.getStatus());
            return saved;
        });

        int attempt = 0;
        long currentIntervalMs = initialIntervalMs;

        while (true) {
            attempt++;
            try {
                final Job jobToProcess = initialJob;
                transactionTemplate.executeWithoutResult(status -> {
                    process(jobToProcess, event);
                });
                break; // Break retry loop on successful execution
            } catch (Exception ex) {
                boolean retryable = isRetryable(ex);
                workerLog.failed(event.getJobId(), "PROCESSING_FAILED", ex);

                if (!retryable || attempt >= maxAttempts) {
                    JobStatus terminalStatus = retryable ? JobStatus.DLQ : JobStatus.FAILED;
                    
                    transactionTemplate.executeWithoutResult(status -> {
                        Job j = jobRepository.findById(event.getJobId()).orElse(initialJob);
                        j.setStatus(terminalStatus);
                        j.setErrorMessage(ex.getMessage());
                        jobRepository.save(j);
                    });

                    workerLog.stage(event.getJobId(), "TERMINAL_STATE", "status=" + terminalStatus + " attempts=" + attempt);

                    if (terminalStatus == JobStatus.DLQ) {
                        try {
                            kafkaTemplate.send(dlqTopic, event.getJobId().toString(), event);
                            workerLog.stage(event.getJobId(), "DLQ_PUBLISH", "published to topic=" + dlqTopic);
                        } catch (Exception kafkaEx) {
                            workerLog.failed(event.getJobId(), "DLQ_PUBLISH_FAILED", kafkaEx);
                        }
                    }
                    break;
                } else {
                    final int nextAttemptCount = attempt;
                    transactionTemplate.executeWithoutResult(status -> {
                        Job j = jobRepository.findById(event.getJobId()).orElse(initialJob);
                        j.setStatus(JobStatus.RETRYING);
                        j.setRetryCount(nextAttemptCount);
                        j.setErrorMessage(ex.getMessage());
                        jobRepository.save(j);
                    });

                    workerLog.stage(event.getJobId(), "RETRY_BACKOFF", "sleeping " + currentIntervalMs + "ms before attempt " + (attempt + 1));
                    try {
                        TimeUnit.MILLISECONDS.sleep(currentIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry backoff interrupted", ie);
                    }

                    currentIntervalMs = (long) Math.min(currentIntervalMs * multiplier, maxIntervalMs);
                }
            }
        }
    }

    @Override
    @Transactional
    public void process(Job job, JobCreatedEvent event) {
        JobProcessor processor = processorRegistry.resolve(job.getJobType());
        workerLog.stage(job.getId(), "DELEGATE", "processor=" + processor.getClass().getSimpleName());
        waitAndLog("processing", job.getId(), 10);
        processor.process(job, event);
        job.setStatus(JobStatus.COMPLETED);
        jobRepository.save(job);
        workerLog.completed(job.getId());
    }

    private void waitAndLog(String stage, java.util.UUID jobId, long seconds) {
        workerLog.stage(jobId, stage.toUpperCase(), "waiting " + seconds + "s");
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            workerLog.failed(jobId, stage.toUpperCase(), ex);
            throw new IllegalStateException("Interrupted while simulating job processing for jobId=" + jobId, ex);
        }
    }

    private boolean isRetryable(Exception ex) {
        if (nonRetryableExceptions == null) {
            return true;
        }
        Throwable cause = ex;
        while (cause != null) {
            String causeClassName = cause.getClass().getName();
            for (String nonRetryable : nonRetryableExceptions) {
                if (causeClassName.equals(nonRetryable.trim())) {
                    return false;
                }
            }
            cause = cause.getCause();
        }
        return true;
    }
}

