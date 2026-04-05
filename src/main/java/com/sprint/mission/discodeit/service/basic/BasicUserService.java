package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public UserDto create(UserCreateRequest request, MultipartFile profile) {
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    User user = new User(request.username(), request.email(), request.password());

    try {
      if (profile != null && !profile.isEmpty()) {
        BinaryContent profileImage = new BinaryContent(profile.getBytes(),
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());
        binaryContentRepository.save(profileImage);
        user.updateProfile(profileImage.getId());
      }
    } catch (IOException e) {
      throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다.");
    }

    userRepository.save(user);

    UserStatus userStatus = new UserStatus(user.getId());
    userStatusRepository.save(userStatus);

    return toDto(user, userStatus);
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    UserStatus status = userStatusRepository.findByUserId(id).orElse(null);
    return toDto(user, status);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> toDto(user, userStatusRepository.findByUserId(user.getId()).orElse(null)))
        .collect(Collectors.toList());
  }

  @Override
  public void update(UUID id, UserUpdateRequest request, MultipartFile profile) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    if (request.newUsername() != null) {
      user.update(request.newUsername());
    }
    try {
      if (profile != null && !profile.isEmpty()) {
        if (user.getProfileId() != null) {
          binaryContentRepository.delete(user.getProfileId());
        }
        BinaryContent newProfile = new BinaryContent(profile.getBytes(),
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());
        binaryContentRepository.save(newProfile);
        user.updateProfile(newProfile.getId());
      }
    } catch (IOException e) {
      throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
    }
    userRepository.save(user);
  }

  @Override
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    userStatusRepository.deleteByUserId(user.getId());
    if (user.getProfileId() != null) {
      binaryContentRepository.delete(user.getProfileId());
    }
    userRepository.delete(id);
  }

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() ->
            new IllegalArgumentException("가입되지 않은 유저이름입니다."));
    if (!user.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    UserStatus status = userStatusRepository.findByUserId(
        user.getId()).orElse(null);
    return toDto(user, status);
  }

  private UserDto toDto(User user, UserStatus status) {
    boolean isOnline = (status != null) && status.isOnline();

    return new UserDto(
        user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
        user.getUsername(), user.getEmail(), user.getProfileId(), isOnline
    );
  }
}