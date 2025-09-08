package dev.muho.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.FakeDBConfiguration;
import dev.muho.user.domain.auth.AuthController;
import dev.muho.user.domain.auth.FakeAuthRepository;
import dev.muho.user.domain.auth.dto.LoginRequest;
import dev.muho.user.domain.auth.dto.PasswordChangeRequest;
import dev.muho.user.domain.auth.dto.TokenRefreshRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(FakeDBConfiguration.class)
public class AuthE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeAuthRepository authRepository;

    @BeforeEach
    void setUp() {
        authRepository.clear();
    }

    @Test
    void 로그인_성공() throws Exception {
        // given
        String email = "test@test.com";
        String password = "password";
        authRepository.save(email, password);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
        String requestJson = objectMapper.writeValueAsString(loginRequest);

        // when & then
        // 로그인 API 호출 및 응답 검증 로직 작성
        mockMvc.perform(post("/v1/auth/login")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void 로그인_실패_잘못된_비밀번호() throws Exception {
        // given
        String email = "test@test.com";
        String password = "password";
        authRepository.save(email, password);

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("wrongpassword")
                .build();
        String requestJson = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/v1/auth/login")
                .contentType("application/json")
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 토큰_갱신_성공() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("refreshtoken")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void 패스워드_변경_성공() throws Exception {
        String email = "test@test.com";
        String password = "password";
        authRepository.save(email, password);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword(password)
                .newPassword("newpassword")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/v1/auth/me/password")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void 패스워드_변경_실패_잘못된_현재_패스워드() throws Exception {
        String email = "test@test.com";
        String password = "password";
        authRepository.save(email, password);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/v1/auth/me/password")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 패스워드_변경_실패_잘못된_새_패스워드() throws Exception {
        String email = "test@test.com";
        String password = "password";
        authRepository.save(email, password);

        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword(password)
                .newPassword("short")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/v1/auth/me/password")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.newPassword").value("새 비밀번호는 최소 8자 이상이어야 합니다."));
    }
}
