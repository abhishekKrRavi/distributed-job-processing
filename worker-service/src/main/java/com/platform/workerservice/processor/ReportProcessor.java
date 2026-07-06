package com.platform.workerservice.processor;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProcessor implements JobProcessor {

    public static final String JOB_TYPE = "REPORT";

    @Override
    public String supportedJobType() {
        return JOB_TYPE;
    }

    @Override
    public void process(Job job, JobCreatedEvent event) {
        log.info("Simulating report processor execution for jobId={} jobType={} payload={}",
                event.getJobId(), event.getJobType(), event.getPayload());
    }
}
