package dev.muho.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.user.domain.user.FakeUserRepository;
import dev.muho.user.domain.user.UserController;
import dev.muho.user.domain.user.UserRole;
import dev.muho.user.domain.user.dto.UserRoleUpdateRequest;
import dev.muho.user.domain.user.dto.UserSignupRequest;
import dev.muho.user.domain.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(FakeDBConfiguration.class)
public class UserE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.clear();
    }

    @Test
    void 유저_목록_조회_성공() throws Exception {
        UserSignupRequest request1 = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        UserSignupRequest request2 = UserSignupRequest.builder()
                .email("test2@test.com")
                .name("Test User2")
                .password("password2")
                .phone("010-1234-5679")
                .build();
        userRepository.save(request1);
        userRepository.save(request2);

        mockMvc.perform(get("/v1/users"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value(request1.getEmail()))
                .andExpect(jsonPath("$.content[1].email").value(request2.getEmail()));
    }

    @Test
    void 유저_조회_성공() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        userRepository.save(request);

        mockMvc.perform(get("/v1/users/me"))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.phone").value(request.getPhone()));
    }

    @Test
    void 유저_등록_성공() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.phone").value(request.getPhone()));
    }

    @Test
    void 유저_등록_실패_잘못된_입력값() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("invalid-email")
                .name("") // 이름이 비어있음
                .password("short") // 비밀번호가 너무 짧음
                .phone("invalid-phone") // 잘못된 전화번호 형식
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/users")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유저_정보_수정_성공() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        userRepository.save(request);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Updated User")
                .phone("010-8765-4321")
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(patch("/v1/users/me")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.phone").value(updateRequest.getPhone()));
    }

    @Test
    void 유저_역할_변경_성공() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        userRepository.save(request);

        UserRoleUpdateRequest roleUpdateRequest = UserRoleUpdateRequest.builder()
                .role(UserRole.ADMIN)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(roleUpdateRequest);

        mockMvc.perform(patch("/v1/users/1")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(roleUpdateRequest.getRole().toString()));
    }

    @Test
    void 유저_정보_수정_실패_잘못된_입력값() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        userRepository.save(request);

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("") // 이름이 비어있음
                .phone("invalid-phone") // 잘못된 전화번호 형식
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(patch("/v1/users/me")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유저_삭제_성공() throws Exception {
        UserSignupRequest request = UserSignupRequest.builder()
                .email("test@test.com")
                .name("Test User")
                .password("password")
                .phone("010-1234-5678")
                .build();
        userRepository.save(request);

        mockMvc.perform(delete("/v1/users/me"))
                .andExpect(status().isNoContent());
    }
}
