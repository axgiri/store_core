package github.oldLab.oldLab.seeder.factory;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.entity.Activates;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivateFactory implements DataFactory<Activates> {

    private final Faker faker;

    @Override
    public Activates create() {
        return Activates.builder()
                .email(faker.internet().emailAddress())
                .otp(faker.number().numberBetween(100000, 999999))
                .otpReset(faker.number().numberBetween(100000, 999999))
                .isActive(true)
                .isLogin(false)
                .createdAt(Instant.now())
                .build();
    }
}
