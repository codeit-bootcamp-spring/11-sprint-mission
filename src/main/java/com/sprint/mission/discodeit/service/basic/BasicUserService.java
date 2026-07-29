package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binaryContent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserEmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserPasswordAlreadyUsedException;
import com.sprint.mission.discodeit.exception.user.UserUsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

  //create
  @Transactional
  @Override
  @CacheEvict(value = CacheConfig.USERS, allEntries = true)
  public UserDto create(UserCreateRequest request, BinaryContentCreateRequest profileImage) {
    // 중복 이메일 검증
    log.debug("유저 생성 시작 - email: {}, username: {}", request.email(), request.username());
    if (userRepository.existsByEmail(request.email())) {
      log.warn("유저 생성 실패 - 중복 이메일:{}", request.email());
      throw new UserEmailAlreadyExistsException(request.email());
    }
    // 중복 이름 검증
    if (userRepository.existsByUsername(request.username())) {
      log.warn("유저 생성 실패 - 중복 이름: {}", request.username());
      throw new UserUsernameAlreadyExistsException(request.username());
    }
    // 패스워드 encode
    String encodedPassword = passwordEncoder.encode(request.password());

    // User를 프로필 없이 먼저 생성
    User user = new User(request.username(), request.email(), encodedPassword, null);

    // 프로필 이미지 선택 생성
    BinaryContent profile = saveProfileImage(profileImage, user.getId());
    user.updateUserProfile(profile);

    userRepository.save(user);
    UserDto dto = userMapper.toDto(user);
    eventPublisher.publishEvent(new UserCreatedEvent(dto));

    log.info("유저 생성 완료 - userId: {}, email: {}", user.getId(), user.getEmail());
    return dto; //UserStatus도 cascade로 자동 저장
  }

  //Read
  @Override
  @Transactional(readOnly = true)
  public UserDto findById(UUID userId) {
    User user = findUserOrThrow(userId);
    return userMapper.toDto(user).withOnline(isOnline(userId));
  }

  //Read all
  @Cacheable(cacheNames = "users", value = CacheConfig.USERS)
  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAll() {
    return userRepository.findAllWithDetails().stream()
        .map(user -> userMapper.toDto(user)
            .withOnline(isOnline(user.getId())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  //Update
  @CacheEvict(value = CacheConfig.USERS, allEntries = true)
  @Transactional
  @Override
  @PreAuthorize("#userId == authentication.principal.userDto.id")
  public UserDto update(UUID userId, UserUpdateRequest request,
      BinaryContentCreateRequest profileImage) {
    log.debug("유저 업데이트 시작 - userId: {}, newUsername: {}, newEmail: {}", userId,
        request.newUsername(),
        request.newEmail());
    User user = findUserOrThrow(userId);

    // 중복 이메일 검증
    if (!user.getEmail().equals(request.newEmail()) && userRepository.existsByEmail(
        request.newEmail())) {
      log.warn("사용중인 이메일 - newEmail: {}", request.newEmail());
      throw new UserEmailAlreadyExistsException(request.newEmail());
    }
    // 중복 이름 검증
    if (!user.getUsername().equals(request.newUsername())
        && userRepository.existsByUsername(request.newUsername())) {
      log.warn("사용중인 이름 - newUsername: {}", request.newUsername());
      throw new UserUsernameAlreadyExistsException(request.newUsername());
    }

    // 사용중인 패스워드인지 검증 후 업데이트
    if (request.newPassword() != null) {
      if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
        log.warn("기존과 동일한 비밀번호 - userId: {}", userId);
        throw new UserPasswordAlreadyUsedException();
      }
      String newEncodedPassword = passwordEncoder.encode(request.newPassword());
      user.updatePassword(newEncodedPassword);
    }

    if (profileImage != null) {
      if (user.getProfile() != null) {
        UUID oldProfileId = user.getProfile().getId();
        binaryContentRepository.delete(user.getProfile());
        log.debug("기존 프로필 이미지 삭제 완료 - profileId: {}", oldProfileId);
      }
      user.updateUserProfile(saveProfileImage(profileImage, userId));
    }

    if (request.newUsername() != null) {
      user.updateUserName(request.newUsername());
    }
    if (request.newEmail() != null) {
      user.updateUserEmail(request.newEmail());
    }
    UserDto dto = userMapper.toDto(user);
    eventPublisher.publishEvent(new UserUpdatedEvent(dto));

    log.info("유저 업데이트 완료 - newUsername: {}, newEmail: {}", user.getUsername(), user.getEmail());
    return dto;
  }

  //Delete
  @CacheEvict(value = CacheConfig.USERS, allEntries = true)
  @Transactional
  @Override
  @PreAuthorize("#userId == authentication.principal.userDto.id")
  public void delete(UUID userId) {
    log.debug("유저 삭제 시작 - userId: {}", userId);
    User user = findUserOrThrow(userId);

    //프로필 이미지 삭제
    if (user.getProfile() != null) {
      binaryContentStorage.delete(user.getProfile().getId());
      log.debug("유저 프로필 삭제");
    }

    userRepository.deleteById(userId); //JPA가 UserStatus도 cascade 삭제
    eventPublisher.publishEvent(new UserDeletedEvent(userId));
    log.info("유저 삭제 완료 - userId: {}", userId);
  }

  // 유저 아이디 검증 로직
  private User findUserOrThrow(UUID userId) {
    return userRepository.findByIdWithDetails(userId)
        .orElseThrow(() -> {
          log.warn("유저를 찾을 수 없음 - userId: {}", userId);
          return new UserNotFoundException(userId);
        });
  }

  // 프로필 생성 로직
  private BinaryContent saveProfileImage(BinaryContentCreateRequest profileImage, UUID ownerId) {
    log.debug("프로필 이미지 저장 시작");
    if (profileImage == null) {
      return null;
    }

    BinaryContent profile = new BinaryContent(
        profileImage.contentType(),
        profileImage.bytes()
    );

    BinaryContent saved = binaryContentRepository.save(profile);
    eventPublisher.publishEvent(
        new BinaryContentCreatedEvent(saved.getId(), profileImage.bytes(), ownerId)
    );
    log.debug("프로필 이미지 저장 완료 - profileId: {}", saved.getId());
    return saved;
  }

  private boolean isOnline(UUID userId) {
    return jwtRegistry.hasActiveJwtInformationByUserId(userId);
  }


}
