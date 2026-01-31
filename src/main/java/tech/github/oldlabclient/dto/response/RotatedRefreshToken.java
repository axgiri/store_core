package tech.github.oldlabclient.dto.response;

import tech.github.oldlabclient.entity.Person;

public record RotatedRefreshToken(
    String token,
    Person person
) {}
