package dev.muho.user.domain.auth.service;

import dev.muho.user.dto.api.LoginRequest;
import dev.muho.user.dto.api.LogoutRequest;
import dev.muho.user.dto.api.TokenRefreshRequest;
import dev.muho.user.dto.command.AuthLoginCommand;
import dev.muho.user.dto.command.AuthLogoutCommand;
import dev.muho.user.dto.command.AuthResult;
import dev.muho.user.dto.command.TokenRefreshCommand;
import dev.muho.user.error.AuthenticationFailedException;
import dev.muho.user.repository.RefreshTokenStore;
import dev.muho.user.entity.User;
import dev.muho.user.repository.UserRepository;
import dev.muho.user.service.PasswordHasher;
import dev.muho.user.service.AuthServiceImpl;
import dev.muho.user.service.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    TokenProvider tokenProvider;
    @Mock
    RefreshTokenStore refreshTokenStore;

    @InjectMocks
    AuthServiceImpl authService;

    @Test
    @DisplayName("login: 성공")
    void login_success() {
        LoginRequest req = LoginRequest.builder().email("a@b.com").password("password123").build();
        AuthLoginCommand command = AuthLoginCommand.from(req);
        User user = User.createNewUser("a@b.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678");
        given(userRepository.findByEmail("a@b.com")).willReturn(Optional.of(user));
        given(passwordHasher.matches("password123", user.getPassword())).willReturn(true);
        given(tokenProvider.createAccessToken(user)).willReturn("access-token");
        given(tokenProvider.createRefreshToken(user)).willReturn("refresh-token");

        AuthResult res = authService.login(command);

        assertThat(res.accessToken()).isEqualTo("access-token");
        assertThat(res.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("login: 이메일 없음 -> 예외")
    void login_noUser() {
        LoginRequest req = LoginRequest.builder().email("x@x.com").password("pw").build();
        AuthLoginCommand command = AuthLoginCommand.from(req);
        given(userRepository.findByEmail("x@x.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(command)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("login: 비활성 사용자 -> 예외")
    void login_inactive() {
        LoginRequest req = LoginRequest.builder().email("a@b.com").password("pw").build();
        AuthLoginCommand command = AuthLoginCommand.from(req);
        User user = User.createNewUser("a@b.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678");
        // withdraw to make inactive
        user.withdraw();
        given(userRepository.findByEmail("a@b.com")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(command)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("login: 비밀번호 불일치 -> 예외")
    void login_badPassword() {
        LoginRequest req = LoginRequest.builder().email("a@b.com").password("wrong").build();
        AuthLoginCommand command = AuthLoginCommand.from(req);
        User user = User.createNewUser("a@b.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678");
        given(userRepository.findByEmail("a@b.com")).willReturn(Optional.of(user));
        given(passwordHasher.matches("wrong", user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(command)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("refresh: 성공")
    void refresh_success() {
        String oldRefresh = "old-refresh";
        TokenRefreshRequest req = TokenRefreshRequest.builder().refreshToken(oldRefresh).build();
        TokenRefreshCommand command = TokenRefreshCommand.from(req);
        // AuthServiceImpl currently uses hardcoded userId = 1L
        User user = User.createNewUser("u@u.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(refreshTokenStore.getUserId(oldRefresh)).willReturn(Optional.of(1L));
        given(tokenProvider.createAccessToken(user)).willReturn("new-access");
        given(tokenProvider.createRefreshToken(user)).willReturn("new-refresh");

        AuthResult res = authService.refresh(command);

        assertThat(res.accessToken()).isEqualTo("new-access");
        assertThat(res.refreshToken()).isEqualTo("new-refresh");
        verify(refreshTokenStore).rotate(1L, oldRefresh, "new-refresh");
    }

    @Test
    @DisplayName("refresh: 토큰으로 사용자 조회 실패 -> 예외")
    void refresh_tokenNotFound() {
        TokenRefreshRequest req = TokenRefreshRequest.builder().refreshToken("missing").build();
        TokenRefreshCommand command = TokenRefreshCommand.from(req);
        given(userRepository.findById(1L)).willReturn(Optional.of(User.createNewUser("u@u.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678")));
        given(refreshTokenStore.getUserId("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(command)).isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    @DisplayName("refresh: 토큰 사용자 불일치 -> 예외 및 revoke 호출")
    void refresh_tokenUserMismatch() {
        String oldRefresh = "old";
        TokenRefreshRequest req = TokenRefreshRequest.builder().refreshToken(oldRefresh).build();
        TokenRefreshCommand command = TokenRefreshCommand.from(req);
        given(userRepository.findById(1L)).willReturn(Optional.of(User.createNewUser("u@u.com", "encoded-password-xxxxxxxxxxxxxxxxxxxxxxxxx", "name", "010-1234-5678")));
        given(refreshTokenStore.getUserId(oldRefresh)).willReturn(Optional.of(999L));

        assertThatThrownBy(() -> authService.refresh(command)).isInstanceOf(AuthenticationFailedException.class);
        verify(refreshTokenStore).revoke(oldRefresh);
    }

    @Test
    @DisplayName("logout: null 요청 무시")
    void logout_nullRequest() {
        authService.logout(null);
        verifyNoInteractions(refreshTokenStore);
    }

    @Test
    @DisplayName("logout: 빈 토큰 무시")
    void logout_blankToken() {
        LogoutRequest req = LogoutRequest.builder().refreshToken("").build();
        AuthLogoutCommand command = AuthLogoutCommand.from(req);
        authService.logout(command);
        verifyNoInteractions(refreshTokenStore);
    }

    @Test
    @DisplayName("logout: 정상 revoke 호출")
    void logout_success() {
        LogoutRequest req = LogoutRequest.builder().refreshToken("to-revoke").build();
        AuthLogoutCommand command = AuthLogoutCommand.from(req);
        authService.logout(command);
        verify(refreshTokenStore).revoke("to-revoke");
    }
}

