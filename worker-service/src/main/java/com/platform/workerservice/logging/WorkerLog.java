package com.platform.workerservice.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class WorkerLog {

    private static final String PREFIX = "[WORKER]";

    public void received(UUID jobId, String jobType, Map<String, Object> payload) {
        log.info("{} received jobId={} jobType={} payload={}", PREFIX, jobId, jobType, payload);
    }

    public void stage(UUID jobId, String stage, String message) {
        log.info("{} jobId={} stage={} {}", PREFIX, jobId, stage, message);
    }

    public void completed(UUID jobId) {
        log.info("{} jobId={} stage=COMPLETED job processing finished", PREFIX, jobId);
    }

    public void failed(UUID jobId, String stage, Exception ex) {
        log.error("{} jobId={} stage={} error={}", PREFIX, jobId, stage, ex.getMessage(), ex);
    }
}
