package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "\"user\"")
@Entity
@NoArgsConstructor
public class User extends BaseUpdatableEntity {


  @Column(nullable = false, length = 50, unique = true)
  private String username;

  @Column(nullable = false, length = 60)
  private String password;

  @Column(nullable = false, length = 100, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private Role role;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "profile_id")
  private BinaryContent profile;


  public User(String username, String email, String password,
      BinaryContent profile) {
    this.profile = profile;
    this.email = email;
    this.role = Role.USER;
    this.password = password;
    this.username = username;
  }

  public void updateUsername(String username) {
    this.username = username;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updateProfile(BinaryContent profile) {
    this.profile = profile;
  }


  public boolean checkSamePassword(String password) {
    return this.password.equals(password);
  }

  public void updateRole(Role role) {
    this.role = role;
  }


  public enum Role {

    ADMIN, USER, CHANNEL_MANAGER
  }


}
