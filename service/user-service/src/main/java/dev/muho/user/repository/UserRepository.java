package dev.muho.user.repository;

import dev.muho.user.dto.command.UserSearchCondition;
import dev.muho.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 커스텀 검색 쿼리
    @Query("SELECT u FROM User u WHERE " +
            "(:#{#condition.keyword} IS NULL OR (u.name LIKE %:#{#condition.keyword}% or u.email LIKE %:#{#condition.keyword}%)) " +
           "AND (:#{#condition.role} IS NULL OR u.role = :#{#condition.role}) " +
           "AND (:#{#condition.status} IS NULL OR u.status = :#{#condition.status})")
    Page<User> search(@Param("condition") UserSearchCondition condition, Pageable pageable);
}
