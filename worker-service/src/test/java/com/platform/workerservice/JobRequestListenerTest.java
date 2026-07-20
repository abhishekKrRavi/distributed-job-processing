package com.platform.workerservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.platform.job.events.JobCreatedEvent;
import com.platform.workerservice.service.JobOrchestratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobRequestListenerTest {

    @Mock
    private JobOrchestratorService orchestratorService;

    @InjectMocks
    private JobRequestListener listener;

    @Test
    void listen_logsAndDelegatesEvent() {
        Logger logger = (Logger) LoggerFactory.getLogger(JobRequestListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        JobCreatedEvent event = JobCreatedEvent.builder()
                .jobId(UUID.randomUUID())
                .jobType("REPORT")
                .payload(Map.of("reportId", 101))
                .tenantId("tenant-1")
                .clientReqId("req-1")
                .build();

        listener.listen(event);

        verify(orchestratorService).handle(event);
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anySatisfy(message -> assertThat(message).contains("Received Job"))
                .anySatisfy(message -> assertThat(message).contains("jobType=REPORT"));

        logger.detachAppender(appender);
    }
}
