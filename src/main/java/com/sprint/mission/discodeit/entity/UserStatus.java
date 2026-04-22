package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_statuses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends MutableBaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    public UserStatus(User user, Instant lastActiveAt) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.lastActiveAt = lastActiveAt;
    }

    public void update(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public boolean isOnline() {
        return lastActiveAt != null &&
                lastActiveAt.isAfter(Instant.now().minus(Duration.ofMinutes(5)));
    }
}