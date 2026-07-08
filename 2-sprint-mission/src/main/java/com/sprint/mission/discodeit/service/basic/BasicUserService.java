package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
  public UserDto.Response create(UserDto.CreateRequest request,
      BinaryContentDto.CreateRequest profileImageRequest) {
    log.debug("사용자 생성 시작: username={}, email={}", request.username(), request.email());

    if (userRepository.existsByUsername(request.username())) {
      throw UserAlreadyExistsException.withUsername(request.username());
    }

    if (userRepository.existsByEmail(request.email())) {
      throw UserAlreadyExistsException.withEmail(request.email());
    }

    BinaryContent profile = null;

    try {
      if (profileImageRequest != null) {
        profile = profileImageRequest.toEntity();
        binaryContentRepository.save(profile);
        binaryContentStorage.put(profile.getId(), profileImageRequest.bytes());
      }

      String encodedPassword = passwordEncoder.encode(request.password());
      User user = request.toEntity(encodedPassword, profile);

      userRepository.save(user);

      log.info("사용자 생성 완료: userId={}, username={}", user.getId(), user.getUsername());
      return userMapper.toDto(user);
    } catch (RuntimeException e) {
      if (profile != null) {
        log.warn("사용자 생성 중 오류 발생으로 저장된 프로필 이미지 삭제: imageId={}", profile.getId());
        binaryContentStorage.delete(profile.getId());
      }
      throw e;
    }
  }

  @Override
  public UserDto.Response findById(UUID id) {
    log.debug("사용자 단건 조회 시작: id={}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    log.info("사용자 단건 조회 완료: id={}", id);
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto.Response> findAll() {
    log.debug("사용자 전체 조회 시작");

    List<UserDto.Response> responses = userRepository.findAllWithProfile().stream()
        .map(userMapper::toDto)
        .toList();

    log.info("사용자 전체 조회 완료: 총 {}건", responses.size());
    return responses;
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN') or #id == principal.userDto.id")
  public UserDto.Response update(UUID id, UserDto.UpdateRequest request,
      BinaryContentDto.CreateRequest profileImageRequest) {
    log.debug("사용자 업데이트 시작: id={}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    BinaryContent newImage = null;
    try {
      if (profileImageRequest != null) {
        newImage = profileImageRequest.toEntity();

        binaryContentRepository.save(newImage);
        binaryContentStorage.put(newImage.getId(), profileImageRequest.bytes());

        user.updateProfileImage(newImage);
      }

      // 사용자명 수정
      Optional.ofNullable(request.newUsername())
          .filter(newUsername -> !newUsername.equals(user.getUsername()))
          .ifPresent(newUsername -> {
            if (userRepository.existsByUsername(newUsername)) {
              throw UserAlreadyExistsException.withUsername(newUsername);
            }
            user.changeUsername(newUsername);
          });

      // 이메일 수정
      Optional.ofNullable(request.newEmail())
          .filter(newEmail -> !newEmail.equals(user.getEmail()))
          .ifPresent(newEmail -> {
            if (userRepository.existsByEmail(newEmail)) {
              throw UserAlreadyExistsException.withEmail(newEmail);
            }
            user.changeEmail(newEmail);
          });

      // 비밀번호 수정
      Optional.ofNullable(request.newPassword())
          .map(passwordEncoder::encode)
          .ifPresent(user::changePassword);

      log.info("사용자 업데이트 완료: userId={}", id);
      return userMapper.toDto(user);
    } catch (RuntimeException e) {
      if (newImage != null) {
        log.warn("사용자 업데이트 중 오류 발생으로 저장된 이미지 삭제: imageId={}", newImage.getId());
        binaryContentStorage.delete(newImage.getId());
      }
      throw e;
    }
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN') or #id == principal.userDto.id")
  public void delete(UUID id) {
    log.debug("사용자 삭제 시작: id={}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> UserNotFoundException.withId(id));

    userRepository.delete(user);
    log.info("사용자 삭제 완료: userId={}", id);
  }
}