package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.BadCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto login(LoginRequest dto) {
    log.debug("login 시도 - username: {}", dto.username()); // 보안을 위해 유저의 이름만 기록

    // dto에서 가져온 이름으로 찾은 유저가 있다면, 유저 비밀번호와 일치하는지 확인
    User user = userRepository.findByUsername(dto.username())
        .orElseThrow(
            () -> new BadCredentialsException(dto.username()));

    if (!user.getPassword().equals(dto.password())) {
      throw new BadCredentialsException(dto.username());
    }

    UserStatus status = user.getStatus();
    status.updateTime(Instant.now());

    log.info("로그인 성공 - userId: {}", user.getId());
    return userMapper.toDto(user);
  }
}
