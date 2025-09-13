package dev.muho.user.domain.auth.controller;

import dev.muho.user.domain.auth.dto.api.AuthResponse;
import dev.muho.user.domain.auth.dto.api.LoginRequest;
import dev.muho.user.domain.auth.dto.api.TokenRefreshRequest;
import dev.muho.user.domain.auth.dto.command.AuthResult;
import dev.muho.user.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult authResult = authService.login(request);
        return ResponseEntity.ok(AuthResponse.of(authResult));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request);
    }

    @PostMapping("/token/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResult authResult = authService.refresh(request);
        return AuthResponse.of(authResult);
    }
}
