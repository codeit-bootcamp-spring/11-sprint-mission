package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.util.List;
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
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;

  @Override
  @Transactional
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
        BinaryContent profileImage = new BinaryContent(
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());

        binaryContentRepository.save(profileImage);

        binaryContentStorage.put(profileImage.getId(), profile.getBytes());

        user.updateProfile(profileImage);
      }
    } catch (IOException e) {
      throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다.");
    }

    UserStatus userStatus = new UserStatus(user);
    user.updateStatus(userStatus);

    userRepository.save(user);

    return userMapper.toDto(user);
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    if (request.newUsername() != null) {
      user.update(request.newUsername());
    }
    try {
      if (profile != null && !profile.isEmpty()) {
        BinaryContent newProfile = new BinaryContent(
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());

        binaryContentStorage.put(newProfile.getId(), profile.getBytes());

        user.updateProfile(newProfile);
      }
    } catch (IOException e) {
      throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
    }
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    userRepository.deleteById(id);
  }

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() ->
            new IllegalArgumentException("가입되지 않은 유저이름입니다."));
    if (!user.getPassword().equals(request.password())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    return userMapper.toDto(user);
  }
}