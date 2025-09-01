package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.MessageChannelEnum;
import github.oldLab.oldLab.dto.events.NotificationMessage;
import github.oldLab.oldLab.service.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSenderServiceImpl implements MessageSenderService {

    @Value("${kafka.topic.message}")
    private String messageTopic;

    @Value("${kafka.partition.message.email}")
    private String messagePartitionEmail;

    @Value("${kafka.partition.message.sms}")
    private String messagePartitionSms;

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    @Override
    public void sendOtp(MessageChannelEnum channel, String destination, int otp) {
        switch (channel) {
            case SMS -> sendSmsOtp(destination, otp);
            case EMAIL -> sendEmailOtp(destination, otp, false);
            default -> throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }

    @Override
    public void sendNotification(MessageChannelEnum channel, String destination, String message, boolean isHtml) {
        switch (channel) {
            case SMS -> sendSmsNotification(destination, message);
            case EMAIL -> sendEmailNotification(destination, message, isHtml);
            default -> throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }

    private void sendSmsOtp(String phoneNumber, int otp) {
        log.debug("sending OTP via SMS to phone number: {}", phoneNumber);
        NotificationMessage message = new NotificationMessage();
        message.setText(Integer.toString(otp));
        message.setHtml(false);
        message.setRecipient(phoneNumber);
        kafkaTemplate.send(messageTopic, messagePartitionSms, message);
    }

    private void sendSmsNotification(String phoneNumber, String text) {
        log.debug("sending notification via SMS to phone number: {}", phoneNumber);
        NotificationMessage message = new NotificationMessage();
        message.setText(text);
        message.setHtml(false);
        message.setRecipient(phoneNumber);
        message.setSubject("otp");
        kafkaTemplate.send(messageTopic, messagePartitionSms, message);
    }


    private void sendEmailOtp(String email, int otp, boolean isHtml) {
        log.debug("sending OTP via Email to: {}", email);
        NotificationMessage message = new NotificationMessage();
        message.setText(Integer.toString(otp));
        message.setHtml(isHtml);
        message.setRecipient(email);
        kafkaTemplate.send(messageTopic, messagePartitionEmail, message);
    }

    private void sendEmailNotification(String email, String text, boolean isHtml) {
        log.debug("sending notification via Email to: {}", email);
        NotificationMessage message = new NotificationMessage();
        message.setText(text);
        message.setHtml(isHtml);
        message.setRecipient(email);
        message.setSubject(text);
        kafkaTemplate.send(messageTopic, messagePartitionEmail, message);
    }
}
