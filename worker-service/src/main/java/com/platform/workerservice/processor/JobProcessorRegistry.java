package com.platform.workerservice.processor;

import com.platform.exception.UnsupportedJobTypeWorkerException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobProcessorRegistry {

    private final Map<String, JobProcessor> processors;

    public JobProcessorRegistry(List<JobProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        processor -> processor.supportedJobType().toUpperCase(),
                        processor -> processor
                ));
    }

    public JobProcessor resolve(String jobType) {
        JobProcessor processor = processors.get(jobType.toUpperCase());
        if (processor == null) {
            throw new UnsupportedJobTypeWorkerException(jobType);
        }
        return processor;
    }
}
