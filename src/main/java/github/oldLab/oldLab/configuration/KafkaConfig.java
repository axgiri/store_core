package github.oldLab.oldLab.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    @Value("${kafka.dlt.review}")
    private String reviewDltTopic;

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.dlt.report}")
    private String reportDltTopic;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public Map<String, Object> producerProps() {
        Map<String, Object> producer = new HashMap<>();
        producer.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        producer.put(ProducerConfig.ACKS_CONFIG, "all");
        producer.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producer.put(ProducerConfig.RETRIES_CONFIG, 10);
        producer.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        producer.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "zstd");
        producer.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        producer.put(ProducerConfig.BATCH_SIZE_CONFIG, 64 * 1024);

        producer.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);
        producer.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30_000);

        producer.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return producer;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper mapper, Map<String, Object> producerProps) {
        DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        producerFactory.setValueSerializer(new JsonSerializer<>(mapper));
        return producerFactory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(template, (record, ex) -> {
                String dlt = record.topic();
                return new TopicPartition(dlt, record.partition());
            });

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(recoverer);
        defaultErrorHandler.addNotRetryableExceptions(SerializationException.class);
        return defaultErrorHandler;
    }

    private NewTopic baseTopic(String name) {
        return TopicBuilder.name(name)
            .partitions(3)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
            .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
            .build();
    }

    private NewTopic dltTopic(String name) {
        return TopicBuilder.name(name)
            .partitions(1)
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(30).toMillis()))
            .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
            .build();
    }

    @Bean 
    public NewTopic reviewEventsTopic() {
        return baseTopic(reviewTopic);
    }

    @Bean 
    public NewTopic reportEventsTopic() {
        return baseTopic(reportTopic);
    }

    @Bean 
    public NewTopic reviewEventsDlt() {
        return dltTopic(reviewDltTopic);
    }

    @Bean 
    public NewTopic reportEventsDlt() {
        return dltTopic(reportDltTopic);
    }
}
