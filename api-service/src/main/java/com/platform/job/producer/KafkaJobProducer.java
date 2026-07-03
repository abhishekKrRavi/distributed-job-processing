package com.platform.job.producer;

import com.platform.job.events.JobCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaJobProducer implements JobProducer {

    private static final String TOPIC = "job.requests";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(JobCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.getJobId().toString(), event);
    }
}
