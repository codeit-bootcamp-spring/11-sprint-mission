package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
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

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public UserResponse create(UserCreateRequest request) {
    if (userRepository.existsByUserName(request.getUsername())) {
      throw DiscodeitDuplicateException.user(request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw DiscodeitDuplicateException.email(request.getEmail());
    }

    User user = new User(request.getUsername(), request.getEmail(), request.getPassword());
    userRepository.create(user);

    UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
    userStatusRepository.create(userStatus);

    BinaryContent profile = binaryContentRepository.readByUserId(user.getId());
    UUID profileId = profile == null ? null : profile.getId();

    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getUserName(),
        user.getUserEmail(),
        user.getUserPassword(),
        profileId,
        userStatus.isOnline()
    );
  }

  @Override
  public UserResponse read(UUID id) {
    User user = userRepository.read(id);
    if (user == null) {
      throw DiscodeitNotFoundException.user(id);
    }

    UserStatus userStatus = userStatusRepository.readByUserId(id);
    BinaryContent profile = binaryContentRepository.readByUserId(id);
    UUID profileId = profile == null ? null : profile.getId();

    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getUserName(),
        user.getUserEmail(),
        user.getUserPassword(),
        profileId,
        userStatus != null && userStatus.isOnline()
    );
  }

  @Override
  public List<UserResponse> readAll() {
    return userRepository.readAll().stream()
        .map(user -> {
          UserStatus userStatus = userStatusRepository.readByUserId(user.getId());
          BinaryContent profile = binaryContentRepository.readByUserId(user.getId());
          UUID profileId = profile == null ? null : profile.getId();
          return new UserResponse(
              user.getId(),
              user.getCreatedAt(),
              user.getUpdatedAt(),
              user.getUserName(),
              user.getUserEmail(),
              user.getUserPassword(),
              profileId,
              userStatus != null && userStatus.isOnline()
          );
        })
        .toList();
  }

  @Override
  public void update(UserUpdateRequest request) {
    User user = userRepository.read(request.getId());
    if (user == null) {
      throw DiscodeitNotFoundException.user(request.getId());
    }

    String newUsername =
        request.getNewUsername() != null ? request.getNewUsername() : user.getUserName();
    String newEmail = request.getNewEmail() != null ? request.getNewEmail() : user.getUserEmail();
    String newPassword =
        request.getNewPassword() != null ? request.getNewPassword() : user.getUserPassword();

    if (userRepository.existsByUserNameExcluding(newUsername, request.getId())) {
      throw DiscodeitDuplicateException.user(newUsername);
    }
    if (userRepository.existsByEmailExcluding(newEmail, request.getId())) {
      throw DiscodeitDuplicateException.email(newEmail);
    }

    user.updateUser(newUsername, newEmail, newPassword);
    userRepository.update(user);
  }

  @Override
  public void delete(UUID id) {
    User user = userRepository.read(id);
    if (user == null) {
      throw DiscodeitNotFoundException.user(id);
    }
    binaryContentRepository.deleteByUserId(id);
    userStatusRepository.delete(id);
    userRepository.delete(id);
  }

  @Override
  public List<UserDto> readAllDto() {
    return userRepository.readAll().stream()
        .map(user -> {
          UserStatus userStatus = userStatusRepository.readByUserId(user.getId());
          BinaryContent profile = binaryContentRepository.readByUserId(user.getId());
          UUID profileId = profile == null ? null : profile.getId();

          return new UserDto(
              user.getId(),
              user.getCreatedAt(),
              user.getUpdatedAt(),
              user.getUserName(),
              user.getUserEmail(),
              profileId,
              userStatus != null && userStatus.isOnline()
          );
        })
        .toList();
  }
}