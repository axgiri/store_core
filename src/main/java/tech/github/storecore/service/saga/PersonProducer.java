package tech.github.storecore.service.saga;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tech.github.storecore.dto.events.RegistrationMessage;
import tech.github.storecore.dto.request.PersonCreateRequest;

@Service
@RequiredArgsConstructor
public class PersonProducer {
    private final KafkaTemplate<String, RegistrationMessage> registrationKafkaTemplate;

    @Value("${kafka.topic.registration}")
    private String registrationTopic;

    public void sendCreateUserEvent(PersonCreateRequest request, UUID idempotencyKey) {

        RegistrationMessage message = new RegistrationMessage(
                idempotencyKey,
                request.email(),
                request.password()
        );
        registrationKafkaTemplate.send(registrationTopic, message);
    }
}
