package dev.muho.user.service;

import dev.muho.user.dto.command.AuthLoginCommand;
import dev.muho.user.dto.command.AuthLogoutCommand;
import dev.muho.user.dto.command.AuthResult;
import dev.muho.user.dto.command.TokenRefreshCommand;
import dev.muho.user.error.AuthenticationFailedException;
import dev.muho.user.entity.User;
import dev.muho.user.security.JwtProvider;
import dev.muho.user.security.PasswordHasher;
import dev.muho.user.redis.RefreshTokenService;
import dev.muho.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResult login(AuthLoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(AuthenticationFailedException::new);

        if (!user.isActive()) {
            throw new AuthenticationFailedException();
        }
        if (!passwordHasher.matches(command.password(), user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        String access = jwtProvider.createAccessToken(user);
        String refresh = jwtProvider.createRefreshToken(user);
        refreshTokenService.saveRefreshToken(user.getId(), refresh);

        return AuthResult.of(access, refresh);
    }

    @Override
    public AuthResult refresh(Long userId, TokenRefreshCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);

        String oldRefresh = command.refreshToken();
        Long userIdFromRefreshToken = refreshTokenService.findUserIdByToken(oldRefresh);

        if (!userId.equals(userIdFromRefreshToken)) {
            refreshTokenService.deleteRefreshToken(oldRefresh);
            throw new AuthenticationFailedException();
        }
        if (!user.isActive()) {
            refreshTokenService.deleteRefreshToken(oldRefresh);
            throw new AuthenticationFailedException();
        }

        String newAccess = jwtProvider.createAccessToken(user);
        String newRefresh = jwtProvider.createRefreshToken(user);
        refreshTokenService.rotateRefreshToken(userId, oldRefresh, newRefresh);

        return AuthResult.of(newAccess, newRefresh);
    }

    @Override
    @Transactional
    public void logout(Long userId, AuthLogoutCommand command) {
        if (command == null) return;
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) return;

        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);

        Long userIdFromRefreshToken = refreshTokenService.findUserIdByToken(refreshToken);

        if (!userId.equals(userIdFromRefreshToken)) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            throw new AuthenticationFailedException();
        }
        if (!user.isActive()) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            throw new AuthenticationFailedException();
        }

        refreshTokenService.deleteRefreshToken(refreshToken);
    }
}
