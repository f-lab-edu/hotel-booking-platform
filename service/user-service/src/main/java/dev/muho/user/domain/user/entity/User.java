package dev.muho.user.domain.user.entity;

import dev.muho.user.common.entity.BaseTimeEntity;
import dev.muho.user.domain.user.error.InvalidUserInputException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password; // 인코딩된 비밀번호

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 13)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column
    private LocalDateTime withdrawnAt; // 회원 탈퇴 일시

    @Builder
    private User(String email, String encodedPassword, String name, String phone) {
        validateEmail(email);
        validateEncodedPassword(encodedPassword);
        validateName(name);
        validatePhone(phone);
        this.email = email;
        this.password = encodedPassword;
        this.name = name;
        this.phone = phone;
        this.role = UserRole.CUSTOMER; // 생성 시 기본 역할은 '고객'
        this.status = UserStatus.ACTIVE; // 생성 시 기본 상태는 '활성'
    }

    public static User createNewUser(String email, String encodedPassword, String name, String phone) {
        return User.builder()
                .email(email)
                .encodedPassword(encodedPassword)
                .name(name)
                .phone(phone)
                .build();
    }

    //== 비즈니스 로직 ==//

    /**
     * 프로필 정보 수정
     */
    public void updateUserProfile(String name, String phone) {
        validateName(name);
        validatePhone(phone);
        this.name = name;
        this.phone = phone;
    }

    /**
     * 비밀번호 변경
     */
    public void updatePassword(String encodedPassword) {
        validateEncodedPassword(encodedPassword);
        this.password = encodedPassword;
    }

    /**
     * 역할 변경 (관리자용)
     */
    public void changeRole(UserRole newRole) {
        if (newRole == null) throw new InvalidUserInputException("역할은 null 일 수 없습니다.");
        if (this.role == newRole) return;
        this.role = newRole; // 버그 수정: 이전에는 this.role = role;
    }

    /**
     * 회원 탈퇴 처리
     */
    public void withdraw() {
        if (this.status == UserStatus.WITHDRAWN) return;
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    /**
     * 회원 활성화 상태 조회
     */
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    // 내부 검증 메서드들
    private static void validateEmail(String email) {
        if (email == null) throw new InvalidUserInputException("이메일은 null 일 수 없습니다.");
        if (email.trim().isEmpty()) throw new InvalidUserInputException("이메일은 공백일 수 없습니다.");
        if (email.length() > 100) throw new InvalidUserInputException("이메일 길이 초과(최대 100). 현재: " + email.length());
        // 형식 검증이 필요하면 정규식 추가 가능
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null) throw new InvalidUserInputException("비밀번호는 null 일 수 없습니다.");
        if (encodedPassword.trim().isEmpty()) throw new InvalidUserInputException("비밀번호는 공백일 수 없습니다.");
        if (encodedPassword.length() < 30) throw new InvalidUserInputException("인코딩된 비밀번호 길이 최소 30 필요. 현재: " + encodedPassword.length());
    }

    private static void validateName(String name) {
        if (name == null) throw new InvalidUserInputException("이름은 null 일 수 없습니다.");
        if (name.trim().isEmpty()) throw new InvalidUserInputException("이름은 공백일 수 없습니다.");
        if (name.length() > 50) throw new InvalidUserInputException("이름 길이 초과(최대 50). 현재: " + name.length());
    }

    private static void validatePhone(String phone) {
        if (phone == null) throw new InvalidUserInputException("전화번호는 null 일 수 없습니다.");
        if (phone.trim().isEmpty()) throw new InvalidUserInputException("전화번호는 공백일 수 없습니다.");
        if (phone.length() > 13) throw new InvalidUserInputException("전화번호 길이 초과(최대 13). 현재: " + phone.length());
        if (!PHONE_PATTERN.matcher(phone).matches()) throw new InvalidUserInputException("전화번호 형식이 올바르지 않습니다.");
    }
}
