package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
                        Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String username = userCreateRequest.username();
    String email = userCreateRequest.email();

    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
            .map(profileRequest -> {
              BinaryContent binaryContent = new BinaryContent(
                      profileRequest.fileName(),
                      (long) profileRequest.bytes().length,
                      profileRequest.contentType()
              );
              return binaryContentRepository.save(binaryContent);
            })
            .orElse(null);

    User user = new User(username, email, userCreateRequest.password(), nullableProfile);
    User createdUser = userRepository.save(user);

    UserStatus userStatus = new UserStatus(createdUser, Instant.now());
    userStatusRepository.save(userStatus);

    return userMapper.toDto(createdUser, userStatus);
  }

  @Override
  public UserDto find(UUID userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
    return userMapper.toDto(user, userStatus);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
            .map(user -> {
              UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
              return userMapper.toDto(user, userStatus);
            })
            .toList();
  }

  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
                        Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();
    if (userRepository.existsByEmail(newEmail)) {
      throw new IllegalArgumentException("User with email " + newEmail + " already exists");
    }
    if (userRepository.existsByUsername(newUsername)) {
      throw new IllegalArgumentException("User with username " + newUsername + " already exists");
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
            .map(profileRequest -> {
              Optional.ofNullable(user.getProfile())
                      .ifPresent(profile -> binaryContentRepository.deleteById(profile.getId()));
              return binaryContentRepository.save(new BinaryContent(
                      profileRequest.fileName(),
                      (long) profileRequest.bytes().length,
                      profileRequest.contentType()
              ));
            })
            .orElse(null);

    user.update(newUsername, newEmail, userUpdateRequest.newPassword(), nullableProfile);
    User updatedUser = userRepository.save(user);

    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
    return userMapper.toDto(updatedUser, userStatus);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

    Optional.ofNullable(user.getProfile())
            .ifPresent(profile -> binaryContentRepository.deleteById(profile.getId()));
    userStatusRepository.deleteByUserId(userId);
    userRepository.deleteById(userId);
  }
}