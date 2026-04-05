package com.sprint.mission.discodeit.entity;

import java.util.UUID;
import lombok.Getter;

@Getter
public class User extends BaseEntity {

  private String nickname;
  private String username;
  private String email;
  private String password;
  private String phoneNumber;
  private UUID profileId;

  public User(String nickname, String username, String email, String password, String phoneNumber,
      UUID profileId) {
    this.nickname = nickname;
    this.username = username;
    this.email = email;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.profileId = profileId;
  }

  public void update(String nickname, String username, String email, String password,
      String phoneNumber, UUID profileId) {
    this.nickname = nickname;
    this.username = username;
    this.email = email;
    this.password = password;
    this.phoneNumber = phoneNumber;
    this.profileId = profileId;
  }
}
