package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    log.debug("사용자 생성 시작 - username: {}, email: {}", request.username(), request.email());

    if (userRepository.existsByUsername(request.username())) {
      log.warn("사용자 생성 실패(중복된 이름) - username: {}", request.username());
      throw new UserAlreadyExistsException(request.username());
    }
    if (userRepository.existsByEmail(request.email())) {
      log.warn("사용자 생성 실패(중복된 이메일) - email: {}", request.email());
      throw new UserAlreadyExistsException(request.email());
    }

    User user = new User(request.username(), request.email(), request.password());

    try {
      if (profile != null && !profile.isEmpty()) {
        BinaryContent profileImage = new BinaryContent(
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());

        binaryContentRepository.save(profileImage);

        binaryContentStorage.put(profileImage.getId(), profile.getBytes());

        user.updateProfile(profileImage);
      } else {
        log.warn("프로필 이미지 없이 사용자 생성 - email: {}", request.email());
      }
    } catch (IOException e) {
      log.error("프로필 이미지 처리 중 IO 오류 발생", e);
      throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다.");
    }

    UserStatus userStatus = new UserStatus(user);
    user.updateStatus(userStatus);

    userRepository.save(user);
    log.info("사용자 생성 완료 - userId: {}", user.getId());

    return userMapper.toDto(user);
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 조회 실패(존재하지 않는 유저) - userId: {}", id);
          return new UserNotFoundException(id);
        });
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
    log.debug("사용자 수정 시작 - userId: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 수정 실패(존재하지 않는 유저) - userId: {}", id);
          return new UserNotFoundException(id);
        });
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
      log.error("프로필 이미지 업데이트 중 IO 오류 발생", e);
      throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
    }

    log.info("사용자 수정 완료 - userId: {}", user.getId());

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("사용자 삭제 시작 - userId: {}", id);
    userRepository.deleteById(id);
    log.info("사용자 삭제 완료 - userId: {}", id);
  }

  @Override
  public UserDto login(UserLoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> {
          log.warn("로그인 실패(존재하지 않는 유저) - username: {}", request.username());
          return new IllegalArgumentException("가입되지 않은 유저이름입니다.");
        });

    if (!user.getPassword().equals(request.password())) {
      log.warn("로그인 실패(비밀번호 불일치) - username: {}", request.username());
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
    return userMapper.toDto(user);
  }
}