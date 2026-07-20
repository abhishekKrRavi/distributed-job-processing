package com.platform.job.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.platform.job.dto.JobSubmitRequest;
import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.job.model.JobStatus;
import com.platform.job.producer.JobProducer;
import com.platform.job.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobProducer jobProducer;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    void submitJob_savesJob_logsAndPublishesEvent() {
        Logger logger = (Logger) LoggerFactory.getLogger(JobServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        UUID jobId = UUID.randomUUID();
        JobSubmitRequest request = JobSubmitRequest.builder()
                .jobType("REPORT")
                .tenantId("tenant-1")
                .payload(Map.of("reportId", 101))
                .build();

        Job savedJob = Job.builder()
                .id(jobId)
                .jobType("REPORT")
                .tenantId("tenant-1")
                .payload(Map.of("reportId", 101))
                .status(JobStatus.PENDING)
                .retryCount(0)
                .build();

//        when(jobRepository.findByClientReqId(any())).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        jobService.submitJob(request, null);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        verify(jobProducer).publish(any(JobCreatedEvent.class));
        verify(jobRepository, never()).deleteById(any());

        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(JobStatus.PENDING);
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anySatisfy(message -> assertThat(message).contains("Submitting job request"))
                .anySatisfy(message -> assertThat(message).contains("Job saved jobId="));

        logger.detachAppender(appender);
    }
}
