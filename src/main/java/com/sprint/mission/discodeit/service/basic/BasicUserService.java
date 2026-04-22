package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DuplicateResourceException;
import com.sprint.mission.discodeit.exception.NotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest request, MultipartFile profile) {
    if (userRepository.existsByUsername(request.username())) {
      throw new DuplicateResourceException(
          "User with username " + request.username() + " already exists");
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new DuplicateResourceException(
          "User with email " + request.email() + " already exists");
    }

    BinaryContent profileContent = null;
    if (profile != null && !profile.isEmpty()) {
      try {
        profileContent = new BinaryContent(
            profile.getOriginalFilename(),
            profile.getSize(),
            profile.getContentType()
        );
        binaryContentRepository.save(profileContent);
        binaryContentStorage.put(profileContent.getId(), profile.getBytes());
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 저장 실패", e);
      }
    }

    User user = new User(request.username(), request.email(), request.password(), profileContent);
    userRepository.save(user);

    UserStatus userStatus = new UserStatus(user, Instant.now());
    userStatusRepository.save(userStatus);

    return toDto(user);
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    return toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    List<User> users = userRepository.findAll();
    List<UUID> userIds = users.stream().map(User::getId).toList();

    Map<UUID, UserStatus> statusMap = userStatusRepository
        .findAllByUserIdIn(userIds).stream()
        .collect(Collectors.toMap(
            us -> us.getUser().getId(),
            us -> us
        ));

    return users.stream()
        .map(user -> userMapper.toDto(user, statusMap.get(user.getId())))
        .toList();
  }

  @Override
  @Transactional
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

    if (request.newUsername() != null && !request.newUsername().equals(user.getUsername())
        && userRepository.existsByUsername(request.newUsername())) {
      throw new DuplicateResourceException(
          "User with username " + request.newUsername() + " already exists");
    }
    if (request.newEmail() != null && !request.newEmail().equals(user.getEmail())
        && userRepository.existsByEmail(request.newEmail())) {
      throw new DuplicateResourceException(
          "User with email " + request.newEmail() + " already exists");
    }

    BinaryContent profileContent = null;
    if (profile != null && !profile.isEmpty()) {
      try {
        profileContent = new BinaryContent(
            profile.getOriginalFilename(),
            (long) profile.getBytes().length,
            profile.getContentType()
        );
        binaryContentRepository.save(profileContent);
        binaryContentStorage.put(profileContent.getId(), profile.getBytes());
      } catch (Exception e) {
        throw new RuntimeException("프로필 이미지 저장 실패", e);
      }
    }

    user.update(request.newUsername(), request.newEmail(), request.newPassword());
    if (profileContent != null) {
      user.updateProfile(profileContent);
    }

    return toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    userRepository.delete(user);
  }

  private UserDto toDto(User user) {
    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
    return userMapper.toDto(user, status);  // ← Mapper 사용!
  }
}