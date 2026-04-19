package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.AUTH_INVALID_CREDENTIALS;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
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
    User user = this.userRepository.findByUsername(loginRequest.username())
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    if (!loginRequest.password().equals(user.getPassword())) {
      throw new ApiException(AUTH_INVALID_CREDENTIALS);
    }

    user.getStatus().updateLastActiveAt(Instant.now());

    return this.mapper.toResponse(user);
  }
}
