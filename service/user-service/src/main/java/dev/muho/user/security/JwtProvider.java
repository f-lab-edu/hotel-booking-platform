package dev.muho.user.security;

import dev.muho.user.entity.User;
import org.springframework.security.core.Authentication;

public interface JwtProvider {

    String createAccessToken(User user);

    String createRefreshToken(User user);

    Authentication getAuthentication(String accessToken);

    boolean validateToken(String accessToken);
}
