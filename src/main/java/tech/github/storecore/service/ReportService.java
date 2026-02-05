package tech.github.storecore.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tech.github.storecore.dto.events.ReportMessage;
import tech.github.storecore.dto.events.ReportMessage.ReportPayload;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.service.report.ReportStrategy;

@Slf4j
@Service
public class ReportService {

    private final KafkaTemplate<String, ReportMessage> reportKafkaTemplate;
    private final Map<ReportTypeEnum, ReportStrategy> strategyMap;

    public ReportService(KafkaTemplate<String, ReportMessage> reportKafkaTemplate, List<ReportStrategy> strategies) {
        this.reportKafkaTemplate = reportKafkaTemplate;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(ReportStrategy::getType, Function.identity()));
    }

    @Value("${kafka.topic.report}")
    private String reportTopic;

    @Value("${kafka.partition.report.create}")
    private String reportCreatePartition;

    public void createReport(ReportRequest request) {
        ReportStrategy strategy = getStrategy(request.getType());
        strategy.validate(request);
        sendReportMessage(request);

        log.info("Report created successfully: type={}, targetId={}", 
                request.getType(), request.getTargetId());
    }

    private ReportStrategy getStrategy(ReportTypeEnum type) {
        ReportStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported report type: " + type);
        }
        return strategy;
    }

    private void sendReportMessage(ReportRequest request) {
        ReportPayload payload = new ReportPayload(
                request.getReporterId(),
                request.getType(),
                request.getTargetId(),
                null,
                request.getReason(),
                request.getDetails());

        ReportMessage message = new ReportMessage(
                null,
                payload,
                null,
                Instant.now());

        reportKafkaTemplate.send(reportTopic, reportCreatePartition, message);
        log.debug("Report message sent to Kafka: topic={}, partition={}", reportTopic, reportCreatePartition);
    }
}
