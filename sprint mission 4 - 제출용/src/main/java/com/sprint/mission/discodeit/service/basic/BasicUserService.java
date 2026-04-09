package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public UserResponse create(UserCreateRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw DiscodeitDuplicateException.user(request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw DiscodeitDuplicateException.email(request.getEmail());
    }

    User user = new User(request.getUsername(), request.getEmail(), request.getPassword());
    UserStatus userStatus = new UserStatus(user, Instant.now());
    user.setStatus(userStatus);
    User savedUser = userRepository.save(user);

    BinaryContent profile = savedUser.getProfile();
    UUID profileId = profile == null ? null : profile.getId();

    return new UserResponse(
        savedUser.getId(),
        savedUser.getCreatedAt(),
        savedUser.getUpdatedAt(),
        savedUser.getUsername(),
        savedUser.getEmail(),
        savedUser.getPassword(),
        profileId,
        savedUser.getStatus() != null && savedUser.getStatus().isOnline()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse find(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.user(id));

    UserStatus userStatus = user.getStatus();
    BinaryContent profile = user.getProfile();
    UUID profileId = profile == null ? null : profile.getId();

    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        profileId,
        userStatus != null && userStatus.isOnline()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus userStatus = user.getStatus();
          BinaryContent profile = user.getProfile();
          UUID profileId = profile == null ? null : profile.getId();

          return new UserResponse(
              user.getId(),
              user.getCreatedAt(),
              user.getUpdatedAt(),
              user.getUsername(),
              user.getEmail(),
              user.getPassword(),
              profileId,
              userStatus != null && userStatus.isOnline()
          );
        })
        .toList();
  }

  @Override
  @Transactional
  public void update(UserUpdateRequest request) {
    User user = userRepository.findById(request.getId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getId()));

    String newUsername =
        request.getNewUsername() != null ? request.getNewUsername() : user.getUsername();
    String newEmail = request.getNewEmail() != null ? request.getNewEmail() : user.getEmail();
    String newPassword =
        request.getNewPassword() != null ? request.getNewPassword() : user.getPassword();

    if (userRepository.existsByUsernameAndIdNot(newUsername, request.getId())) {
      throw DiscodeitDuplicateException.user(newUsername);
    }
    if (userRepository.existsByEmailAndIdNot(newEmail, request.getId())) {
      throw DiscodeitDuplicateException.email(newEmail);
    }

    user.updateUser(newUsername, newEmail, newPassword);
    // 변경감지
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.user(id));

    // cascade delete - 일괄 삭제
    userRepository.delete(user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAllDto() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus userStatus = user.getStatus();
          BinaryContent profile = user.getProfile();
          UUID profileId = profile == null ? null : profile.getId();

          return new UserDto(
              user.getId(),
              user.getCreatedAt(),
              user.getUpdatedAt(),
              user.getUsername(),
              user.getEmail(),
              profileId,
              userStatus != null && userStatus.isOnline()
          );
        })
        .toList();
  }
}