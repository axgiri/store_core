package github.oldLab.oldLab.dto.handler;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.service.PersonService;
import github.oldLab.oldLab.service.RefreshTokenService;
import github.oldLab.oldLab.serviceImpl.TokenServiceImpl;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenServiceImpl jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PersonService personService;

    @Value("${frontend.redirect.url}")
    private String frontendRedirectUrl;

    @Value("${jwt.refresh.ttl}")
    private int refreshTokenTTL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, java.io.IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");

        Person person = personService.upsertFromOAuth(email, firstName, lastName);

        String jwt = jwtTokenProvider.generateToken(person);
        String refreshToken = refreshTokenService.issue(person);

        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 25);

        Cookie refreshCookie = new Cookie("refresh", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(60 * 60 * 24 * refreshTokenTTL);

        response.addCookie(jwtCookie);
        response.addCookie(refreshCookie);

        response.sendRedirect(frontendRedirectUrl);
    }
}
