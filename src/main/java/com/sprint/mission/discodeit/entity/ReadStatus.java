package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "read_status")
@Entity
@NoArgsConstructor
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private Channel channel;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(nullable = false)
  private Instant lastReadAt;

  private boolean notificationEnabled;


  public ReadStatus(User user, Channel channel, Instant lastReadAt, boolean notificationEnabled) {
    this.channel = channel;
    this.user = user;
    this.lastReadAt = lastReadAt;
    this.notificationEnabled = notificationEnabled;

  }

  public Instant updateLastReadAt(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
    return lastReadAt;
  }

  public void updateNotificationEnabled(boolean notificationEnabled) {
    this.notificationEnabled = notificationEnabled;
  }


}
