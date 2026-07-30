package com.platform.workerservice.processor.simulation;

import com.platform.job.events.JobCreatedEvent;
import com.platform.job.model.Job;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ErrorSimulationRegistry {

    private final List<ErrorSimulationStrategy> strategies;
    private Map<String, ErrorSimulationStrategy> strategyMap;

    @PostConstruct
    public void init() {
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase(),
                        s -> s
                ));
    }

    public void runSimulation(String type, Job job, JobCreatedEvent event) {
        if (type == null) {
            return;
        }
        ErrorSimulationStrategy strategy = strategyMap.get(type.toLowerCase());
        if (strategy != null) {
            strategy.execute(job, event);
        }
    }
}
