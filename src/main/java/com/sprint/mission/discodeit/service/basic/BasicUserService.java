package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public UserResponse createUser(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    log.debug("user create trial: request={}, profile={}", userCreateRequest,
        binaryContentCreateRequest.isPresent());
    if (this.userRepository.existsByUsername(userCreateRequest.username())) {
      throw DuplicateUserException.withUsername(userCreateRequest.username());
    }
    if (this.userRepository.existsByEmail(userCreateRequest.email())) {
      throw DuplicateUserException.withEmail(userCreateRequest.email());
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
        this.passwordEncoder.encode(userCreateRequest.password()),
        profile
    );

    UserStatus status = new UserStatus(user);
    user.initStatus(status);

    this.userRepository.save(user);

    log.info("user create success: id={}, username={}", user.getId(), user.getUsername());
    return this.mapper.toResponse(user);
  }

  @Override
  public UserResponse findById(UUID id) {
    log.debug("user find-by-id trial: id={}", id);
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    log.info("user find-by-id success: id={}", id);
    return this.mapper.toResponse(user);
  }

  @Override
  public List<UserResponse> findAll() {
    log.debug("user find-all trial");
    List<User> users = this.userRepository.findAll();

    log.info("user find-all success: count={}", users.size());
    return users.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @Transactional
  @Override
  public UserResponse updateUser(UUID id, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> binaryContentCreateRequest) {
    log.debug("user update trial: id={}, request={}, profile={}", id, userUpdateRequest,
        binaryContentCreateRequest.isPresent());
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    String username = user.getUsername();
    if (userUpdateRequest.newUsername() != null && !userUpdateRequest.newUsername().isBlank()) {
      if (!user.getUsername().equals(userUpdateRequest.newUsername())
          && this.userRepository.existsByUsername(userUpdateRequest.newUsername())) {
        throw DuplicateUserException.withUsername(userUpdateRequest.newUsername());
      }
      username = userUpdateRequest.newUsername();
    }

    String email = user.getEmail();
    if (userUpdateRequest.newEmail() != null && !userUpdateRequest.newEmail().isBlank()) {
      if (!user.getEmail().equals(userUpdateRequest.newEmail())
          && this.userRepository.existsByEmail(userUpdateRequest.newEmail())) {
        throw DuplicateUserException.withEmail(userUpdateRequest.newEmail());
      }
      email = userUpdateRequest.newEmail();
    }

    String password = user.getPassword();
    if (userUpdateRequest.newPassword() != null && !userUpdateRequest.newPassword().isBlank()) {
      password = this.passwordEncoder.encode(userUpdateRequest.newPassword());
    }

    BinaryContent profile = user.getProfile();
    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      profile = new BinaryContent(req.fileName(), req.size(), req.contentType());
      this.binaryContentStorage.put(profile.getId(), req.bytes());
    }

    user.update(username, email, password, profile);
    user.getStatus().updateLastActiveAt(Instant.now());

    log.info("user update success: id={}, username={}", id, user.getUsername());
    return this.mapper.toResponse(user);
  }

  @Transactional
  @Override
  public void deleteUser(UUID id) {
    log.debug("user delete trial: id={}", id);
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    this.readStatusRepository.deleteAllByUser(user);

    this.userRepository.delete(user);

    log.info("user delete success: id={}", user.getId());
  }
}