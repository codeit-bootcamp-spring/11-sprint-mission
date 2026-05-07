package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper mapper;

  @Transactional
  @Override
  public UserResponse login(LoginRequest loginRequest) {
    log.debug("auth login trial: username={}", loginRequest.username());
    User user = this.userRepository.findByUsername(loginRequest.username())
        .orElseThrow(() -> UserNotFoundException.withUsername(loginRequest.username()));

    if (!loginRequest.password().equals(user.getPassword())) {
      throw InvalidCredentialsException.withWrongPassword();
    }

    user.getStatus().updateLastActiveAt(Instant.now());

    log.info("auth login success: userId={}, username={}", user.getId(), user.getUsername());
    return this.mapper.toResponse(user);
  }
}