package dev.muho.user.domain.user.service;

import dev.muho.user.dto.command.UserCreateCommand;
import dev.muho.user.dto.command.UserInfoResult;
import dev.muho.user.dto.command.UserPasswordChangeCommand;
import dev.muho.user.dto.command.UserProfileUpdateCommand;
import dev.muho.user.dto.command.UserSearchCondition;
import dev.muho.user.entity.User;
import dev.muho.user.entity.UserRole;
import dev.muho.user.entity.UserStatus;
import dev.muho.user.error.InvalidUserInputException;
import dev.muho.user.error.UserAlreadyExistsException;
import dev.muho.user.error.UserNotFoundException;
import dev.muho.user.repository.UserRepository;
import dev.muho.user.security.PasswordHasher;
import dev.muho.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordHasher passwordHasher;

    @InjectMocks
    UserServiceImpl userService;

    private static final String RAW_PASSWORD = "PlainPass123!";
    private static final String ENCODED_PASSWORD = "ENCODED_PASSWORD_HASH_VALUE_EXCEEDING_30_CHARS_123"; // >30 chars

    @BeforeEach
    void setup() {
        // no-op
    }

    private User newUser(Long id, String email) {
        User user = User.createNewUser(email, ENCODED_PASSWORD, "홍길동", "010-1234-5678");
        setId(user, id);
        return user;
    }

    private void setId(User user, Long id) {
        try {
            Field f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("create: 정상 생성")
    void create_success() {
        UserCreateCommand cmd = new UserCreateCommand("test@example.com", RAW_PASSWORD, "홍길동", "010-1234-5678");
        given(userRepository.existsByEmail(cmd.email())).willReturn(false);
        given(passwordHasher.hash(cmd.rawPassword())).willReturn(ENCODED_PASSWORD);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            setId(u, 10L);
            return u;
        });

        UserInfoResult result = userService.create(cmd);

        verify(userRepository).existsByEmail(cmd.email());
        verify(passwordHasher).hash(cmd.rawPassword());
        verify(userRepository).save(captor.capture());
        assertThat(result.id()).isEqualTo(10L);
        assertThat(captor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(captor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("create: 이메일 중복")
    void create_duplicateEmail() {
        UserCreateCommand cmd = new UserCreateCommand("dup@example.com", RAW_PASSWORD, "홍길동", "010-1234-5678");
        given(userRepository.existsByEmail(cmd.email())).willReturn(true);
        assertThatThrownBy(() -> userService.create(cmd)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById: 성공")
    void findById_success() {
        User user = newUser(1L, "find@example.com");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        UserInfoResult result = userService.findById(1L);
        assertThat(result.email()).isEqualTo("find@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("findById: 미존재")
    void findById_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(99L)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfile: 이름/전화 변경")
    void updateProfile() {
        User user = newUser(2L, "profile@example.com");
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        UserProfileUpdateCommand cmd = new UserProfileUpdateCommand("새이름", "010-9999-8888");
        UserInfoResult result = userService.updateProfile(2L, cmd);
        assertThat(result.name()).isEqualTo("새이름");
        assertThat(user.getName()).isEqualTo("새이름");
        assertThat(user.getPhone()).isEqualTo("010-9999-8888");
    }

    @Nested
    class ChangePassword {
        @Test
        @DisplayName("changePassword: 성공")
        void changePassword_success() {
            User user = newUser(3L, "pwd@example.com");
            given(userRepository.findById(3L)).willReturn(Optional.of(user));
            given(passwordHasher.matches("old", user.getPassword())).willReturn(true);
            given(passwordHasher.hash("newPass!234"))
                    .willReturn("NEW_ENCODED_PASSWORD_VALUE_LONG_ENOUGH_1234567890");
            userService.changePassword(3L, new UserPasswordChangeCommand("old", "newPass!234"));
            assertThat(user.getPassword()).startsWith("NEW_ENCODED");
        }

        @Test
        @DisplayName("changePassword: 현재 비밀번호 불일치")
        void changePassword_mismatch() {
            User user = newUser(4L, "pwd2@example.com");
            given(userRepository.findById(4L)).willReturn(Optional.of(user));
            given(passwordHasher.matches("wrong", user.getPassword())).willReturn(false);
            assertThatThrownBy(() -> userService.changePassword(4L, new UserPasswordChangeCommand("wrong", "new")))
                    .isInstanceOf(InvalidUserInputException.class);
        }
    }

    @Test
    @DisplayName("changeRole: ADMIN 으로 변경")
    void changeRole() {
        User user = newUser(5L, "role@example.com");
        given(userRepository.findById(5L)).willReturn(Optional.of(user));
        UserInfoResult result = userService.changeRole(5L, UserRole.ADMIN);
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("withdraw: 상태 변경 및 idempotent")
    void withdraw() {
        User user = newUser(6L, "withdraw@example.com");
        given(userRepository.findById(6L)).willReturn(Optional.of(user));
        userService.withdraw(6L);
        LocalDateTime firstWithdrawTime = user.getWithdrawnAt();
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        // second call
        userService.withdraw(6L);
        assertThat(user.getWithdrawnAt()).isEqualTo(firstWithdrawTime);
    }

    @Test
    @DisplayName("existsByEmail: 호출 위임")
    void existsByEmail() {
        given(userRepository.existsByEmail("exist@example.com")).willReturn(true);
        assertThat(userService.existsByEmail("exist@example.com")).isTrue();
        verify(userRepository).existsByEmail("exist@example.com");
    }

    @Test
    @DisplayName("search: 결과 매핑")
    void search() {
        User u1 = newUser(11L, "a@example.com");
        User u2 = newUser(12L, "b@example.com");
        Pageable pageable = PageRequest.of(0, 10);
        given(userRepository.search(any(UserSearchCondition.class), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(u1, u2), pageable, 2));
        Page<UserInfoResult> page = userService.search(new UserSearchCondition(null, null, ""), pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(UserInfoResult::email)
                .containsExactlyInAnyOrder("a@example.com", "b@example.com");
    }
}
