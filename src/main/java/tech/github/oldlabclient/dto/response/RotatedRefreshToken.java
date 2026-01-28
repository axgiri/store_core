package tech.github.oldlabclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.github.oldlabclient.entity.Person;

@Getter
@AllArgsConstructor
public class RotatedRefreshToken {
    private final String token;
    private final Person person;
}
