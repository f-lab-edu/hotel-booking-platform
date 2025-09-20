package dev.muho.user.redis;

import dev.muho.user.error.InvalidTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenValidityInMs;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-validity-in-ms}") long refreshTokenValidityInMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    public void saveRefreshToken(Long userId, String refreshTokenValue) {
        RefreshToken refreshToken = new RefreshToken(refreshTokenValue, userId, this.refreshTokenValidityInMs);
        refreshTokenRepository.save(refreshToken);
    }

    public Long findUserIdByToken(String refreshTokenValue) {
        return refreshTokenRepository.findById(refreshTokenValue) // ID(토큰값)로 조회
                .map(RefreshToken::getUserId) // 성공 시 userId 반환
                .orElseThrow(InvalidTokenException::new); // 실패 시 예외
    }

    public void deleteRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.deleteById(refreshTokenValue);
    }

    @Override
    public String rotateRefreshToken(Long userId, String oldToken, String newToken) {
        // 기존 토큰으로 userId 조회 (예외 발생 시 InvalidTokenException)
        Long userIdByToken = findUserIdByToken(oldToken);
        // 토큰 소유자 확인
        if (!userId.equals(userIdByToken)) {
            throw new InvalidTokenException();
        }
        // 기존 토큰 삭제
        deleteRefreshToken(oldToken);
        // 새 토큰 저장
        saveRefreshToken(userId, newToken);
        // 새 토큰 반환
        return newToken;
    }
}
