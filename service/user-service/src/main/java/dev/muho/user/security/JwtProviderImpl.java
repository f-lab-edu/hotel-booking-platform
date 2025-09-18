package dev.muho.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
public class JwtProviderImpl implements JwtProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private final SecretKey secretKey;
    private final long accessTokenValidityInMs;
    private final long refreshTokenValidityInMs;

    public JwtProviderImpl(
            @Value("${jwt.secret-key}") String secretString,
            @Value("${jwt.access-token-validity-in-ms}") long accessTokenValidityInMs,
            @Value("${jwt.refresh-token-validity-in-ms}") long refreshTokenValidityInMs
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMs = accessTokenValidityInMs;
        this.refreshTokenValidityInMs = refreshTokenValidityInMs;
    }

    /**
     * 인증(Authentication) 객체를 받아 Access Token을 생성하는 메서드
     */
    @Override
    public String createAccessToken(dev.muho.user.entity.User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + this.accessTokenValidityInMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims()
                    .add("role", user.getRole().name())
                    .add("email", user.getEmail())
                    .and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 인증(Authentication) 객체를 받아 Refresh Token을 생성하는 메서드
     */
    @Override
    public String createRefreshToken(dev.muho.user.entity.User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + this.refreshTokenValidityInMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰을 받아 인증(Authentication) 객체를 반환하는 메서드
     */
    @Override
    public Authentication getAuthentication(String accessToken) {
        // 1. 토큰을 파싱하여 Claims 추출
        Claims claims = parseClaims(accessToken);

        // 2. Claims에서 권한 정보를 직접 추출
        //    권한이 단일 역할이라고 가정, 여러 역할인 경우 로직 수정 필요
        String role = claims.get("role", String.class);
        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

        // 3. UserDetails를 구현한 UserPrincipal 객체를 생성
        //    Subject에서 userId를, Claim에서 email을 가져옵니다.
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserPrincipal principal = new UserPrincipal(userId, email, authorities);

        // 4. Authentication 객체를 생성하여 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * JWT 토큰의 유효성을 검증하는 메서드
     */
    @Override
    public boolean validateToken(String accessToken) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 Claims 정보를 추출하는 private 헬퍼 메서드
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었더라도, Claims 정보는 반환 (예: 만료된 토큰의 사용자 정보를 확인해야 할 경우)
            return e.getClaims();
        }
    }
}
