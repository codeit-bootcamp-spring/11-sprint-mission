package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass // 필드를 자식 Entity가 컬럼으로 상속
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)  // 자동 관리 동작.
public abstract class BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", nullable = false)
  protected UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  protected Instant createdAt;
}
