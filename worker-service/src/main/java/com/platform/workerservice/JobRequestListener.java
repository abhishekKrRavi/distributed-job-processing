package com.platform.workerservice;

import com.platform.job.events.JobCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class JobRequestListener {

    @KafkaListener(topics = "job.requests", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(JobCreatedEvent event) {
        System.out.println("Received Job");
        System.out.println("Job Id: " + event.getJobId());
        System.out.println("Job Type: " + event.getJobType());
        System.out.println("Payload: " + event.getPayload());
    }
}
