package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@NoArgsConstructor
@MappedSuperclass
public abstract class MutableBaseEntity extends BaseEntity{

    @LastModifiedDate
    @Column(nullable = false)
    protected Instant updatedAt;
}
