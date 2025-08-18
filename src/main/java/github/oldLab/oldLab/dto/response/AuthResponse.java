package github.oldLab.oldLab.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AuthResponse {

    @JsonProperty("access_token")
    private String token;
    
    @JsonProperty("refresh_token")
    private String refreshToken;

    private PersonResponse person;

    public AuthResponse(String token, PersonResponse person) {
        this.token = token;
        this.person = person;
    }

    public AuthResponse(String token, String refreshToken, PersonResponse person) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.person = person;
    }
}
