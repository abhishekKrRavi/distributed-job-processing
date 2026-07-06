package com.platform.workerservice.processor;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;

public interface JobProcessor {

    String supportedJobType();

    void process(Job job, JobCreatedEvent event);
}
