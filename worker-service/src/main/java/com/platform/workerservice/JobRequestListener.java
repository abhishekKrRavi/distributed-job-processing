package com.platform.workerservice;

import com.platform.job.events.JobCreatedEvent;
import com.platform.workerservice.service.JobOrchestratorService;
import com.platform.workerservice.logging.WorkerLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobRequestListener {

    private final JobOrchestratorService orchestratorService;
    private final WorkerLog workerLog;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("[WORKER] Kafka listener ready topic=job.requests groupId=${spring.kafka.consumer.group-id}");
    }

    @KafkaListener(topics = "job.requests", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(JobCreatedEvent event) {
        workerLog.received(event.getJobId(), event.getJobType(), event.getPayload());
        orchestratorService.handle(event);
    }
}
