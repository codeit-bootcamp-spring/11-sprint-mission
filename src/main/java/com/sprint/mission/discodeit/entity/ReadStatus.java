package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "read_statuses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "channel_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @Column(name = "last_read_at", nullable = false)
  private Instant lastReadAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", referencedColumnName = "id", nullable = false)
  private Channel channel;

  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = (lastReadAt != null) ? lastReadAt : Instant.now();
  }

  public void update(Instant newLastReadAt) {
    if (newLastReadAt != null) {
      this.lastReadAt = newLastReadAt;
    }
  }
}
