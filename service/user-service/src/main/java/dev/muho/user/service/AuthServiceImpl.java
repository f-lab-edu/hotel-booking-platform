package dev.muho.user.service;

import dev.muho.user.dto.api.LoginRequest;
import dev.muho.user.dto.api.TokenRefreshRequest;
import dev.muho.user.dto.command.AuthLoginCommand;
import dev.muho.user.dto.command.AuthLogoutCommand;
import dev.muho.user.dto.command.AuthResult;
import dev.muho.user.dto.command.TokenRefreshCommand;
import dev.muho.user.error.AuthenticationFailedException;
import dev.muho.user.repository.RefreshTokenStore;
import dev.muho.user.entity.User;
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
    private final TokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;

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

        String access = tokenProvider.createAccessToken(user);
        String refresh = tokenProvider.createRefreshToken(user);
        refreshTokenStore.store(user.getId(), refresh);

        return AuthResult.of(access, refresh);
    }

    @Override
    public AuthResult refresh(TokenRefreshCommand command) {
        Long userId = 1L; // TODO: 인증 정보에서 사용자 ID 추출
        User user = userRepository.findById(userId)
                .orElseThrow(AuthenticationFailedException::new);

        String oldRefresh = command.refreshToken();
        Long userIdFromRefreshToken = refreshTokenStore.getUserId(oldRefresh)
                .orElseThrow(AuthenticationFailedException::new);

        if (!userId.equals(userIdFromRefreshToken)) {
            // 리프레시 토큰이 해당 사용자에게 속하지 않음 (위조 또는 탈취 가능성)
            refreshTokenStore.revoke(oldRefresh); // 방어적 제거
            throw new AuthenticationFailedException();
        }

        if (!user.isActive()) {
            // 사용자 상태가 더 이상 활성 아님
            refreshTokenStore.revoke(oldRefresh); // 방어적 제거
            throw new AuthenticationFailedException();
        }

        String newAccess = tokenProvider.createAccessToken(user);
        String newRefresh = tokenProvider.createRefreshToken(user);
        refreshTokenStore.rotate(userId, oldRefresh, newRefresh);

        return AuthResult.of(newAccess, newRefresh);
    }

    @Override
    @Transactional
    public void logout(AuthLogoutCommand command) {
        if (command == null) return;
        String refresh = command.refreshToken();
        if (refresh == null || refresh.isBlank()) return;

        refreshTokenStore.revoke(refresh);
    }
}
