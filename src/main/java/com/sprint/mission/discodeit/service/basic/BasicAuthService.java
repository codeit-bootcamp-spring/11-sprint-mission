package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.AUTH_INVALID_CREDENTIALS;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.AUTH_PASSWORD_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.AUTH_USERNAME_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ApiException;
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

  @Transactional
  @Override
  public UserResponse login(AuthLoginRequest authLoginRequest) {
    if (authLoginRequest.username() == null || authLoginRequest.username().isBlank()) {
      throw new ApiException(AUTH_USERNAME_REQUIRED);
    }

    if (authLoginRequest.password() == null || authLoginRequest.password().isBlank()) {
      throw new ApiException(AUTH_PASSWORD_REQUIRED);
    }

    User user = this.userRepository.findByUsername(authLoginRequest.username())
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    if (!authLoginRequest.password().equals(user.getPassword())) {
      throw new ApiException(AUTH_INVALID_CREDENTIALS);
    }

    user.getStatus().updateLastActiveAt(Instant.now());

    return this.toResponse(user);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getProfile() == null ? null : user.getProfile().getId(),
        new UserStatusResponse(
            user.getStatus().getLastActiveAt(),
            user.getStatus().getLastActiveAt().isAfter(Instant.now().minusSeconds(5 * 60))
        )
    );
  }
}
