package github.oldLab.oldLab.configuration;

import github.oldLab.oldLab.service.ActivateService;
import github.oldLab.oldLab.service.PersonService;
import github.oldLab.oldLab.serviceImpl.RefreshTokenServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTasksConfig {

    private final ActivateService activateService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final PersonService personService;

    @Value("${app.cleanup.cron}")
    private String cleanupCron;

    @Value("${app.cleanup.timezone}")
    private String timezone;

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupActiveTable() {
        activateService.cleanupOldRecords();
    }

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupExpiredTokens() {
        refreshTokenService.cleanupExpiredTokens();
    }

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupInactiveUsers() {
        personService.cleanupInactivePersons();
    }
}