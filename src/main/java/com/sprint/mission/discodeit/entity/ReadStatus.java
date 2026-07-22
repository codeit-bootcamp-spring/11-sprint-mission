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
@Table(
    name = "read_statuses",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "channel_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", columnDefinition = "uuid")
  private User user;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "channel_id", columnDefinition = "uuid")
  private Channel channel;
  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant lastReadAt;
  @Column(nullable = false)
  private boolean notificationEnabled;

  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
    // 비공개 채널은 기본 알림 켜고 공개 채널은 기본 알림 끔
    this.notificationEnabled = channel.getType() == ChannelType.PRIVATE;
  }

  public void update(Instant newLastReadAt, Boolean newNotificationEnabled) {
    if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)) {
      this.lastReadAt = newLastReadAt;
    }

    // null이면 기존 알림 설정 유지함
    if (newNotificationEnabled != null
        && this.notificationEnabled != newNotificationEnabled) {
      this.notificationEnabled = newNotificationEnabled;
    }
  }
}