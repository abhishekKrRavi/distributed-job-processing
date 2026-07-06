package com.platform.workerservice.service;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;

public interface JobOrchestratorService {
    void handle(JobCreatedEvent event);
    void process(Job job, JobCreatedEvent event);
}
