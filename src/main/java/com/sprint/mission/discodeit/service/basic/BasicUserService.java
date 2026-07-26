package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserCreatedEvent;
import com.sprint.mission.discodeit.event.sse.UserDeletedEvent;
import com.sprint.mission.discodeit.event.sse.UserUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final ApplicationEventPublisher eventPublisher;
  private final PasswordEncoder passwordEncoder;

  @CacheEvict(cacheNames = "users", allEntries = true)
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
    }

    User user = new User(
        userCreateRequest.username(),
        userCreateRequest.email(),
        this.passwordEncoder.encode(userCreateRequest.password()),
        profile
    );

    if (binaryContentCreateRequest.isPresent()) {
      BinaryContentCreateRequest req = binaryContentCreateRequest.get();
      this.eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(profile.getId(), req.bytes(), Set.of(user.getId())));
    }

    this.userRepository.save(user);

    UserResponse response = this.mapper.toResponse(user);
    this.eventPublisher.publishEvent(new UserCreatedEvent(response));

    log.info("user create success: id={}, username={}", user.getId(), user.getUsername());
    return response;
  }

  @Override
  public UserResponse findById(UUID id) {
    log.debug("user find-by-id trial: id={}", id);
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    log.info("user find-by-id success: id={}", id);
    return this.mapper.toResponse(user);
  }

  @Cacheable(cacheNames = "users")
  @Override
  public List<UserResponse> findAll() {
    log.debug("user find-all trial");
    List<User> users = this.userRepository.findAll();

    log.info("user find-all success: count={}", users.size());
    return users.stream()
        .map(this.mapper::toResponse)
        .toList();
  }

  @CacheEvict(cacheNames = "users", allEntries = true)
  @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.id == #id")
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
      this.eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(profile.getId(), req.bytes(), Set.of(user.getId())));
    }

    user.update(username, email, password, profile);

    UserResponse response = this.mapper.toResponse(user);
    this.eventPublisher.publishEvent(new UserUpdatedEvent(response));

    log.info("user update success: id={}, username={}", id, user.getUsername());
    return response;
  }

  @CacheEvict(cacheNames = "users", allEntries = true)
  @PreAuthorize("hasRole('ADMIN') or authentication.principal.user.id == #id")
  @Transactional
  @Override
  public void deleteUser(UUID id) {
    log.debug("user delete trial: id={}", id);
    User user = this.userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    UserResponse response = this.mapper.toResponse(user);

    this.readStatusRepository.deleteAllByUser(user);

    this.userRepository.delete(user);

    this.eventPublisher.publishEvent(new UserDeletedEvent(response));

    log.info("user delete success: id={}", user.getId());
  }
}