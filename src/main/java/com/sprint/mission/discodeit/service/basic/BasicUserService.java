package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

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

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(request.username(), request.email(), encodedPassword);

    try {
      if (profile != null && !profile.isEmpty()) {
        BinaryContent profileImage = new BinaryContent(
            profile.getOriginalFilename(), profile.getSize(), profile.getContentType());

        binaryContentRepository.save(profileImage);

        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(profileImage.getId(), profile.getBytes()));

        user.updateProfile(profileImage);
      } else {
        log.warn("프로필 이미지 없이 사용자 생성 - email: {}", request.email());
      }
    } catch (IOException e) {
      log.error("프로필 이미지 처리 중 IO 오류 발생", e);
      throw new RuntimeException("프로필 이미지 처리 중 오류가 발생했습니다.");
    }

    userRepository.save(user);
    log.info("사용자 생성 완료 - userId: {}", user.getId());

    UserDto dto = userMapper.toDto(user);
    return new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), false, dto.role());
  }

  @Override
  public UserDto findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 조회 실패(존재하지 않는 유저) - userId: {}", id);
          return new UserNotFoundException(id);
        });
    UserDto dto = userMapper.toDto(user);
    boolean isOnline = isUserOnline(id);
    return new UserDto(dto.id(), dto.username(), dto.email(), dto.profile(), isOnline, dto.role());
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserDto userDto = userMapper.toDto(user);
          boolean isOnline = jwtRegistry.hasActiveJwtInformationByUserId(user.getId());

          return userDto.withOnline(isOnline);
        })
        .collect(Collectors.toList());
  }

  private boolean isUserOnline(UUID userId) {
    return jwtRegistry.hasActiveJwtInformationByUserId(userId);
  }

  @Override
  @Transactional
  @PreAuthorize("#id == principal.userDto.id or hasRole('ADMIN')")
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

        binaryContentRepository.save(newProfile);

        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(newProfile.getId(), profile.getBytes()));

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
  @PreAuthorize("#id == principal.userDto.id or hasRole('ADMIN')")
  public void delete(UUID id) {
    log.debug("사용자 삭제 시작 - userId: {}", id);

    if (!userRepository.existsById(id)) {
      log.warn("사용자 삭제 실패(존재하지 않는 유저) - userId: {}", id);
      throw new UserNotFoundException(id);
    }

    userRepository.deleteById(id);
    log.info("사용자 삭제 완료 - userId: {}", id);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest request) {
    log.debug("사용자 권한 변경 시작 - userId: {}, newRole: {}", request.userId(), request.newRole());

    User user = userRepository.findById(request.userId()).orElseThrow(() -> {
      log.warn("사용자 권한 변경 실패(존재하지 않는 유저) - userId: {}", request.userId());
      return new UserNotFoundException(request.userId());
    });

    String oldRole = String.valueOf(user.getRole());

    user.updateRole(request.newRole());
    log.info("사용자 권한 변경 완료 - userId: {}, newRole: {}", user.getId(), request.newRole());

    expireUserTokens(request.userId());

    eventPublisher.publishEvent(new RoleUpdatedEvent(
        user.getId(),
        oldRole,
        String.valueOf(request.newRole())
    ));

    return userMapper.toDto(user);
  }

  private void expireUserTokens(UUID targetUserId) {
    log.debug("권한 변경에 따른 JWT 토큰 무효화 처리 시작 - targetUserId: {}", targetUserId);
    jwtRegistry.invalidateJwtInformationByUserId(targetUserId);
    log.info("해당 사용자의 활성 JWT 토큰 무효화 완료 - userId: {}", targetUserId);
  }
}