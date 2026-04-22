package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(
        name = "read_statuses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends MutableBaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    public ReadStatus(User user, Channel channel, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.channel = channel;
        this.lastReadAt = lastReadAt;
    }

    public void update(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
