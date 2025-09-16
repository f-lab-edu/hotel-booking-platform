package dev.muho.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordHasherImpl implements PasswordHasher {

    private static final int TARGET_STRENGTH = 10; // BCrypt 기본 비용
    private final PasswordEncoder delegate; // BCryptPasswordEncoder Bean

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("rawPassword length must be >= 8");
        }
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isBlank()) return false; // 블랭크는 실패 처리
        if (encodedPassword == null || encodedPassword.isBlank()) return false;
        return delegate.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean needsRehash(String encodedPassword) {
        if (!(delegate instanceof BCryptPasswordEncoder) || encodedPassword == null) return false;
        // 포맷: $2a$10$... 앞의 비용(cost) 추출
        // 예: $2a$10$abcdefgh... → 세 번째 '$' 이후 두 자리 비용
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length < 3) return false;
            String costPart = parts[2]; // 예: 10
            int cost = Integer.parseInt(costPart.substring(0, 2));
            return cost < TARGET_STRENGTH; // 정책 비용보다 낮으면 재해시 필요
        } catch (Exception e) {
            return false;
        }
    }
}
