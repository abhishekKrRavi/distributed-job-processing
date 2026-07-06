package com.platform.workerservice.processor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReportProcessorTest {

    @Test
    void process_logsSimulationMessage() {
        Logger logger = (Logger) LoggerFactory.getLogger(ReportProcessor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

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

        processor.process(job, event);

        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anySatisfy(message -> assertThat(message).contains("Simulating report processor execution"));

        logger.detachAppender(appender);
    }
}
