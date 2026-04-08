package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class User extends BaseEntity {

  private String username;
  //  미사용 필드 주석처리
//  private String nickname;
//  private String description;
  private String email;
  private String password;
  private UUID profileImageId;


  public void changeUsername(String newUsername) {
    this.username = newUsername;
    this.timeUpdate();
  }

  public void changeEmail(String newEmail) {
    this.email = newEmail;
    this.timeUpdate();
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
    this.timeUpdate();
  }

  // 프로필 이미지 수정
  public void updateProfileImage(UUID newProfileImageId) {
    if (newProfileImageId != null && !newProfileImageId.equals(this.profileImageId)) {
      this.profileImageId = newProfileImageId;
    }
    super.timeUpdate();
  }

  // 비밀번호 검증
  // 비밀번호 암호화는 Spring Security의 PasswordEncoder로 스프린트 미션에 맞추어 이후 진행 예정
  public void validatePassword(String password) {
    if (this.password == null || !this.password.equals(password)) {
      throw new IllegalArgumentException("Invalid username or password");
    }
  }

  @Override
  public String toString() {
    return "사용자 [" +
        "UUID: " + getId() +
        "\n이름: " + getUsername() +
        ", 이메일: " + getEmail() +
        //               ", 비밀번호: " + getPassword() + // 추후 비밀번호 관련 로직 변경 예정 + 제외
        ", 프로필 사진: " + getProfileImageId() +
        ", 생성 시간: " + getCreatedAt() +
        ", 수정 시간: " + getUpdatedAt() +
        "]\n";
  }

}
