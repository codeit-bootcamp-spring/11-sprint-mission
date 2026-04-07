package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User extends BaseUpdatableEntity {

  private String username;
  private String email;
  private String password;

  // 별도로 엔티티있음.
  private BinaryContent profile;
  private UserStatus status;

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public void updateUser(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
}
