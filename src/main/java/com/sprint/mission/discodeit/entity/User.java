package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 60)
  private String password;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id")
  private BinaryContent profile;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.USER;  // 추가

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.role = Role.USER;  // 추가
  }

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
    this.role = Role.USER;  // 추가
  }

  public void update(String newUsername, String newEmail, String newPassword) {
    if (newUsername != null && !newUsername.isEmpty()) {
      this.username = newUsername;
    }
    if (newEmail != null && !newEmail.isEmpty()) {
      this.email = newEmail;
    }
    if (newPassword != null && !newPassword.isEmpty()) {
      this.password = newPassword;
    }
  }

  public void updateProfile(BinaryContent profile) {
    this.profile = profile;
  }

  public void updateRole(Role role) {  // 추가
    this.role = role;
  }
}