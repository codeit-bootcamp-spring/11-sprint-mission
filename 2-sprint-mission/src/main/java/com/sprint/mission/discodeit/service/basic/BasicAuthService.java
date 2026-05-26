package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.AuthDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto.Response login(AuthDto.LoginRequest request) {
    log.debug("로그인 시작: username={}", request.username());
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(InvalidCredentialsException::wrongPassword);

    // 비밀번호 검증
    if (!user.matchesPassword(request.password())) {
      throw InvalidCredentialsException.wrongPassword();
    }

    log.info("로그인 성공: userId={}, username={}", user.getId(), user.getUsername());
    return userMapper.toDto(user);
  }
}