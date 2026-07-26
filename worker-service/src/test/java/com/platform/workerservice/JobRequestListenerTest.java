package com.platform.workerservice;

import com.platform.job.events.JobCreatedEvent;
import com.platform.workerservice.logging.WorkerLog;
import com.platform.workerservice.service.JobOrchestratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JobRequestListenerTest {

    @Mock
    private JobOrchestratorService orchestratorService;

    @Mock
    private WorkerLog workerLog;

    @InjectMocks
    private JobRequestListener listener;

    @Test
    void listen_logsAndDelegatesEvent() {
        JobCreatedEvent event = JobCreatedEvent.builder()
                .jobId(UUID.randomUUID())
                .jobType("REPORT")
                .payload(Map.of("reportId", 101))
                .tenantId("tenant-1")
                .clientReqId("req-1")
                .build();

        listener.listen(event);

        verify(workerLog).received(event.getJobId(), event.getJobType(), event.getPayload());
        verify(orchestratorService).handle(event);
    }
}
