package com.platform.job;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for Kafka producer configuration (Template Method Pattern).
 *
 * <p>Defines the skeleton for creating a {@link ProducerFactory} and {@link KafkaTemplate}:
 * <ol>
 *   <li>Builds the base producer properties (bootstrap servers, key serializer).</li>
 *   <li>Calls {@link #customizeProducerProps(Map)} — an abstract hook that each
 *       microservice implements to supply its own value serializer, acks, compression,
 *       retries, or any other producer-specific settings.</li>
 *   <li>Exposes the resulting beans for Spring injection.</li>
 * </ol>
 *
 * <p>Subclasses must be annotated with {@code @Configuration} in their own module.
 *
 * <pre>
 * Example:
 *   {@code @Configuration}
 *   public class KafkaProducerConfig extends AbstractKafkaProducerConfig {
 *       {@code @Override}
 *       protected void customizeProducerProps(Map<String, Object> props) {
 *           props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
 *       }
 *   }
 * </pre>
 */
public abstract class AbstractKafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Hook method — subclasses must override this to configure value serialization
     * and any other producer properties specific to their service.
     *
     * @param props the base properties map (bootstrap servers + key serializer already set)
     */
    protected abstract void customizeProducerProps(Map<String, Object> props);

    /**
     * Builds the base producer properties shared by all services.
     * Subclasses should call {@code super} via {@link #customizeProducerProps(Map)}
     * rather than overriding this method directly.
     */
    protected Map<String, Object> buildProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        customizeProducerProps(props);
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(buildProducerProps());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
