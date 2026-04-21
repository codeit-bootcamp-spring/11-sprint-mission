package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

    private static final Duration ONLINE_THRESHOLD = Duration.ofMinutes(5);

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private Instant lastActiveAt;

    public UserStatus(User user) {
        super();
        this.user = user;
        this.lastActiveAt = Instant.now();
    }


    public void updateLastActiveAt() {
        this.lastActiveAt = Instant.now();
    }

    public void updateLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public boolean isOnline() {
        Instant onlineCutoff = Instant.now().minus(ONLINE_THRESHOLD);
        return !lastActiveAt.isBefore(onlineCutoff);
    }
}
