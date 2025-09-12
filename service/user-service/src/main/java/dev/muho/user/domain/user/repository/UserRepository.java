package dev.muho.user.domain.user.repository;

import dev.muho.user.domain.user.dto.command.UserSearchCondition;
import dev.muho.user.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findById(Long id);

    Page<User> search(UserSearchCondition condition, Pageable pageable);

    User save(User user);

    void deleteById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
