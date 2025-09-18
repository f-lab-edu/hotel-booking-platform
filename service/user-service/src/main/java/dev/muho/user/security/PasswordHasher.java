package dev.muho.user.security;

/**
 * 비밀번호 해싱 / 검증 포트.
 * User 도메인은 인코딩 알고리즘을 몰라도 되고,
 * 인코딩된 문자열만 다루도록 하기 위한 추상화.
 */
public interface PasswordHasher {

    /**
     * 원문 비밀번호를 해시(암호화)하여 저장용 문자열을 반환.
     * @param rawPassword 원문 비밀번호 (null/blank 불가)
     * @return 해싱된 비밀번호
     * @throws IllegalArgumentException 입력이 유효하지 않을 때
     */
    String hash(String rawPassword);

    /**
     * 원문 비밀번호가 인코딩된 비밀번호와 일치하는지 확인.
     * @param rawPassword 원문
     * @param encodedPassword 저장된 해시 값
     * @return 일치 여부
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * (선택적) 해시 재계산 필요 여부 (알고리즘 강도 변경 등)
     * 기본 구현은 false. 구현체에서 필요 시 override.
     */
    default boolean needsRehash(String encodedPassword) { return false; }
}

