package tech.github.storecore.configuration;

import java.time.Duration;
import java.util.List;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import tech.github.storecore.dto.events.RegistrationCompensateMessage;
import tech.github.storecore.dto.events.RegistrationMessage;
import tech.github.storecore.dto.events.ReportMessage;
import tech.github.storecore.dto.events.ReviewMessage;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.review}")
    private String reviewTopic;

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.topic.registration}")
    private String registrationTopic;

    @Value("${app.kafka.replicas}")
    private int replicas;

    @Value("${app.kafka.min-insync-replicas}")
    private int minInSyncReplicas;

    private <T> KafkaTemplate<String, T> createKafkaTemplate(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        var props = kafkaProperties.buildProducerProperties();
        var serializer = new JacksonJsonSerializer<T>(jsonMapper);
        serializer.setAddTypeInfo(false);
        var factory = new DefaultKafkaProducerFactory<>(props, new StringSerializer(), serializer);
        var template = new KafkaTemplate<>(factory);
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public KafkaTemplate<String, ReportMessage> reportKafkaTemplate(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        return createKafkaTemplate(kafkaProperties, jsonMapper);
    }

    @Bean
    public KafkaTemplate<String, ReviewMessage> reviewKafkaTemplate(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        return createKafkaTemplate(kafkaProperties, jsonMapper);
    }

    @Bean
    public KafkaTemplate<String, RegistrationMessage> registrationKafkaTemplate(KafkaProperties kafkaProperties,JsonMapper jsonMapper) {
        return createKafkaTemplate(kafkaProperties, jsonMapper);
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, ReportMessage> reportKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(reportKafkaTemplate, (event, ex) -> {
                String dlt = event.topic() + ".DLT";
                return new TopicPartition(dlt, event.partition());
            });

        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(recoverer);
        defaultErrorHandler.addNotRetryableExceptions(SerializationException.class);
        return defaultErrorHandler;
    }

        @Bean
    public ConsumerFactory<String, RegistrationCompensateMessage> registrationCompensateConsumerFactory(KafkaProperties kafkaProperties, JsonMapper jsonMapper) {
        var props = kafkaProperties.buildConsumerProperties();
        var deserializer = new JacksonJsonDeserializer<>(RegistrationCompensateMessage.class, jsonMapper);
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RegistrationCompensateMessage> registrationCompensateKafkaListenerContainerFactory(
            ConsumerFactory<String, RegistrationCompensateMessage> registrationCompensateConsumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, RegistrationCompensateMessage>();
        factory.setConsumerFactory(registrationCompensateConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
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
        return TopicBuilder.name(name + ".DLT")
                .partitions(1)
                .replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(30).toMillis()))
                .config("min.insync.replicas", String.valueOf(minInSyncReplicas))
                .build();
    }

    private List<NewTopic> createTopicWithDlt(String name) {
        return List.of(baseTopic(name), dltTopic(name));
    }

    @Bean
    public List<NewTopic> reviewTopics() {
        return createTopicWithDlt(reviewTopic);
    }

    @Bean
    public List<NewTopic> reportTopics() {
        return createTopicWithDlt(reportTopic);
    }

    @Bean
    public List<NewTopic> registrationTopics() {
        return createTopicWithDlt(registrationTopic);
    }
}
