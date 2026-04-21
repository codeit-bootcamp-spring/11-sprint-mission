package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  public UserStatus(User user) {
    this.user = user;
    this.lastActiveAt = Instant.now();
  }

  public boolean isOnline() {
    Instant targetTime = this.getUpdatedAt() != null ? this.getUpdatedAt() : this.lastActiveAt;

    if (targetTime == null) {
      return false;
    }

    return targetTime.isAfter(Instant.now().minus(5, ChronoUnit.MINUTES));
  }

  public void update(Instant newLastActiveAt) {
    this.lastActiveAt = newLastActiveAt != null ? newLastActiveAt : Instant.now();
  }

  void setUser(User user) {
    this.user = user;
  }
}
