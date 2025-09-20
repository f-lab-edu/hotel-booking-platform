package dev.muho.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.user.dto.api.LoginRequest;
import dev.muho.user.dto.api.LogoutRequest;
import dev.muho.user.dto.api.TokenRefreshRequest;
import dev.muho.user.entity.User;
import dev.muho.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
public class AuthControllerIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL 설정
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        // Redis 설정
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트용 사용자 생성
        User testUser = User.createNewUser(
                testEmail,
                passwordEncoder.encode(testPassword),
                "테스트 사용자",
                "010-1234-5678"
        );
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("유효한 자격 증명으로 로그인하면 토큰을 반환한다")
    void login_WithValidCredentials_ReturnsTokens() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("잘못된 이메일로 로그인하면 401을 반환한다")
    void login_WithInvalidEmail_ReturnsUnauthorized() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong@example.com")
                .password(testPassword)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401을 반환한다")
    void login_WithInvalidPassword_ReturnsUnauthorized() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password("wrongpassword")
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식으로 로그인하면 400을 반환한다")
    void login_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid-email")
                .password(testPassword)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 너무 짧으면 400을 반환한다")
    void login_WithShortPassword_ReturnsBadRequest() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password("short")
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 로그아웃하면 204를 반환한다")
    void logout_WithValidRefreshToken_ReturnsNoContent() throws Exception {
        // given
        // 먼저 로그인해서 리프레시 토큰을 얻는다
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        String loginResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        LogoutRequest logoutRequest = LogoutRequest.builder()
                .refreshToken(refreshToken)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("null 리프레시 토큰으로 로그아웃하면 401을 반환한다")
    void logout_WithNullRefreshToken_ReturnsBadRequest() throws Exception {
        // given
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .refreshToken(null)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 토큰 갱신하면 새 토큰을 반환한다")
    void refresh_WithValidRefreshToken_ReturnsNewTokens() throws Exception {
        // given
        // 먼저 로그인해서 리프레시 토큰을 얻는다
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password(testPassword)
                .build();

        String loginResponse = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();
        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        TokenRefreshRequest tokenRefreshRequest = TokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("null 리프레시 토큰으로 토큰 갱신하면 401을 반환한다")
    void refresh_WithNullRefreshToken_ReturnsBadRequest() throws Exception {
        // given
        TokenRefreshRequest tokenRefreshRequest = TokenRefreshRequest.builder()
                .refreshToken(null)
                .build();

        // when & then
        mockMvc.perform(post("/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("빈 요청 바디로 로그인하면 400을 반환한다")
    void login_WithEmptyBody_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 요청 바디로 로그아웃하면 401을 반환한다")
    void logout_WithEmptyBody_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("빈 요청 바디로 토큰 갱신하면 401을 반환한다")
    void refresh_WithEmptyBody_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/v1/auth/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
