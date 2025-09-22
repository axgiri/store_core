package github.oldLab.oldLab.seeder.factory;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.entity.Activate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivateFactory implements DataFactory<Activate> {

    private final Faker faker;

    @Override
    public Activate create() {
        return Activate.builder()
                .email(faker.internet().emailAddress())
                .otp(faker.number().numberBetween(100000, 999999))
                .otpReset(faker.number().numberBetween(100000, 999999))
                .isActive(true)
                .isLogin(false)
                .createdAt(Instant.now())
                .build();
    }
}
