package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
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
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseUpdatableEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id")
  private BinaryContent profile;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserStatus status;


  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
    this.role = Role.USER;
  }

  // 필드를 수정하는 update 함수
  public void update(String name, String email, String password, BinaryContent profile) {
    this.username = name;
    this.email = email;
    this.password = password;
    this.profile = profile;
  }

  public void updateRole(Role role) {
    this.role = role;
  }

  public void initStatus(UserStatus status) {
    this.status = status;
  }


  @Override
  public String toString() {
    return "User { username=`" + username + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
}
