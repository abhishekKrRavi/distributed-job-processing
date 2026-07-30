package com.platform.workerservice.processor.simulation;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AlwaysErrorSimulationStrategy implements ErrorSimulationStrategy {
    @Override
    public String getName() {
        return "always";
    }

    @Override
    public void execute(Job job, JobCreatedEvent event) {
        log.warn("Processor: throwing transient exception (always) for jobId={}", event.getJobId());
        throw new RuntimeException("Simulated persistent transient processing error");
    }
}
