package dev.muho.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // 필터의 핵심 로직
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);
        log.info("Extracted Token: {}", token);

        // 2. 토큰 유효성 검사
        // 토큰이 존재하고, 유효성 검사를 통과하면 SecurityContext에 인증 정보 저장
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // 토큰으로부터 인증(Authentication) 객체를 받아옴
            Authentication authentication = jwtProvider.getAuthentication(token);
            // SecurityContextHolder에 인증 객체를 저장
            // 이렇게 저장하면, 해당 요청을 처리하는 동안 인증된 사용자로 간주됨
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authentication successful for user: {}", authentication.getName());
        } else {
            log.warn("No valid JWT token found in request.");
        }

        // 3. 다음 필터로 제어 전달
        // 이 필터에서 인증 처리가 끝나면, 다음 필터로 요청과 응답을 넘겨줌
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰을 추출하는 헬퍼 메서드.
     * @param request The HTTP request.
     * @return Extracted JWT token string or null if not found.
     */
    private String resolveToken(HttpServletRequest request) {
        // "Authorization" 헤더 값을 가져옴
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 헤더가 존재하고 "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사를 제거하고 실제 토큰 값만 반환
            return bearerToken.substring(7);
        }

        return null;
    }
}
