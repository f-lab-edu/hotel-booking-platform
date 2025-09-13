package dev.muho.user.domain.auth.service;

import dev.muho.user.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface TokenProvider {
    String createAccessToken(User user);
    String createRefreshToken(User user);
    boolean validate(String token);
    Optional<Long> extractUserId(String token);
}

