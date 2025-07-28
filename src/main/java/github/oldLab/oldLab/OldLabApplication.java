package github.oldLab.oldLab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableAspectJAutoProxy
@SpringBootApplication
public class OldLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(OldLabApplication.class, args);
    }
}
