package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;


  @Override
  @Transactional
  public UserDto create(UserCreateRequest userDto,
      Optional<BinaryContentCreateRequest> binaryContentDto) {
    if (userRepository.existsByUsernameOrEmail(userDto.username(), userDto.email())) {
      throw new IllegalArgumentException(
          "User with email " + userDto.email() + " or username " + userDto.username()
              + " already exists"
      );
    }

    BinaryContent profile = null;
    if (binaryContentDto.isPresent()) {
      BinaryContentCreateRequest profileRequest = binaryContentDto.get();
      profile = new BinaryContent(profileRequest.fileName(), (long) profileRequest.bytes().length,
          profileRequest.contentType());
      profile = binaryContentRepository.save(profile);
      binaryContentStorage.put(profile.getId(), binaryContentDto.get().bytes());
    }

    User user = new User(userDto.username(), userDto.email(), userDto.password(), profile);
    user = userRepository.save(user);

    UserStatus status = new UserStatus(user, Instant.now());
    userStatusRepository.save(status);

    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public UserDto update(UUID userId, UserUpdateRequest userDto,
      Optional<BinaryContentCreateRequest> binaryContentDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

    // 새로 받은 정보가 나를 제외하고, 다른 객체의 이름 및 이메일과 다른지 검사
    if (userRepository.existsByUsernameAndIdNot(userDto.newUsername(), userId)
        || userRepository.existsByEmailAndIdNot(userDto.newEmail(), userId)) {
      throw new IllegalArgumentException(
          "User with email " + userDto.newEmail() + " or username " + userDto.newUsername()
              + " already exists");
    }

    BinaryContent profile = user.getProfile();
    if (binaryContentDto.isPresent()) {

      if (user.getProfile() != null) {
        binaryContentRepository.delete(user.getProfile());
      }

      BinaryContentCreateRequest profileRequest = binaryContentDto.get();
      profile = new BinaryContent(profileRequest.fileName(), (long) profileRequest.bytes().length,
          profileRequest.contentType());
      profile = binaryContentRepository.save(profile);
      binaryContentStorage.put(profile.getId(), binaryContentDto.get().bytes());
    }

    user.update(userDto.newUsername(), userDto.newEmail(), userDto.newPassword(), profile);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("User with id " + id + " not found"));

    userRepository.delete(user);
  }
}
