package dev.muho.user.controller;

import dev.muho.user.dto.api.EmailExistResponse;
import dev.muho.user.dto.api.UserPasswordChangeRequest;
import dev.muho.user.dto.api.UserResponse;
import dev.muho.user.dto.api.UserRoleUpdateRequest;
import dev.muho.user.dto.api.UserSignupRequest;
import dev.muho.user.dto.api.UserProfileUpdateRequest;
import dev.muho.user.dto.command.UserCreateCommand;
import dev.muho.user.dto.command.UserPasswordChangeCommand;
import dev.muho.user.dto.command.UserProfileUpdateCommand;
import dev.muho.user.dto.command.UserSearchCondition;
import dev.muho.user.entity.UserRole;
import dev.muho.user.entity.UserStatus;
import dev.muho.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 검색 (role/status/keyword 조건)
    @GetMapping
    public Page<UserResponse> users(@RequestParam(required = false) UserRole role,
                                    @RequestParam(required = false) UserStatus status,
                                    @RequestParam(required = false) String keyword,
                                    Pageable pageable) {
        UserSearchCondition condition = new UserSearchCondition(role, status, keyword);
        return userService.search(condition, pageable).map(UserResponse::from);
    }

    // 단건 조회
    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        return UserResponse.from(userService.findById(userId));
    }

    // 내 정보 조회 (임시: 고정 ID 사용)
    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.findById(currentUserId()));
    }

    // 회원 가입
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserSignupRequest request) {
        UserCreateCommand command = UserCreateCommand.from(request);
        return UserResponse.from(userService.create(command));
    }

    // 내 프로필 수정
    @PatchMapping("/me")
    public UserResponse update(@Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileUpdateCommand command = UserProfileUpdateCommand.from(request);
        return UserResponse.from(userService.updateProfile(currentUserId(), command));
    }

    // 내 비밀번호 변경
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody UserPasswordChangeRequest request) {
        UserPasswordChangeCommand command = UserPasswordChangeCommand.from(request);
        userService.changePassword(currentUserId(), command);
    }

    // 역할 변경 (관리자 용도 가정)
    @PatchMapping("/{userId}")
    public UserResponse updateRole(@PathVariable Long userId, @Valid @RequestBody UserRoleUpdateRequest request) {
        return UserResponse.from(userService.changeRole(userId, request.getRole()));
    }

    // 내 탈퇴
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.withdraw(currentUserId());
    }

    // 특정 사용자 탈퇴 (관리자)
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long userId) {
        userService.withdraw(userId);
    }

    // 이메일 중복 체크
    @GetMapping("/exists")
    public EmailExistResponse exists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return EmailExistResponse.of(email, exists);
    }

    // TODO: 실제 인증 연동하여 현재 사용자 ID 추출하도록 수정 필요
    private Long currentUserId() {
        return 1L; // placeholder
    }
}
