package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.PasswordNotMatchException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  private final UserMapper userMapper;

  @Transactional
  public UserDto login(LoginRequest loginRequest) {
    String name = loginRequest.username();
    String password = loginRequest.password();

    User user = userRepository.findByUsername(name)
        .orElseThrow(() -> new UserNotFoundException(name));
    if (!user.getPassword().equals(password.trim())) {
      throw new PasswordNotMatchException(name);
    }
    UserStatus status = userStatusRepository.findByUserId(user.getId())
        .orElseThrow(() -> new UserStatusNotFoundException(user.getId()));

    status.update(null);

    userStatusRepository.save(status);

    return userMapper.toDto(user, status);
  }
}
