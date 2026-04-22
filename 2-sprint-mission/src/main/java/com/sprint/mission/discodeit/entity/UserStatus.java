package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(nullable = false)
  private Instant lastActiveAt;

  @Builder
  public UserStatus(User user, Instant lastActiveAt) {
    this.setUser(user);
    this.lastActiveAt = lastActiveAt != null ? lastActiveAt : Instant.now();
  }

  // 온라인 여부 확인 메서드
  public boolean isOnline() {
    // 접속 시간 5분 이내: 온라인
    return lastActiveAt.isAfter(Instant.now().minusSeconds(300));
  }

  public void updateActiveTime(Instant lastActiveAt) {
    if (lastActiveAt != null && !lastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = lastActiveAt;
    }
  }

  protected void setUser(User user) {
    this.user = user;
    if (user != null) {
      user.setStatus(this);
    }
  }
}