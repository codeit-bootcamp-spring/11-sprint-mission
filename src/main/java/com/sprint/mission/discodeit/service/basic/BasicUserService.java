package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest userDto,
      Optional<BinaryContentCreateRequest> binaryContentDto) {
    log.debug("create 시작 - 입력값: {}, {}", userDto, binaryContentDto);
    if (userRepository.existsByUsernameOrEmail(userDto.username(), userDto.email())) {
      throw new UserAlreadyExistsException(userDto.username(), userDto.email());
    }

    BinaryContent profile = null;
    if (binaryContentDto.isPresent()) {
      BinaryContentCreateRequest profileRequest = binaryContentDto.get();
      profile = new BinaryContent(profileRequest.fileName(), (long) profileRequest.bytes().length,
          profileRequest.contentType());
      profile = binaryContentRepository.save(profile);
      binaryContentStorage.put(profile.getId(), binaryContentDto.get().bytes());
    }

    String encodedPassword = passwordEncoder.encode(userDto.password());
    User user = new User(userDto.username(), userDto.email(), encodedPassword, profile);
    user = userRepository.save(user);

    if (user.getProfile() == null) {
      log.warn("프로필 없이 사용자 생성 - userId: {}", user.getId());
    }
    log.info("사용자 생성 완료 - userId: {}", user.getId());

    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAllWithProfile().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  public UserDto findById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  @PreAuthorize("principal.userDto.id == #userId")
  public UserDto update(UUID userId, UserUpdateRequest userDto,
      Optional<BinaryContentCreateRequest> binaryContentDto) {
    log.debug("update 시작 - 입력값: {}, {}", userDto, binaryContentDto);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // 새로 받은 정보가 나를 제외하고, 다른 객체의 이름 및 이메일과 다른지 검사
    if (userRepository.existsByUsernameAndIdNot(userDto.newUsername(), userId)
        || userRepository.existsByEmailAndIdNot(userDto.newEmail(), userId)) {
      throw new UserAlreadyExistsException(userDto.newUsername(), userDto.newEmail());
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

    String encodedPassword = passwordEncoder.encode(userDto.newPassword());
    user.update(userDto.newUsername(), userDto.newEmail(), encodedPassword, profile);
    log.info("사용자 수정 완료 - userId: {}", user.getId());

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  @PreAuthorize("principal.userDto.id == #userId")
  public void delete(UUID userId) {
    log.debug("delete 시작 - 입력값: {}", userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    userRepository.delete(user);
    log.info("사용자 삭제 완료 - userId: {}", userId);
  }
}
