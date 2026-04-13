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
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Transactional
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
              byte[] bytes = profileRequest.bytes();  // bytes 변수 선언 추가
              BinaryContent binaryContent = new BinaryContent(
                      profileRequest.fileName(),
                      (long) bytes.length,
                      profileRequest.contentType()
              );
              BinaryContent saved = binaryContentRepository.save(binaryContent);
              binaryContentStorage.put(saved.getId(), bytes);  // 정상
              return saved;
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

  @Transactional
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
              byte[] bytes = profileRequest.bytes();  // bytes 선언
              Optional.ofNullable(user.getProfile())
                      .ifPresent(profile -> binaryContentRepository.deleteById(profile.getId()));
              BinaryContent saved = binaryContentRepository.save(new BinaryContent(
                      profileRequest.fileName(),
                      (long) bytes.length,
                      profileRequest.contentType()
              ));
              binaryContentStorage.put(saved.getId(), bytes);  // 여기서 저장
              return saved;
            })
            .orElse(null);

    user.update(newUsername, newEmail, userUpdateRequest.newPassword(), nullableProfile);
    User updatedUser = userRepository.save(user);

    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
    return userMapper.toDto(updatedUser, userStatus);
  }

  @Transactional
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