package tech.github.storecore.service.saga;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.dto.events.RegistrationCompensateMessage;
import tech.github.storecore.service.PersonService;

@Service
@Slf4j
@AllArgsConstructor
public class PersonConsumer{
    private final PersonService personService;

    @KafkaListener(
        topics = "${app.kafka.topic.registration-compensate}",
        containerFactory = "registrationCompensateKafkaListenerContainerFactory",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleRegistration(
            @Payload RegistrationCompensateMessage request,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received compensate event: key={}, partition={}, offset={}, id={}", key, partition, offset, request);
            personService.delete(request.getIdempotencyKey());
    }
}
