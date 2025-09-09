package github.oldLab.oldLab.seeder.factory;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PersonFactory implements DataFactory<Person> {

    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Person create() {
        var phone = "+" + faker.number().digits(11);
        return new Person()
                .setFirstName(faker.name().firstName())
                .setLastName(faker.name().lastName())
                .setPhoneNumber(phone)
                .setEmail(phone + "@example.com")
                .setPassword(passwordEncoder.encode(phone))
                .setRoleEnum(RoleEnum.values()[ThreadLocalRandom.current().nextInt(RoleEnum.values().length)])
                .setCompanyId(ThreadLocalRandom.current().nextBoolean() ? faker.number().numberBetween(1L, 1000L) : null)
                .setIsActive(true)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(Instant.now());
    }
}
