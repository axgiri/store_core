package github.oldLab.oldLab.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    @Primary
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        int corePool = Math.max(2, processors - 1); //to not make it NULL, you can change it. minimum 2 threads
        int maxPool = corePool * 2;
        executor.setCorePoolSize(corePool);
        executor.setMaxPoolSize(maxPool);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("T-");
        executor.initialize();
        return executor;
    }
}
