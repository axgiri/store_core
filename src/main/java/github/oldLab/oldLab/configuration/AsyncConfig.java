package github.oldLab.oldLab.configuration;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    @Primary
    public TaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        
        executor.setCorePoolSize(processors * 2);
        executor.setMaxPoolSize(processors * 4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setTaskDecorator(new SecurityContextPropagatingTaskDecorator());
        executor.initialize();
        return executor;
    }
    
    public static class SecurityContextPropagatingTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            SecurityContext context = SecurityContextHolder.getContext();
            return () -> {
                try {
                    SecurityContextHolder.setContext(context);
                    runnable.run();
                } finally {
                    SecurityContextHolder.clearContext();
                }
            };
        }
    }
}
