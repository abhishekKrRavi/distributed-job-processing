package com.platform.workerservice.processor;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;


class ReportProcessorTest {

    @Test
    void process_logsSimulationMessage() {
        ReportProcessor processor = new ReportProcessor();
        Job job = Job.builder()
                .id(UUID.randomUUID())
                .jobType("REPORT")
                .status(JobStatus.PROCESSING)
                .payload(Map.of("reportId", 101))
                .build();
        JobCreatedEvent event = JobCreatedEvent.builder()
                .jobId(job.getId())
                .jobType("REPORT")
                .payload(Map.of("reportId", 101))
                .tenantId("tenant-1")
                .clientReqId("req-1")
                .build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> processor.process(job, event));
    }
}
