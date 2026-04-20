package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
public abstract class BaseEntity {

  @Id
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private Instant createdAt;

  protected BaseEntity() {
    // @GeneratedValue 대신 생성자에서 직접 할당하여
    // 트랜잭션 도중 ID가 null인 시점이 없도록 합니다.
    this.id = UUID.randomUUID();
  }
}