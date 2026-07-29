package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "read_statuses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Column(nullable = false)
  private Instant lastReadAt;

  @Column(nullable = false)
  private boolean notificationEnabled;

  public ReadStatus(User user, Channel channel) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = Instant.now();
    this.notificationEnabled = (channel.getType() == ChannelType.PRIVATE);
  }

  public void updateLastReadAt(Instant newLastReadAt) {
    if (newLastReadAt.isAfter(lastReadAt)) {
      this.lastReadAt = newLastReadAt;
    }
  }

  public void updateNotificationEnabled(Boolean newNotificationEnabled) {
    if (newNotificationEnabled != null) {
      this.notificationEnabled = newNotificationEnabled;
    }
  }

  public String toString() {
    return "user: " + user
        + ", channel: " + channel
        + ", lastReadAt: " + lastReadAt;
  }
}

