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
    // Prioritize email: generate a unique email; phone is optional (can be null)
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String unique = Long.toString(Math.abs(ThreadLocalRandom.current().nextLong()), 36);
    String email = (unique + "@oldlab.cis").toLowerCase();

    // 50% chance to set a phone number, otherwise leave null (phone is not primary)
    String phone = ThreadLocalRandom.current().nextBoolean()
        ? "+" + ThreadLocalRandom.current().nextInt(1, 10) + faker.number().digits(10)
        : null;

    return new Person()
        .setFirstName(firstName)
        .setLastName(lastName)
        .setPhoneNumber(phone)
        .setEmail(email)
        // Set password based on email to align with email-first auth
        .setPassword(passwordEncoder.encode(email))
        .setRoleEnum(RoleEnum.values()[ThreadLocalRandom.current().nextInt(RoleEnum.values().length)])
        .setIsActive(true)
        .setCreatedAt(Instant.now())
        .setUpdatedAt(Instant.now());
    }
}
