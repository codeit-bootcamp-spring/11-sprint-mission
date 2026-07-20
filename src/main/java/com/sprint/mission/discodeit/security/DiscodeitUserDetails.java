package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@EqualsAndHashCode(of = "userDto")
@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

  private final UserDto userDto;
  private final String password;

  // JWT claim으로 인증 주체를 구성한다. 인가에 필요한 id/role만 채우고 나머지는 비운다. (Stateless)
  public static DiscodeitUserDetails ofClaims(UUID userId, String username, Role role) {
    UserDto userDto = new UserDto(userId, username, null, null, null, role);
    return new DiscodeitUserDetails(userDto, null);
  }

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
}
