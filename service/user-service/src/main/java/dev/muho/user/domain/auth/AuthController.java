package dev.muho.user.domain.auth;

import dev.muho.user.domain.auth.dto.response.AuthResponse;
import dev.muho.user.domain.auth.dto.request.LoginRequest;
import dev.muho.user.domain.auth.dto.request.PasswordChangeRequest;
import dev.muho.user.domain.auth.dto.request.TokenRefreshRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final FakeAuthRepository authRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        boolean isValid = authRepository.validateCredentials(request);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        AuthResponse response = authRepository.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
    }

    @PostMapping("/token/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authRepository.refreshToken(request);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        // 내 이메일이 test@test.com 이라 가정
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.com")
                .password(request.getCurrentPassword())
                .build();

        boolean isValid = authRepository.validateCredentials(loginRequest);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        authRepository.changePassword(loginRequest.getEmail(), request);
        return ResponseEntity.noContent().build();
    }
}
