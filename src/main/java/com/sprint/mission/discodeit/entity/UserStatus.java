package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor
public class UserStatus extends BaseUpdatableEntity {

  // 연관관계 필드
  // user_id uuid unique not null references users (id) on delete cascade
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  // last_active_at timestamptz not null
  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;


  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  // 온라인인지 아닌지 (5분 이내이면 true, 아니면 false)
  public User.Status status() {
    // 현재(Instant.now()) -(minusSeconds) 5분(5 * 60) 이 이후이면(isAfter)
    if (lastActiveAt.isAfter(Instant.now().minusSeconds(5 * 60))) {
      return User.Status.ONLINE;
    }
    return User.Status.OFFLINE;
  }

  public void updateLastOnline(Instant lastActiveAt) {
    this.lastActiveAt = lastActiveAt;
  }
}
