// com.sprint.mission.discodeit.security.DiscodeitUserDetailsService

package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  // 세션이 열려있는 동안 user.getProfile() 등 지연 로딩 연관관계를 매핑해야 하므로 트랜잭션 필요
  // (없으면 프로필이 있는 계정 로그인 시 LazyInitializationException 발생)
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

    var userDto = userMapper.toDto(user, false);  // 로그인 시점엔 아직 세션 없으므로 false

    return new DiscodeitUserDetails(userDto, user.getPassword());
  }
}