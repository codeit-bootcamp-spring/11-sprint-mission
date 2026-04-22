package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor
public class UserStatus extends BaseUpdatableEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Instant lastActiveAt;

  public UserStatus(User user) {
    this.user = user;
    this.lastActiveAt = Instant.now();
  }

  public void update(Instant newLastActiveAt) {
    this.lastActiveAt = (newLastActiveAt != null) ? newLastActiveAt : Instant.now();
  }

  @JsonIgnore
  public boolean getOnlineStatus() {
    if (lastActiveAt == null) {
      return false;
    }

    Instant now = Instant.now();
    Instant fiveMinuteAgo = now.minusSeconds(5 * 60);

    return lastActiveAt.isAfter(fiveMinuteAgo);
  }
}
