package tech.github.storecore.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import tech.github.storecore.dto.events.ReportMessage;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportReasonEnum;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.service.report.ReportStrategy;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private KafkaTemplate<String, ReportMessage> reportKafkaTemplate;
    @Mock private ReportStrategy userReportStrategy;
    @Mock private ReportStrategy reviewReportStrategy;

    private ReportService createService(ReportTypeEnum strategyType) {
        ReportStrategy strategy = strategyType == ReportTypeEnum.USER ? userReportStrategy : reviewReportStrategy;
        when(strategy.getType()).thenReturn(strategyType);

        var service = new ReportService(reportKafkaTemplate, List.of(strategy));
        ReflectionTestUtils.setField(service, "reportTopic", "reports");
        return service;
    }

    private ReportRequest reportRequest(ReportTypeEnum type) {
        var req = new ReportRequest();
        req.setType(type);
        req.setTargetId(UUID.randomUUID());
        req.setReason(ReportReasonEnum.SPAM);
        req.setDetails("spam account");
        return req;
    }

    @Nested
    @DisplayName("createReport")
    class CreateReport {

        @Test
        @DisplayName("validates via strategy and sends Kafka message for USER report")
        void sendsUserReport() {
            var service = createService(ReportTypeEnum.USER);
            var reporterId = UUID.randomUUID();
            var request = reportRequest(ReportTypeEnum.USER);

            service.createReport(reporterId, request);

            verify(userReportStrategy).validate(reporterId, request);
            verify(reportKafkaTemplate).send(eq("reports"), any(ReportMessage.class));
        }

        @Test
        @DisplayName("throws for unsupported report type")
        void throws_whenUnsupportedType() {
            var reporterId = UUID.randomUUID();
            var service = createService(ReportTypeEnum.USER);
            var request = reportRequest(ReportTypeEnum.REVIEW);

            assertThatThrownBy(() -> service.createReport(reporterId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported");
        }
    }
}
