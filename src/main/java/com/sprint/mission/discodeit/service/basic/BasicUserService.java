package com.sprint.mission.discodeit.service.basic;

import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_EMAIL_DUPLICATED;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_NOT_FOUND;
import static com.sprint.mission.discodeit.exception.ApiException.ERROR.USER_USERNAME_DUPLICATED;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ApiException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
  private final UserMapper mapper;
  private final BinaryContentStorage binaryContentStorage;
  @Transactional
  @Override
  public UserResponse createUser(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    if (this.userRepository.existsByUsername(userCreateRequest.username())) {
      throw new ApiException(USER_USERNAME_DUPLICATED);
    }
    if (this.userRepository.existsByEmail(userCreateRequest.email())) {
      throw new ApiException(USER_EMAIL_DUPLICATED);
    }

    BinaryContent profile = null;
    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      profile = new BinaryContent(req.fileName(), req.size(), req.contentType());
      this.binaryContentStorage.put(profile.getId(), req.bytes());
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
    return this.mapper.toResponse(user);
  }

  @Override
  public UserResponse findById(UUID id) {
    return this.mapper.toResponse(this.userRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND)));
  }

  @Override
  public List<UserResponse> findAll() {
    return this.userRepository.findAll().stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> new ApiException(USER_NOT_FOUND));

    String username = user.getUsername();
    if (userUpdateRequest.newUsername() != null && !userUpdateRequest.newUsername().isBlank()) {
      if (!user.getUsername().equals(userUpdateRequest.newUsername())
          && this.userRepository.existsByUsername(userUpdateRequest.newUsername())) {
        throw new ApiException(USER_USERNAME_DUPLICATED);
      }
      username = userUpdateRequest.newUsername();
    }

    String email = user.getEmail();
    if (userUpdateRequest.newEmail() != null && !userUpdateRequest.newEmail().isBlank()) {
      if (!user.getEmail().equals(userUpdateRequest.newEmail())
          && this.userRepository.existsByEmail(userUpdateRequest.newEmail())) {
        throw new ApiException(USER_EMAIL_DUPLICATED);
      }
      email = userUpdateRequest.newEmail();
    }

    String password = user.getPassword();
    if (userUpdateRequest.newPassword() != null && !userUpdateRequest.newPassword().isBlank()) {
      password = userUpdateRequest.newPassword();
    }

    BinaryContent profile = user.getProfile();
    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      profile = new BinaryContent(req.fileName(), req.size(), req.contentType());
      this.binaryContentStorage.put(profile.getId(), req.bytes());
    }

    user.update(username, email, password, profile);
    user.getStatus().updateLastActiveAt(Instant.now());

    log.info("{} has been updated successfully. ✅ [ID: {}]", user.getUsername(), id);
    return this.mapper.toResponse(user);
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
}
