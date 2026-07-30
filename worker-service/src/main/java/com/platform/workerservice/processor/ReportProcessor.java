package com.platform.workerservice.processor;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import com.platform.workerservice.processor.simulation.ErrorSimulationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProcessor implements JobProcessor {

    public static final String JOB_TYPE = "REPORT";
    
    private final ErrorSimulationRegistry simulationRegistry;

    @Override
    public String supportedJobType() {
        return JOB_TYPE;
    }

    @Override
    public void process(Job job, JobCreatedEvent event) {
        log.info("Simulating report processor execution for jobId={} jobType={} payload={}",
                event.getJobId(), event.getJobType(), event.getPayload());

        if (event.getPayload() != null && event.getPayload().containsKey("simulateError")) {
            String simType = String.valueOf(event.getPayload().get("simulateError"));
            simulationRegistry.runSimulation(simType, job, event);
        }
    }
}
