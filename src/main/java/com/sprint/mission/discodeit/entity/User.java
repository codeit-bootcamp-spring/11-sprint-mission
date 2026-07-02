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
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseUpdatableEntity {

  // 사용자에 대한 데이터
  // username varchar(50) UNIQUE NOT NULL
  @Column(name = "username", length = 50, unique = true, nullable = false)
  private String username; // 사용자 이름

  // email varchar(100) UNIQUE NOT NULL
  @Column(name = "email", length = 100, unique = true, nullable = false)
  private String email; // 사용자 이메일, 중복 불가

  // password varchar(60) NOT NULL
  @Column(name = "password", length = 60, nullable = false)
  private String password;

  // 연관관계 필드
  // 1:1 관계 : 1개의 User는 1개의 BinaryContent(profile)을 갖는다.
  // User가 삭제될 때 프로필이미지도 삭제되어야한다.(부모 : User / 자식 : BinaryContent)
  // profile_id UUID UNIQUE references binary_contents (id) on delete set null
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id", unique = true)
  private BinaryContent profile; // BinaryContent의 id

  // 사용자 권한 : 관리자(ADMIN) / 채널 매니저(CHANNEL_MANAGER) / 일반 사용자(USER)
  @Enumerated(EnumType.STRING)
  private Role role;

  // 생성자 오버로딩하여 코드 탬플릿에 적합한 생성자 생성
  private User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // 정적 팩토리 메서드
  public static User create(String username, String email, String password) {
    User user = new User(username, email, password);

    // 유저 권한 설정(회원가입 시 모든 사용자는 USER 권한을 갖도록 설정)
    user.role = Role.USER;

    return user;
  }

  // get메서드(Lombok의 @Getter 사용)

  public void updateName(String username) {
    this.username = username;
  }

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updatePassword(String password) {
    this.password = password;
  }

  public void updateProfile(BinaryContent profile) {
    this.profile = profile;
  }

  public void updateRole(Role role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "유저 이름 : " + username + "유저 이메일 : " + email;
  }

  public enum Status {
    ONLINE("온라인"), AWAY("자리비움"), DO_NOT_DISTURB("방해 금지"), OFFLINE("오프라인");

    private final String description;

    Status(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Role {
    ADMIN, CHANNEL_MANAGER, USER;
  }
}