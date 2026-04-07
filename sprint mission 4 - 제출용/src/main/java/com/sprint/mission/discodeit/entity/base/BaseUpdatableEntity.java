package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseUpdatableEntity extends BaseEntity {

  @LastModifiedDate
  protected Instant updatedAt;
}
