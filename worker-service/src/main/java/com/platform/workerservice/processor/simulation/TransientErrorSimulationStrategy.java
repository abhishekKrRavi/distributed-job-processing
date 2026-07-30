package com.platform.workerservice.processor.simulation;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransientErrorSimulationStrategy implements ErrorSimulationStrategy {
    @Override
    public String getName() {
        return "transient";
    }

    @Override
    public void execute(Job job, JobCreatedEvent event) {
        if (job.getRetryCount() == 0) {
            log.warn("Processor: throwing transient exception on attempt 1 for jobId={}", event.getJobId());
            throw new RuntimeException("Simulated transient processing error on first attempt");
        } else {
            log.info("Processor: transient error recovered on retry attempt {} for jobId={}", job.getRetryCount(), event.getJobId());
        }
    }
}
