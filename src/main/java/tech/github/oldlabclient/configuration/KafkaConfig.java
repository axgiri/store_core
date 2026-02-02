package tech.github.oldlabclient.configuration;

import java.time.Duration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import tech.github.oldlabclient.dto.events.ReportMessage;
import tech.github.oldlabclient.dto.events.ReviewMessage;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    @Value("${kafka.dlt.review}")
    private String reviewDltTopic;

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.dlt.report}")
    private String reportDltTopic;

    @Value("${app.kafka.replicas}")
    private int replicas;

    @Value("${app.kafka.min-insync-replicas}")
    private int minInSyncReplicas;

    @Bean
    public ProducerFactory<String, ReportMessage> reportProducerFactory(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        var props = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JacksonJsonSerializer<>(jsonMapper));
    }

    @Bean
    public KafkaTemplate<String, ReportMessage> reportKafkaTemplate(ProducerFactory<String, ReportMessage> reportProducerFactory) {
        return new KafkaTemplate<>(reportProducerFactory);
    }

    @Bean
    public ProducerFactory<String, ReviewMessage> reviewProducerFactory(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        var props = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JacksonJsonSerializer<>(jsonMapper));
    }

    @Bean
    public KafkaTemplate<String, ReviewMessage> reviewKafkaTemplate(ProducerFactory<String, ReviewMessage> reviewProducerFactory) {
        return new KafkaTemplate<>(reviewProducerFactory);
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, ReportMessage> reportKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(reportKafkaTemplate, (event, ex) -> {
                String dlt = event.topic();
                return new TopicPartition(dlt, event.partition());
            });

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(recoverer);
        defaultErrorHandler.addNotRetryableExceptions(SerializationException.class);
        return defaultErrorHandler;
    }

    private NewTopic baseTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
                .config("min.insync.replicas", String.valueOf(minInSyncReplicas))
                .build();
    }

    private NewTopic dltTopic(String name) {
        return TopicBuilder.name(name)
                .partitions(1)
                .replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(30).toMillis()))
                .config("min.insync.replicas", String.valueOf(minInSyncReplicas))
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
