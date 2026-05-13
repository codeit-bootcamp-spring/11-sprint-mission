package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.PasswordMismatchException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username()).
        orElseThrow(() -> {
          log.warn("로그인 실패(존재하지 않는 유저) - username: {}", request.username());
          return new UserNotFoundException(request.username());
        });
    if (!user.getPassword().equals(request.password())) {
      log.warn("로그인 실패(비밀번호 불일치) - username: {}", request.username());
      throw new PasswordMismatchException(request.username());
    }
    return userMapper.toDto(user);
  }
}
