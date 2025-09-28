package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.RoleEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl extends DefaultOAuth2UserService {
    private final PersonRepository persons;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        String email = user.getAttribute("email");
        String firstName = user.getAttribute("given_name");
        String lastName = user.getAttribute("family_name");
        Person person = persons.findByEmail(email)
                .orElseGet(() -> {
                    Person newPerson = new Person();
                    newPerson.setEmail(email);
                    newPerson.setFirstName(firstName != null ? firstName : "");
                    newPerson.setLastName(lastName != null ? lastName : "");
                    newPerson.setIsActive(true);
                    newPerson.setRoleEnum(RoleEnum.USER);
                    newPerson.setCreatedAt(Instant.now());
                    newPerson.setUpdatedAt(Instant.now());
                    // zaglushka
                    newPerson.setPassword(UUID.randomUUID().toString());
                    return persons.save(newPerson);
                });

        person.setFirstName(firstName != null ? firstName : person.getFirstName());
        person.setLastName(lastName != null ? lastName : person.getLastName());
        person.setUpdatedAt(Instant.now());
        persons.save(person);

        return user;
    }
}
