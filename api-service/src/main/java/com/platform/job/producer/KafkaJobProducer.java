package com.platform.job.producer;

import com.platform.job.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaJobProducer implements JobProducer {

    private static final String TOPIC = "job.requests";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(JobCreatedEvent event) {
        log.info("Sending job event to Kafka topic={} jobId={} jobType={}", TOPIC, event.getJobId(), event.getJobType());
        kafkaTemplate.send(TOPIC, event.getJobId().toString(), event);
    }
}
