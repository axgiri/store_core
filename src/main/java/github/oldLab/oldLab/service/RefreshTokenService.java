package github.oldLab.oldLab.service;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.RefreshToken;

public interface RefreshTokenService {

    String issue(Person person);
    
    RefreshToken rotate(String token);
    
    void revoke(String token);

    void revokeAllForPerson(String refreshToken);
}
