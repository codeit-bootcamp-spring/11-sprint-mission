package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.PasswordChangeEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final JwtRegistry jwtRegistry;

  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public UserDto createUser(UserCreateRequest request, MultipartFile profile) {
    String name = request.username().trim();
    String email = request.email().trim();
    String password = request.password().trim();
    log.debug("사용자 생성 비즈니스 로직 시작 - username: {}, email: {}", name, email);
    if (userRepository.findByUsername(name).isPresent()) {
      log.warn("사용자 생성 실패 - 중복된 username: {}", name);
      throw new DuplicateUserException(name);
    }
    if (userRepository.findByEmail(email).isPresent()) {
      log.warn("사용자 생성 실패 - 중복된 email: {}", email);
      throw new EmailAlreadyExistsException(email);
    }

    BinaryContent profileEntity = null;
    if (profile != null && !profile.isEmpty()) {
      log.debug("프로필 이미지 업로드 시작 - filename: {}, size: {}", profile.getOriginalFilename(),
          profile.getSize());
      try {
        profileEntity = new BinaryContent(profile.getOriginalFilename(), profile.getContentType(),
            profile.getSize());
        BinaryContent savedProfile = binaryContentRepository.save(profileEntity);
        binaryContentStorage.put(savedProfile.getId(), profile.getBytes());

        log.debug("프로필 이미지 저장 완료 - binaryContentId: {}", savedProfile.getId());
      } catch (IOException e) {
        log.error("사용자 프로필 이미지 저장 중 서버 오류 발생 - username: {}", name, e);
        throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR,
            Map.of("errorMessage", e.getMessage()));
      }
    }

    String encodedPassword = passwordEncoder.encode(password);
    User newUser = new User(name, email, encodedPassword, profileEntity);

    userRepository.save(newUser);

    log.info("사용자 생성 성공 - userId: {}, username: {}", newUser.getId(), newUser.getUsername());

    return userMapper.toDto(newUser, false);
  }

  @Override
  public UserDto readUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자를 찾을 수 없음 - userId: {}", id);
          return new UserNotFoundException(id);
        });

    return userMapper.toDto(user, getOnlineUserIds().contains(user.getId()));
  }

  @Override
  public List<UserDto> allReadUser() {
    List<User> users = userRepository.findAll();
    Set<UUID> onlineUserIds = getOnlineUserIds();

    return users.stream()
        .map(user -> userMapper.toDto(user, onlineUserIds.contains(user.getId())))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @PreAuthorize("#id == authentication.principal.userDto.id()")
  public void deleteUser(UUID id) {
    log.debug("사용자 삭제 비즈니스 로직 시작 - userId: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("삭제할 사용자를 찾을 수 없음 - userId: {}", id);
          return new UserNotFoundException(id);
        });

    BinaryContent profile = user.getProfile();
    user.setProfile(null);
    userRepository.save(user);
    if (profile != null) {
      binaryContentRepository.delete(profile);
      log.debug("프로필 이미지 삭제 완료 - userId: {}", id);
    }
    userRepository.delete(user);

    log.info("사용자 삭제 성공 - userId: {}", id);
  }

  @Override
  @Transactional
  @PreAuthorize("#id == authentication.principal.userDto.id()")
  public UserDto updateUser(UUID id, UserUpdateRequest request, MultipartFile profile) {
    log.debug("사용자 업데이트 비즈니스 로직 시작 - updateName: {}, updateEmail: {}", request.newUsername(),
        request.newEmail());
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("업데이트 할 사용자를 찾을 수 없음 - userId: {}", id);
          return new UserNotFoundException(id);
        });
    String name = (request.newUsername() != null) ? request.newUsername() : user.getUsername();
    if (request.newUsername() != null && !name.equals(user.getUsername())) {
      userRepository.findByUsername(name)
          .ifPresent(u -> {
            log.warn("사용자 이름이 이미 존재함 - username: {}", name);
            throw new DuplicateUserException(name);
          });
    }
    String email = (request.newEmail() != null) ? request.newEmail() : user.getEmail();
    if (request.newEmail() != null && !email.equals(user.getEmail())) {
      userRepository.findByEmail(email)
          .ifPresent(u -> {
            log.warn("사용자 이메일이 이미 존재함 - email: {}", email);
            throw new EmailAlreadyExistsException(email);
          });
    }

    String password =
        (request.newPassword() != null) ? passwordEncoder.encode(request.newPassword())
            : user.getPassword();

    BinaryContent currentProfile = user.getProfile();
    if (profile != null && !profile.isEmpty()) {
      if (currentProfile != null) {
        log.debug("기존 프로필 이미지 삭제 - userId: {}", id);
        binaryContentRepository.delete(currentProfile);
      }
      try {
        currentProfile = new BinaryContent(
            profile.getOriginalFilename(),
            profile.getContentType(),
            profile.getSize()
        );
        BinaryContent savedProfile = binaryContentRepository.save(currentProfile);
        binaryContentStorage.put(savedProfile.getId(), profile.getBytes());
        log.debug("새 프로필 이미지 저장 완료 - userId: {}, binaryContentId: {}", id, savedProfile.getId());
      } catch (IOException e) {
        log.error("프로필 이미지 저장 중 서버 오류 발생 - userId: {}", id, e);
        throw new DiscodeitException(ErrorCode.INTERNAL_SERVER_ERROR,
            Map.of("userId", id, "errorMessage", e.getMessage()));
      }
    }

    user.update(name, email, password, currentProfile);

    if (request.newPassword() != null) {
      eventPublisher.publishEvent(new PasswordChangeEvent(id));
    }

    log.info("사용자 정보 업데이트 완료 - userId: {}, username: {}, email: {}", id, name, email);

    return userMapper.toDto(user, getOnlineUserIds().contains(user.getId()));
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(RoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    user.updateRole(request.newRole());

    jwtRegistry.invalidateJwtInformationByUserId(request.userId());

    boolean isOnline = jwtRegistry.hasActiveJwtInformationByUserId(user.getId());
    return userMapper.toDto(user, isOnline);
  }

  private Set<UUID> getOnlineUserIds() {
    return userRepository.findAll().stream()
        .map(User::getId)
        .filter(jwtRegistry::hasActiveJwtInformationByUserId)
        .collect(Collectors.toSet());
  }
}
