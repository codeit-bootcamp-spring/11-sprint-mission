package com.sprint.mission.discodeit.security.auth;


import com.sprint.mission.discodeit.dto.response.UserDto;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

  private final UserDto userDto;
  private final String password;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + userDto.role().name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return userDto.username();
  }

  public String getId() {
    return userDto.id().toString();
  }

  // 계정이 만료되었는지 확인
  // true : 정상 계정 / false : 계정 만료되어 로그인 차단
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  // 계정이 잠겨있는지 확인
  // true : 정상 계정 / false : 계정 잠김되어 로그인 차단
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  // 비밀번호가 만료되었는지 확인
  // true : 비밀번호 만료되지 않음 / false : 비밀번호 변경 필요하여 로그인 차단
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  // 계정이 활성화 되었는지 확인
  // true : 계정 활성화 / false : 계정 비활성화되어 로그인 차단
  @Override
  public boolean isEnabled() {
    return true;
  }

  // Object 클래스의 equals 메서드 오버라이딩
  @Override
  public boolean equals(Object obj) {
    // 메모리 내 객체의 주소는 다르기 때문에 통과됨
    // DiscodeitUserDetails A != DiscodeitUserDetails B, A가 기존 로그인 B가 신규 로그인
    // this.equals(obj) → A.equals(B)
    if (this == obj) {
      return true;
    }

    // 비교하려는 Object가 DiscodeitUserDetails 인지 여부 확인 → DiscodeitUserDetails 이기 때문에 통과
    if (!(obj instanceof DiscodeitUserDetails userDetails)) {
      return false;
    }

    // 객체 주소가 아닌 id를 비교
    // 동시 로그인 시 id가 "abcd"면 "abcd".equals("abcd") → true
    // userDetails는 2번째 if문에서 자동 형변환 시킴, 실제로는 (DiscodeitUserDetails) obj
    // 즉 받아온 신규 로그인의 id(obj.userDto)와 기존 로그인의 id(userDto)를 비교
    // 이후 maximumSession 설정으로 넘어감 → maximumSession 1이면 maxSessionsPreventsLogin 정책으로 넘어감
    return userDto.id().equals(userDetails.userDto.id());
  }

  // Object 클래스의 hashCode 메서드 오버라이딩
  // 같은 Hash 저장소(자료구조)에 들어가기 위해 오버라이딩
  @Override
  public int hashCode() {
    return userDto.id().hashCode();
  }

}
