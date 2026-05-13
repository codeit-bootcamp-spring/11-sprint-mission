package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "user_status")
@Entity
@NoArgsConstructor
public class UserStatus extends BaseUpdatableEntity {

  @OneToOne
  private User user;

  @Column(nullable = false)
  private Instant lastActiveAt;

  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  public void updateLastActiveAt(Instant lastActiveAt) {
    this.lastActiveAt = lastActiveAt;
  }

  public boolean isOnline() {

    Instant now = Instant.now();

    if (getLastActiveAt().isAfter(now.minusSeconds(300))) {

      return true;
    } else {
      return false;
    }


  }


}
