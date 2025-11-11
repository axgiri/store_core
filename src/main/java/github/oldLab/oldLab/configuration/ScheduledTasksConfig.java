package github.oldLab.oldLab.configuration;

import java.util.concurrent.atomic.AtomicBoolean;

import github.oldLab.oldLab.service.ActivateService;
import github.oldLab.oldLab.service.PersonService;
import github.oldLab.oldLab.serviceImpl.RefreshTokenServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasksConfig {

    private final ActivateService activateService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final PersonService personService;
    private final AtomicBoolean cleanupActivateInProgress = new AtomicBoolean(false);
    private final AtomicBoolean cleanupTokensInProgress = new AtomicBoolean(false);
    private final AtomicBoolean cleanupPersonsInProgress = new AtomicBoolean(false);

    @Value("${app.cleanup.cron}")
    private String cleanupCron;

    @Value("${app.cleanup.timezone}")
    private String timezone;

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupActiveTable() {
        if (!cleanupActivateInProgress.compareAndSet(false, true)) {
            log.warn("Previous activate cleanup still in progress, skipping");
            return;
        }
        try {
            log.info("Starting activate table cleanup");
            activateService.cleanupOldRecords();
            log.info("Activate table cleanup completed");
        } catch (Exception e) {
            log.error("Error during activate cleanup", e);
        } finally {
            cleanupActivateInProgress.set(false);
        }
    }

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupExpiredTokens() {
        if (!cleanupTokensInProgress.compareAndSet(false, true)) {
            log.warn("Previous token cleanup still in progress, skipping");
            return;
        }
        try {
            log.info("Starting expired tokens cleanup");
            refreshTokenService.cleanupExpiredTokens();
            log.info("Expired tokens cleanup completed");
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        } finally {
            cleanupTokensInProgress.set(false);
        }
    }

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupInactiveUsers() {
        if (!cleanupPersonsInProgress.compareAndSet(false, true)) {
            log.warn("Previous persons cleanup still in progress, skipping");
            return;
        }
        try {
            log.info("Starting inactive persons cleanup");
            personService.cleanupInactivePersons();
            log.info("Inactive persons cleanup completed");
        } catch (Exception e) {
            log.error("Error during persons cleanup", e);
        } finally {
            cleanupPersonsInProgress.set(false);
        }
    }
}