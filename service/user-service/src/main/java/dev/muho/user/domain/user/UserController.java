package dev.muho.user.domain.user;

import dev.muho.user.domain.user.dto.UserResponse;
import dev.muho.user.domain.user.dto.UserRoleUpdateRequest;
import dev.muho.user.domain.user.dto.UserSignupRequest;
import dev.muho.user.domain.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final FakeUserRepository userRepository;

    @GetMapping
    public Page<UserResponse> users(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @GetMapping("/me")
    public UserResponse me() {
        // 내 아이디가 1이라 가정
        return userRepository.findById(1L);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserSignupRequest request) {
        return userRepository.save(request);
    }

    @PatchMapping("/me")
    public UserResponse update(@Valid @RequestBody UserUpdateRequest request) {
        // 내 아이디가 1이라 가정
        return userRepository.update(1L, request);
    }

    @PatchMapping("/{userId}")
    public UserResponse updateRole(@PathVariable Long userId, @Valid @RequestBody UserRoleUpdateRequest request) {
        return userRepository.updateRole(userId, request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        // 내 아이디가 1이라 가정
        userRepository.deleteById(1L);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long userId) {
        userRepository.deleteById(userId);
    }
}
