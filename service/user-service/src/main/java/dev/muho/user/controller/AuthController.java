package dev.muho.user.controller;

import dev.muho.user.dto.api.AuthResponse;
import dev.muho.user.dto.api.LoginRequest;
import dev.muho.user.dto.api.LogoutRequest;
import dev.muho.user.dto.api.TokenRefreshRequest;
import dev.muho.user.dto.command.AuthLoginCommand;
import dev.muho.user.dto.command.AuthLogoutCommand;
import dev.muho.user.dto.command.AuthResult;
import dev.muho.user.dto.command.TokenRefreshCommand;
import dev.muho.user.service.AuthService;
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
        AuthLoginCommand command = AuthLoginCommand.from(request);
        AuthResult authResult = authService.login(command);
        return ResponseEntity.ok(AuthResponse.from(authResult));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        AuthLogoutCommand command = AuthLogoutCommand.from(request);
        authService.logout(command);
    }

    @PostMapping("/token/refresh")
    public AuthResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshCommand command = TokenRefreshCommand.from(request);
        AuthResult authResult = authService.refresh(command);
        return AuthResponse.from(authResult);
    }
}
