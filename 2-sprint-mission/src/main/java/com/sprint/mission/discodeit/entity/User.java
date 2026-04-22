package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

  @Column(length = 50, nullable = false, unique = true)
  private String username;

  @Column(length = 100, nullable = false, unique = true)
  private String email;

  @Column(length = 60, nullable = false)
  private String password;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "profile_id", columnDefinition = "uuid")
  private BinaryContent profile;

  @Setter(AccessLevel.PROTECTED)
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserStatus status;

  //  미사용 필드 주석처리
  //  private String nickname;
  //  private String description;

  @Builder
  public User(String username, String email, String password, BinaryContent profile,
      UserStatus status) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
    this.status = status;
  }

  public void changeUsername(String newUsername) {
    this.username = newUsername;
  }

  public void changeEmail(String newEmail) {
    this.email = newEmail;
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  // 프로필 이미지 수정
  public void updateProfileImage(BinaryContent newProfile) {
    if (newProfile != null && !newProfile.equals(this.profile)) {
      this.profile = newProfile;
    }
  }

  // 비밀번호 검증
  // 비밀번호 암호화는 Spring Security의 PasswordEncoder로 스프린트 미션에 맞추어 이후 진행 예정
  public void validatePassword(String password) {
    if (this.password == null || !this.password.equals(password)) {
      throw new IllegalArgumentException("Invalid username or password");
    }
  }
}
