package github.oldLab.oldLab.configuration;

import github.oldLab.oldLab.service.ActivateService;
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

    @Value("${app.cleanup.cron}")
    private String cleanupCron;

    @Value("${app.cleanup.timezone}")
    private String timezone;

    @Scheduled(cron = "${app.cleanup.cron}", zone = "${app.cleanup.timezone}")
    public void cleanupActiveTable() {
        activateService.cleanupOldRecords();
    }
}