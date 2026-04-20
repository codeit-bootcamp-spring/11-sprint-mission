package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // @LastModifiedDate 설정되도록 JPA Auditing 활성화
public class BaseUpdatableEntity extends BaseEntity {

  @LastModifiedDate
  @Column(nullable = false) // Not Null
  private Instant updatedAt;
}
