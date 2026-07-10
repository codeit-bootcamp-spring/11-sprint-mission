package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.time.Instant;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "read_statuses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "channel_id"})
})
@NoArgsConstructor
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Instant lastReadAt;

  @Column(nullable = false)
  private boolean notificationEnabled;

  public ReadStatus(User user, Channel channel, Instant lastReadAt, boolean notificationEnabled) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
    this.notificationEnabled = notificationEnabled;
  }

  public void updateNotificationEnabled(boolean notificationEnabled) {
    this.notificationEnabled = notificationEnabled;
  }

  public void update(Instant lastReadAt) {
    if (lastReadAt == null) {
      throw new IllegalArgumentException("업데이트할 시각이 존재하지 않습니다");
    }

    this.lastReadAt = lastReadAt;
  }
}
