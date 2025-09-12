package dev.muho.user.domain.user.service;

import dev.muho.user.domain.user.entity.UserRole;
import dev.muho.user.domain.user.dto.command.*;
import dev.muho.user.domain.user.error.AccessDeniedException;
import dev.muho.user.domain.user.error.UserAlreadyExistsException;
import dev.muho.user.domain.user.error.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * 사용자 생성
     * @param command 생성 요청(원문 비밀번호 포함) - 내부에서 암호화 처리
     * @return 생성된 사용자 정보
     * @throws UserAlreadyExistsException 이미 이메일 존재
     */
    UserInfoResult create(UserCreateCommand command);

    /**
     * ID 로 단건 조회
     * @throws UserNotFoundException 미존재
     */
    UserInfoResult findById(Long id);

    /**
     * 프로필(이름/전화번호) 수정
     */
    UserInfoResult updateProfile(Long id, UserProfileUpdateCommand command);

    /**
     * 비밀번호 변경 (현재 비밀번호 검증 후 인코딩 저장)
     */
    void changePassword(Long id, UserPasswordChangeCommand command);

    /**
     * 역할 변경 (관리자 권한 필요)
     * @throws AccessDeniedException 권한 없음
     */
    UserInfoResult changeRole(Long id, UserRole role);

    /**
     * 탈퇴(소프트 삭제) 처리
     */
    void withdraw(Long id);

    /** 조건 검색 (role/status/keyword) */
    Page<UserInfoResult> search(UserSearchCondition condition, Pageable pageable);

    /** 이메일 존재 여부 */
    boolean existsByEmail(String email);
}
