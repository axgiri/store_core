package github.oldLab.oldLab.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import github.oldLab.oldLab.dto.events.ReportEvent;
import github.oldLab.oldLab.dto.events.ReviewEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;


import java.util.HashMap;
import java.util.Map;

@Configuration //TODO: check this config and improve it
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private ProducerFactory<String, ReportEvent> reportEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        DefaultKafkaProducerFactory<String, ReportEvent> factory =
                new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper()));
        return factory;
    }

    private ProducerFactory<String, ReviewEvent> reviewEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        DefaultKafkaProducerFactory<String, ReviewEvent> factory =
                new DefaultKafkaProducerFactory<>(configProps);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper()));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, ReportEvent> reportEventKafkaTemplate() {
        return new KafkaTemplate<>(reportEventProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, ReviewEvent> reviewEventKafkaTemplate() {
        return new KafkaTemplate<>(reviewEventProducerFactory());
    }

    private NewTopic createTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(2)
                .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
                .build();
    }

    private NewTopic createDltTopic(String name) {
        return TopicBuilder.name(name + "-dlt")
                .partitions(3)
                .replicas(2)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000")
                .build();
    }

    @Bean
    public NewTopic reviewEventsTopic() {
        return createTopic("review-events");
    }

    @Bean
    public NewTopic reportEventsTopic() {
        return createTopic("report-events");
    }

    @Bean
    public NewTopic reviewEventsDlt() {
        return createDltTopic("review-events");
    }

    @Bean
    public NewTopic reportEventsDlt() {
        return createDltTopic("report-events");
    }
}
