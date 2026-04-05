package com.sprint.mission.discodeit.entity;


import lombok.AccessLevel;
import lombok.Getter;
import java.util.UUID;

@Getter
public class User extends Entity {

  private String nickname; //닉네임
  private String email;
  private String password; //비밀번호
  private UUID profileId;


  public User(String nickname, String email, String password, UUID profileId) {
    this.password = password;
    this.nickname = nickname;
    this.email = email;
    this.profileId = profileId;

  }


  public boolean updatePassword(String newPassword) {

    this.password = newPassword;
    super.updateUpdatedAt();
    return true;

  }


  public boolean updateNickname(String nickname) {

    this.nickname = nickname;
    super.updateUpdatedAt();
    return true;
  }

  public boolean updateEmail(String email) {

    this.email = email;
    super.updateUpdatedAt();
    return true;
  }

  public boolean updateProfileImage(UUID profileImage) {

    this.profileId = profileImage;
    super.updateUpdatedAt();
    return true;
  }

  public boolean checkSamePassword(String password) {
    return this.password.equals(password);
  }


  @Override
  public String toString() {
    return "User{" +
        "username='" + nickname + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
