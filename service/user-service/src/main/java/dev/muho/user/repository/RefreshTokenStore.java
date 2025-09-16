package dev.muho.user.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenStore {

    /** 사용자 ID 와 refresh 토큰을 저장 (기존 토큰 교체 정책) */
    void store(Long userId, String refreshToken);

    /** refresh 토큰으로 사용자 ID 조회 */
    Optional<Long> getUserId(String refreshToken);

    /** 토큰 회전: 기존(old) -> 신규(new) */
    void rotate(Long userId, String oldRefresh, String newRefresh);

    /** 특정 refresh 토큰 폐기 */
    void revoke(String refreshToken);
}

