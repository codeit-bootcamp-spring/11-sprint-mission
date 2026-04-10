package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_EMAIL_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_EMAIL_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_INVALID_EMAIL_FORMAT;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_INVALID_PASSWORD_LENGTH;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_PASSWORD_REQUIRED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_USERNAME_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_USERNAME_REQUIRED;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final ReadStatusRepository readStatusRepository;
  private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  @Transactional
  @Override
  public UserResponse createUser(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    if (userCreateRequest.username() == null || userCreateRequest.username().isBlank()) {
      throw new ApiException(USER_USERNAME_REQUIRED);
    }
    if (this.userRepository.existsByUsername(userCreateRequest.username())) {
      throw new ApiException(USER_USERNAME_DUPLICATED);
    }

    if (userCreateRequest.email() == null || userCreateRequest.email().isBlank()) {
      throw new ApiException(USER_EMAIL_REQUIRED);
    }
    if (!userCreateRequest.email().matches(EMAIL_REGEX)) {
      throw new ApiException(USER_INVALID_EMAIL_FORMAT);
    }
    if (this.userRepository.existsByEmail(userCreateRequest.email())) {
      throw new ApiException(USER_EMAIL_DUPLICATED);
    }

    if (userCreateRequest.password() == null || userCreateRequest.password().isBlank()) {
      throw new ApiException(USER_PASSWORD_REQUIRED);
    }
    if (userCreateRequest.password().length() < 8) {
      throw new ApiException(USER_INVALID_PASSWORD_LENGTH);
    }

    BinaryContent profile = null;
    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      profile = new BinaryContent(req.fileName(), req.size(), req.contentType(), req.data());
    }

    User user = new User(
        userCreateRequest.username(),
        userCreateRequest.email(),
        userCreateRequest.password(),
        profile
    );

    UserStatus status = new UserStatus(user);
    user.initStatus(status);

    this.userRepository.save(user);

    log.info("{} has been created successfully. ✅ [ID: {}]", user.getUsername(), user.getId());
    return this.toResponse(user);
  }

  @Override
  public UserResponse findById(UUID id) {
    return this.toResponse(this.userRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND)));
  }

  @Override
  public List<UserResponse> findAll() {
    return this.userRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    String username = user.getUsername();
    if (userUpdateRequest.username() != null && !userUpdateRequest.username().isBlank()) {
      if (!user.getUsername().equals(userUpdateRequest.username())
          && this.userRepository.existsByUsername(userUpdateRequest.username())) {
        throw new ApiException(USER_USERNAME_DUPLICATED);
      }
      username = userUpdateRequest.username();
    }

    String email = user.getEmail();
    if (userUpdateRequest.email() != null && !userUpdateRequest.email().isBlank()) {
      if (!userUpdateRequest.email().matches(EMAIL_REGEX)) {
        throw new ApiException(USER_INVALID_EMAIL_FORMAT);
      }
      if (!user.getEmail().equals(userUpdateRequest.email()) && this.userRepository.existsByEmail(
          userUpdateRequest.email())) {
        throw new ApiException(USER_EMAIL_DUPLICATED);
      }
      email = userUpdateRequest.email();
    }

    String password = user.getPassword();
    if (userUpdateRequest.password() != null && !userUpdateRequest.password().isBlank()) {
      if (userUpdateRequest.password().length() < 8) {
        throw new ApiException(USER_INVALID_PASSWORD_LENGTH);
      }
      password = userUpdateRequest.password();
    }

    BinaryContent profile = user.getProfile();
    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      profile = new BinaryContent(req.fileName(), req.size(), req.contentType(), req.data());
    }

    user.update(username, email, password, profile);
    user.getStatus().updateLastActiveAt(Instant.now());

    log.info("{} has been updated successfully. ✅ [ID: {}]", user.getUsername(), id);
    return this.toResponse(user);
  }

  @Transactional
  @Override
  public void deleteUser(UUID id) {
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    this.readStatusRepository.deleteAllByUser(user);

    this.userRepository.delete(user);

    log.info("{} has been deleted successfully. ✅ [ID: {}]", user.getUsername(), id);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        new UserStatusResponse(
            user.getStatus().getUpdatedAt(),
            user.getStatus().getUpdatedAt().isAfter(Instant.now().minusSeconds(5 * 60))
        )
    );
  }
}
