package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

  private final User user;

  public UUID getId() {
    // 인증된 사용자의 id를 쉽게 꺼내기 위함
    return user.getId();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Role 구현 전 임시 권한임
    // 이후 User.role 값을 기준으로 ROLE_ADMIN, ROLE_CHANNEL_MANAGER, ROLE_USER 반환하도록 변경 예정임
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    // Spring Security가 PasswordEncoder로 검증할 암호화된 비밀번호 반환함
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    // 로그인 식별자로 username 사용함
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    // 계정 만료 정책은 아직 없으므로 항상 true임
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    // 계정 잠금 정책은 아직 없으므로 항상 true임
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // 비밀번호 만료 정책은 아직 없으므로 항상 true임
    return true;
  }

  @Override
  public boolean isEnabled() {
    // 계정 비활성화 정책은 아직 없으므로 항상 true임
    return true;
  }

  @Override
  public boolean equals(Object o) {
    // 세션 동시성 제어에서 같은 사용자인지 판단하기 위함
    if (this == o) {
      return true;
    }
    if (!(o instanceof DiscodeitUserDetails that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    // equals 기준과 동일하게 user id 기반으로 해시 생성함
    return Objects.hash(getId());
  }
}