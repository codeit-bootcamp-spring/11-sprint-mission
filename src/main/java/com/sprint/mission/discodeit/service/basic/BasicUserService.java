package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
  private final ApplicationEventPublisher eventPublisher;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  public UserDto create(UserCreateRequest request, MultipartFile profile) {
    log.debug("사용자 생성 요청 - username: {}, email: {}", request.username(), request.email());

    if (userRepository.existsByUsername(request.username())) {
      log.warn("사용자 생성 실패 - 중복 username: {}", request.username());
      throw new UserAlreadyExistsException("username", request.username());
    }
    if (userRepository.existsByEmail(request.email())) {
      log.warn("사용자 생성 실패 - 중복 email: {}", request.email());
      throw new UserAlreadyExistsException("email", request.email());
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
        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(profileContent.getId(), profile.getBytes()));
        log.debug("프로필 이미지 메타데이터 저장 완료 - fileId: {}", profileContent.getId());
      } catch (Exception e) {
        log.error("프로필 이미지 저장 실패 - username: {}", request.username(), e);
        throw new RuntimeException("프로필 이미지 저장 실패", e);
      }
    }

    User user = new User(request.username(), request.email(),
        passwordEncoder.encode(request.password()), profileContent);
    userRepository.save(user);

    log.info("사용자 생성 완료 - id: {}, username: {}", user.getId(), user.getUsername());
    return toDto(user);
  }

  @Override
  public UserDto findById(UUID id) {
    log.debug("사용자 단건 조회 - id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 조회 실패 - 존재하지 않는 id: {}", id);
          return new UserNotFoundException(id);
        });
    return toDto(user);
  }

  @Override
  @Cacheable(cacheNames = "users")
  public List<UserDto> findAll() {
    log.debug("사용자 전체 조회");
    return userRepository.findAll().stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  @PreAuthorize("authentication.principal.userDto.id == #id")
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {
    log.debug("사용자 수정 요청 - id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 수정 실패 - 존재하지 않는 id: {}", id);
          return new UserNotFoundException(id);
        });

    if (request.newUsername() != null && !request.newUsername().equals(user.getUsername())
        && userRepository.existsByUsername(request.newUsername())) {
      log.warn("사용자 수정 실패 - 중복 username: {}", request.newUsername());
      throw new UserAlreadyExistsException("username", request.newUsername());
    }
    if (request.newEmail() != null && !request.newEmail().equals(user.getEmail())
        && userRepository.existsByEmail(request.newEmail())) {
      log.warn("사용자 수정 실패 - 중복 email: {}", request.newEmail());
      throw new UserAlreadyExistsException("email", request.newEmail());
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
        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(profileContent.getId(), profile.getBytes()));
        log.debug("프로필 이미지 메타데이터 수정 완료 - fileId: {}", profileContent.getId());
      } catch (Exception e) {
        log.error("프로필 이미지 저장 실패 - userId: {}", id, e);
        throw new RuntimeException("프로필 이미지 저장 실패", e);
      }
    }

    user.update(request.newUsername(), request.newEmail(), request.newPassword());
    if (profileContent != null) {
      user.updateProfile(profileContent);
    }

    log.info("사용자 수정 완료 - id: {}", id);
    return toDto(user);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  @PreAuthorize("authentication.principal.userDto.id == #id")
  public void delete(UUID id) {
    log.debug("사용자 삭제 요청 - id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 삭제 실패 - 존재하지 않는 id: {}", id);
          return new UserNotFoundException(id);
        });
    userRepository.delete(user);
    log.info("사용자 삭제 완료 - id: {}", id);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    Role previousRole = user.getRole();
    user.updateRole(request.newRole());
    eventPublisher.publishEvent(
        new RoleUpdatedEvent(user.getId(), previousRole, request.newRole()));
    return toDto(user);
  }

  private UserDto toDto(User user) {
    boolean isOnline = jwtRegistry.hasActiveJwtInformationByUserId(user.getId());
    return userMapper.toDto(user, isOnline);
  }
}