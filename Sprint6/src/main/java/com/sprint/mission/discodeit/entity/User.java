package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users") // DB의 users 테이블과 매핑
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class User extends BaseUpdatableEntity { // BaseEntity 상속!

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(columnDefinition = "UUID")
  private UUID profileId; // 나중에 BinaryContent 엔티티로 변경 가능

  @Builder
  public User(String username, String email, String password, UUID profileId) {
    // 부모인 BaseEntity 생성자에서 ID가 자동으로 생성됩니다.
    this.username = username;
    this.email = email;
    this.password = password;
    this.profileId = profileId;
  }

  public void update(String newUsername, String newEmail, String newPassword, UUID newProfileId) {
    // 기존 비즈니스 로직 유지
    if (newUsername != null) {
      this.username = newUsername;
    }
    if (newEmail != null) {
      this.email = newEmail;
    }
    if (newPassword != null) {
      this.password = newPassword;
    }
    if (newProfileId != null) {
      this.profileId = newProfileId;
    }

  }
}