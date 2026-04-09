package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
public abstract class UpdatableEntity extends BaseEntity {

    @LastModifiedDate
    @Column
    private Instant updatedAt;
}