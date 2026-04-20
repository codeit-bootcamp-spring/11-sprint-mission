package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass // 추상클래스가 jpa 상위 엔티티임을 명시
@EntityListeners(AuditingEntityListener.class)
// @CreatedDate가 설정되도록 JPA Auditing 활성화(메인 클래스에 @EnableJpaAuditing 필요)
public abstract class BaseEntity {

  @Id
//  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false) // Not Null
  private UUID id;

  @CreatedDate
  @Column(updatable = false, nullable = false) // Not Null
  private Instant createdAt;

  // protected : 같은 패키지거나 상속받은 클래스에서만
  protected BaseEntity() {
    this.id = UUID.randomUUID();
  }
}
