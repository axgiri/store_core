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

	private final int coreProcessors = Runtime.getRuntime().availableProcessors();

	@Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreProcessors - 1);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("T-");
        executor.initialize();
        return executor;
    }
}
