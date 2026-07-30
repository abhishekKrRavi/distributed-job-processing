package com.platform.workerservice;

import com.platform.job.AbstractKafkaProducerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 * Kafka producer configuration for the worker service.
 *
 * <p>Extends {@link AbstractKafkaProducerConfig} and configures JSON value serialization
 * for publishing exhausted jobs to the Dead Letter Queue (DLQ) topic.
 *
 * <p>Future customizations specific to the worker (e.g., higher retries, idempotent
 * producer settings, or different acks for reliability) should be added here
 * via {@link #customizeProducerProps(Map)}.
 */
@Configuration
public class KafkaProducerConfig extends AbstractKafkaProducerConfig {

    @Override
    protected void customizeProducerProps(Map<String, Object> props) {
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    }
}
