package github.oldLab.oldLab.dto.response;

import github.oldLab.oldLab.entity.Person;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RotatedRefreshToken {
    private final String token;
    private final Person person;
}
