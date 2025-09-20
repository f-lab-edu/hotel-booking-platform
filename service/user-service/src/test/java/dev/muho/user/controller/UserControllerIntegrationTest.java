package dev.muho.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.user.dto.api.UserPasswordChangeRequest;
import dev.muho.user.dto.api.UserProfileUpdateRequest;
import dev.muho.user.dto.api.UserRoleUpdateRequest;
import dev.muho.user.dto.api.UserSignupRequest;
import dev.muho.user.entity.User;
import dev.muho.user.entity.UserRole;
import dev.muho.user.repository.UserRepository;
import dev.muho.user.security.JwtProvider;
import dev.muho.user.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379); // 컨테이너의 6379 포트를 외부에 노출

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
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 테스트용 사용자 생성
        testUser = User.createNewUser(
                "test@example.com",
                passwordHasher.hash("password123"),
                "Test User",
                "010-1234-5678"
        );
        userRepository.save(testUser);

        // JWT 토큰 생성
        authToken = jwtProvider.createAccessToken(testUser);
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    void getUsersSuccess() throws Exception {
        // Given
        User anotherUser = User.createNewUser(
                "another@example.com",
                passwordHasher.hash("password123"),
                "Another User",
                "010-9876-5432"
        );
        anotherUser.changeRole(UserRole.ADMIN);
        userRepository.save(anotherUser);

        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[*].email", hasItems("test@example.com", "another@example.com")));
    }

    @Test
    @DisplayName("역할별 사용자 검색 성공")
    void searchUsersByRoleSuccess() throws Exception {
        // Given
        User adminUser = User.createNewUser(
                "admin@example.com",
                passwordHasher.hash("password123"),
                "Admin User",
                "010-1111-2222"
        );
        adminUser.changeRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("role", "ADMIN")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].role", everyItem(is("ADMIN"))));
    }

    @Test
    @DisplayName("특정 사용자 조회 성공")
    void getUserByIdSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfoSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/me")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("Test User")));
    }

    @Test
    @DisplayName("회원 가입 성공")
    void createUserSuccess() throws Exception {
        // Given
        UserSignupRequest request = UserSignupRequest.builder()
                .email("newuser@example.com")
                .name("New User")
                .password("newpassword123")
                .phone("010-5555-6666")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.name", is("New User")))
                .andExpect(jsonPath("$.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("내 프로필 수정 성공")
    void updateMyProfileSuccess() throws Exception {
        // Given
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .name("Updated Name")
                .phone("010-9999-8888")
                .build();

        // When & Then
        mockMvc.perform(patch("/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.phone", is("010-9999-8888")));
    }

    @Test
    @DisplayName("내 비밀번호 변경 성공")
    void changeMyPasswordSuccess() throws Exception {
        // Given
        UserPasswordChangeRequest request = UserPasswordChangeRequest.builder()
                .currentPassword("password123")
                .newPassword("newpassword456")
                .build();

        // When & Then
        mockMvc.perform(patch("/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자 역할 변경 성공 (관리자)")
    void updateUserRoleSuccess() throws Exception {
        // Given
        User adminUser = User.createNewUser(
                "admin@example.com",
                passwordHasher.hash("adminpass"),
                "Admin User",
                "010-0000-0000"
        );
        adminUser.changeRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        String adminToken = jwtProvider.createAccessToken(adminUser);

        UserRoleUpdateRequest request = UserRoleUpdateRequest.builder()
                .role(UserRole.ADMIN)
                .build();

        // When & Then
        mockMvc.perform(patch("/v1/users/{userId}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("내 계정 탈퇴 성공")
    void deleteMyAccountSuccess() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/users/me")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("특정 사용자 탈퇴 성공 (관리자)")
    void deleteUserByIdSuccess() throws Exception {
        // Given
        User adminUser = User.createNewUser(
                "admin@example.com",
                passwordHasher.hash("adminpass"),
                "Admin User",
                "010-0000-0000"
        );
        adminUser.changeRole(UserRole.ADMIN);
        userRepository.save(adminUser);

        String adminToken = jwtProvider.createAccessToken(adminUser);

        User targetUser = User.createNewUser(
                "target@example.com",
                passwordHasher.hash("password"),
                "Target User",
                "010-7777-8888"
        );
        userRepository.save(targetUser);

        // When & Then
        mockMvc.perform(delete("/v1/users/{userId}", targetUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재하는 이메일")
    void checkEmailExistsTrue() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/exists")
                        .param("email", "test@example.com"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.exists", is(true)));
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재하지 않는 이메일")
    void checkEmailExistsFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/exists")
                        .param("email", "nonexistent@example.com"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("nonexistent@example.com")))
                .andExpect(jsonPath("$.exists", is(false)));
    }

    @Test
    @DisplayName("인증되지 않은 요청 실패")
    void unauthorizedRequestFail() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 토큰으로 요청 실패")
    void invalidTokenRequestFail() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bearer 접두사 없는 토큰으로 요청")
    void requestWithoutBearerPrefix() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users/me")
                        .header("Authorization", authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("빈 검색 키워드로 사용자 검색")
    void searchUsersWithEmptyKeyword() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("keyword", "")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("매우 긴 키워드로 사용자 검색")
    void searchUsersWithVeryLongKeyword() throws Exception {
        // Given
        String veryLongKeyword = "a".repeat(1000);

        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("keyword", veryLongKeyword)
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특수 문자가 포함된 키워드로 검색")
    void searchUsersWithSpecialCharacters() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("keyword", "!@#$%^&*()")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SQL 인젝션 시도하는 키워드로 검색")
    void searchUsersWithSqlInjectionAttempt() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("keyword", "'; DROP TABLE users; --")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("여러 조건을 동시에 사용한 사용자 검색")
    void searchUsersWithMultipleConditions() throws Exception {
        // Given
        User testUserForSearch = User.createNewUser(
                "searchtest@example.com",
                passwordHasher.hash("password123"),
                "Search Test User",
                "010-1111-3333"
        );
        userRepository.save(testUserForSearch);

        // When & Then
        mockMvc.perform(get("/v1/users")
                        .param("keyword", "Search")
                        .param("status", "ACTIVE")
                        .param("role", "CUSTOMER")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + authToken))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem("Search Test User")));
    }

    @Test
    @DisplayName("매우 약한 비밀번호로 회원가입 시도")
    void createUserWithWeakPassword() throws Exception {
        // Given
        UserSignupRequest request = UserSignupRequest.builder()
                .email("weakpass@example.com")
                .name("Weak Password User")
                .password("123")  // 매우 약한 비밀번호
                .phone("010-1234-5678")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("한글 이름으로 회원가입")
    void createUserWithKoreanName() throws Exception {
        // Given
        UserSignupRequest request = UserSignupRequest.builder()
                .email("korean@example.com")
                .name("김한국")
                .password("koreanpassword123")
                .phone("010-1234-5678")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("김한국")));
    }

    @Test
    @DisplayName("특수문자가 포함된 이름으로 회원가입")
    void createUserWithSpecialCharactersInName() throws Exception {
        // Given
        UserSignupRequest request = UserSignupRequest.builder()
                .email("special@example.com")
                .name("John O'Connor-Smith")
                .password("specialname123")
                .phone("010-1234-5678")
                .build();

        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John O'Connor-Smith")));
    }

    @Test
    @DisplayName("동시에 여러 필드 유효성 검사 실패")
    void createUserWithMultipleValidationErrors() throws Exception {
        // Given
        UserSignupRequest request = UserSignupRequest.builder()
                .email("")  // 빈 이메일
                .name("")   // 빈 이름
                .password("") // 빈 비밀번호
                .phone("") // 빈 전화번호
                .build();

        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 body로 회원가입 시도")
    void createUserWithEmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null body로 회원가입 시도")
    void createUserWithNullBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }
}
