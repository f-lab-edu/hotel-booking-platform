package dev.muho.user.domain.auth;

import dev.muho.user.domain.auth.dto.response.AuthResponse;
import dev.muho.user.domain.auth.dto.request.LoginRequest;
import dev.muho.user.domain.auth.dto.request.PasswordChangeRequest;
import dev.muho.user.domain.auth.dto.request.TokenRefreshRequest;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class FakeAuthRepository {

    // 테스트를 위해 임시로 여기에서 모든 것을 다 처리하도록 함

    @Getter
    @Builder
    static class UserAuth {
        String email;
        String password;
    }

    private final Map<Long, UserAuth> users = new HashMap<>();
    private long currentId = 1L;

    // 테스트를 위한 메서드
    public void save(String email, String password) {
        UserAuth userAuth = UserAuth.builder()
                .email(email)
                .password(password)
                .build();
        users.put(currentId++, userAuth);
    }

    public boolean validateCredentials(LoginRequest request) {
        return users.values().stream()
                .anyMatch(userAuth -> userAuth.getEmail().equals(request.getEmail()) && userAuth.getPassword().equals(request.getPassword()));
    }

    public AuthResponse login(LoginRequest request) {
        // 실제로는 JWT 토큰 생성 로직이 들어가야 함
        return AuthResponse.builder()
                .accessToken("fake-access-token")
                .refreshToken("fake-refresh-token")
                .build();
    }

    public AuthResponse refreshToken(TokenRefreshRequest request) {
        // 실제로는 JWT 토큰 재발급 로직이 들어가야 함
        return AuthResponse.builder()
                .accessToken("new-fake-access-token")
                .refreshToken("new-fake-refresh-token")
                .build();
    }

    public void changePassword(String email, PasswordChangeRequest request) {
        users.values().stream()
                .filter(userAuth -> userAuth.getEmail().equals(email) && userAuth.getPassword().equals(request.getCurrentPassword()))
                .findFirst()
                .ifPresent(userAuth -> userAuth.password = request.getNewPassword());
    }

    public void clear() {
        users.clear();
        currentId = 1L;
    }
}
