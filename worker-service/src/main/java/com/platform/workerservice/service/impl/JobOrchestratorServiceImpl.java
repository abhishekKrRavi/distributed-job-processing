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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JobOrchestratorServiceImpl implements JobOrchestratorService {

    private final JobRepository jobRepository;
    private final JobProcessorRegistry processorRegistry;
    private final WorkerLog workerLog;

    @Override
    @Transactional
    public void handle(JobCreatedEvent event) {
        workerLog.stage(event.getJobId(), "RECEIVED", "handling jobType=" + event.getJobType());
        Job job = jobRepository.findById(event.getJobId())
                .orElseThrow(() -> new JobNotFoundWorkerException(event.getJobId()));

        workerLog.stage(job.getId(), "INITIAL", "status=" + job.getStatus());
        waitAndLog("pre-processing", job.getId(), 10);

        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);
        workerLog.stage(job.getId(), "STATUS_UPDATE", "status=" + job.getStatus());

        process(job, event);
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
}
