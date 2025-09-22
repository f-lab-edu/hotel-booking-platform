package dev.muho.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA 엔터티 클래스들이 해당 클래스를 상속할 경우 필드들도 칼럼으로 인식하게 합니다.
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 포함시킵니다.
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false) // 생성 시간은 수정 불가
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
