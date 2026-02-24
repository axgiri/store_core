package tech.github.storecore.service.report;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.github.storecore.client.NotificationReportsClient;
import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportReasonEnum;
import tech.github.storecore.enumeration.ReportTypeEnum;
import tech.github.storecore.exception.DuplicateReportException;
import tech.github.storecore.exception.UserNotFoundException;
import tech.github.storecore.service.PersonService;

@ExtendWith(MockitoExtension.class)
class UserReportStrategyTest {

    @Mock private PersonService personService;
    @Mock private NotificationReportsClient notificationClient;

    @InjectMocks
    private UserReportStrategy strategy;

    private ReportRequest request(UUID targetId) {
        var req = new ReportRequest();
        req.setType(ReportTypeEnum.USER);
        req.setTargetId(targetId);
        req.setReason(ReportReasonEnum.SCAM);
        return req;
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("passes when all validations succeed")
        void passes_whenValid() {
            var reporterId = UUID.randomUUID();
            var targetId = UUID.randomUUID();
            var req = request(targetId);

            when(personService.existsById(reporterId)).thenReturn(true);
            when(personService.existsById(targetId)).thenReturn(true);
            when(notificationClient.hasReportByReporter(reporterId, targetId, ReportTypeEnum.USER))
                    .thenReturn(false);

            assertThatCode(() -> strategy.validate(reporterId, req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("throws when reporter tries to report themselves")
        void throws_whenSelfReport() {
            var id = UUID.randomUUID();
            var req = request(id);

            assertThatThrownBy(() -> strategy.validate(id, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("yourself");
        }

        @Test
        @DisplayName("throws when reporter not found")
        void throws_whenReporterMissing() {
            var reporterId = UUID.randomUUID();
            var req = request(UUID.randomUUID());
            when(personService.existsById(reporterId)).thenReturn(false);

            assertThatThrownBy(() -> strategy.validate(reporterId, req))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Reporter");
        }

        @Test
        @DisplayName("throws when target user not found")
        void throws_whenTargetMissing() {
            var reporterId = UUID.randomUUID();
            var targetId = UUID.randomUUID();
            var req = request(targetId);

            when(personService.existsById(reporterId)).thenReturn(true);
            when(personService.existsById(targetId)).thenReturn(false);

            assertThatThrownBy(() -> strategy.validate(reporterId, req))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Target");
        }

        @Test
        @DisplayName("throws DuplicateReportException when already reported")
        void throws_whenDuplicate() {
            var reporterId = UUID.randomUUID();
            var targetId = UUID.randomUUID();
            var req = request(targetId);

            when(personService.existsById(reporterId)).thenReturn(true);
            when(personService.existsById(targetId)).thenReturn(true);
            when(notificationClient.hasReportByReporter(reporterId, targetId, ReportTypeEnum.USER))
                    .thenReturn(true);

            assertThatThrownBy(() -> strategy.validate(reporterId, req))
                    .isInstanceOf(DuplicateReportException.class);
        }
    }

    @Test
    @DisplayName("getType returns USER")
    void getTypeReturnsUser() {
        assertThatCode(() -> {
            var type = strategy.getType();
            assert type == ReportTypeEnum.USER;
        }).doesNotThrowAnyException();
    }
}
