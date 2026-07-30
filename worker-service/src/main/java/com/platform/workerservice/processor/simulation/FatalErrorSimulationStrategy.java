package com.platform.workerservice.processor.simulation;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FatalErrorSimulationStrategy implements ErrorSimulationStrategy {
    @Override
    public String getName() {
        return "fatal";
    }

    @Override
    public void execute(Job job, JobCreatedEvent event) {
        log.warn("Processor: throwing fatal exception for jobId={}", event.getJobId());
        throw new IllegalArgumentException("Simulated terminal/fatal processing error");
    }
}
