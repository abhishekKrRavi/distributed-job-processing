package com.platform.workerservice.processor.simulation;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;

public interface ErrorSimulationStrategy {
    String getName();
    void execute(Job job, JobCreatedEvent event);
}
