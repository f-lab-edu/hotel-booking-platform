package dev.muho.user.domain.user.service;

import dev.muho.user.domain.user.dto.command.*;
import dev.muho.user.domain.user.entity.User;
import dev.muho.user.domain.user.entity.UserRole;
import dev.muho.user.domain.user.error.InvalidUserInputException;
import dev.muho.user.domain.user.error.UserAlreadyExistsException;
import dev.muho.user.domain.user.error.UserNotFoundException;
import dev.muho.user.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    @Transactional
    public UserInfoResult create(UserCreateCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException();
        }
        String encoded = passwordHasher.hash(command.rawPassword());
        User user = User.createNewUser(command.email(), encoded, command.name(), command.phone());
        userRepository.save(user);
        return UserInfoResult.from(user);
    }

    @Override
    public UserInfoResult findById(Long id) {
        return UserInfoResult.from(getUserOrThrow(id));
    }

    @Override
    @Transactional
    public UserInfoResult updateProfile(Long id, UserProfileUpdateCommand command) {
        User user = getUserOrThrow(id);
        user.updateUserProfile(command.name(), command.phone());
        return UserInfoResult.from(user);
    }

    @Override
    @Transactional
    public void changePassword(Long id, UserPasswordChangeCommand command) {
        User user = getUserOrThrow(id);
        if (!passwordHasher.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidUserInputException();
        }
        String encoded = passwordHasher.hash(command.newPassword());
        user.updatePassword(encoded);
    }

    @Override
    @Transactional
    public UserInfoResult changeRole(Long id, UserRole role) {
        User user = getUserOrThrow(id);
        user.changeRole(role);
        return UserInfoResult.from(user);
    }

    @Override
    @Transactional
    public void withdraw(Long id) {
        User user = getUserOrThrow(id);
        user.withdraw();
    }

    @Override
    public Page<UserInfoResult> search(UserSearchCondition condition, Pageable pageable) {
        return userRepository.search(condition, pageable).map(UserInfoResult::from);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
