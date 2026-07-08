package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  private String token;

  @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "expires_at", nullable = false, columnDefinition = "timestamp with time zone")
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean rotated;

  public RefreshToken(String token, UUID userId, Instant expiresAt, boolean rotated) {
    this.token = token;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.rotated = rotated;
  }

  public boolean isExpired() {
    return expiresAt.isBefore(Instant.now());
  }

  public void invalidate() {
    this.expiresAt = Instant.now();
  }
}