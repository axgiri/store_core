package github.oldLab.oldLab;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync(proxyTargetClass = true)
@EnableAspectJAutoProxy
@SpringBootApplication
public class OldLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(OldLabApplication.class, args);
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
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
