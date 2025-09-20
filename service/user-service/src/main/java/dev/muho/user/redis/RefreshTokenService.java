package dev.muho.user.redis;

public interface RefreshTokenService {
    // 리프레시 토큰 저장
    void saveRefreshToken(Long userId, String tokenValue);

    // 리프레시 토큰으로 유저 ID 찾기
    Long findUserIdByToken(String tokenValue);

    // 리프레시 토큰 삭제
    void deleteRefreshToken(String tokenValue);

    // 리프레시 토큰 갱신 (rotate)
    String rotateRefreshToken(Long userId, String oldToken, String newToken);
}
