package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "read_statuses")
@NoArgsConstructor
public class ReadStatus extends BaseUpdatableEntity {

  // 연관관계 필드
  // user_id uuid not null references users (id) on delete cascade
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // channel_id uuid not null references channels (id) on delete cascade
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  // last_read_at timestamptz,
  @Column(name = "last_read_at")
  private Instant lastReadAt;

  // notification_enabled boolean not null
  @Column(name = "notification_enabled", nullable = false)
  private boolean notificationEnabled;

//    private final UUID messageId; // 특정 메시지를 읽은 시간이 아닌 채널 별 마지막으로 읽은 시간이기 때문에 X

  // 생성자
  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;

    // 채널 타입이 PRIVATE일 경우 true, PUBLIC일 경우 false
    this.notificationEnabled = channel.getType() == ChannelType.PRIVATE;
  }

  // update
  // 매개변수로는 요청 DTO로 설정됨
  public void update(Instant lastReadAt, Boolean notificationEnabled) {
    if (lastReadAt != null) {
      this.lastReadAt = lastReadAt;
    }

    if (notificationEnabled != null) {
      this.notificationEnabled = notificationEnabled;
    }
  }
}
