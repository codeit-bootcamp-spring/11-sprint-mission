package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserCreatedEvent;
import com.sprint.mission.discodeit.event.user.UserDeletedEvent;
import com.sprint.mission.discodeit.event.user.UserUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserEmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.io.IOException;
import java.time.Instant;
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
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;

  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  // Create
  @Override
  @Transactional
  @CacheEvict(value = "users", allEntries = true)
  public UserDto create(UserCreateRequest dto, MultipartFile profile) {

    log.debug("[USER_CREATE_START] 유저 생성 시작 - 유저 이름={}, 유저 이메일={}", dto.username(), dto.email());

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (userRepository.existsByUsername(dto.username())) {
      log.warn("[USER_CREATE_FAILED] 유저 생성 실패 - 이름 중복 - 유저 이름={}", dto.username());
      throw new UsernameAlreadyExistException(dto.username());
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (userRepository.existsByEmail(dto.email())) {
      log.warn("[USER_CREATE_FAILED] 유저 생성 실패 - 이메일 중복 - 유저 이메일={}", dto.email());
      throw new UserEmailAlreadyExistsException(dto.email());
    }

    // 유저 생성 전 요청받은 비밀번호를 암호화
    String encodePassword = passwordEncoder.encode(dto.password());

    // 유저 생성(이름, 이메일 비밀번호)
    User user = User.create(dto.username(), dto.email(), encodePassword);

    // User -> BinaryContent(ProfileImage) 순으로 생성
    User savedUser = userRepository.save(user);

    // 프로필 이미지 등록(선택)
    if (profile != null && !profile.isEmpty()) {
      log.debug("[USER_CREATE_PROFILE_START] 유저 프로필 이미지 등록 시작 - 프로필 이미지 이름={}",
          profile.getOriginalFilename());

      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      BinaryContent savedProfileImage = binaryContentRepository.save(profileImage);
      savedUser.updateProfile(savedProfileImage);

      // 기존 BinaryContentStorage.put 메서드를 이벤트로 처리
      // 이벤트 리스너에서 AFTER_COMMIT 옵션으로 트랜잭션 커밋 후 전달받은 이벤트를 처리하기 때문에 DB 커넥션 점유 시간 감소
      eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(
              savedProfileImage,
              savedProfileImage.getCreatedAt(),
              getBytes(profile),
              null,
              savedUser.getId()
          )
      );

      log.debug("[USER_CREATE_PROFILE_SUCCESS] 유저 프로필 이미지 등록 완료 - 프로필 ID={}", profileImage.getId());
    }

    log.info("[USER_CREATE_SUCCESS] 유저 생성 완료 - 유저 ID={}, 유저 이름={}", user.getId(),
        user.getUsername());

    UserDto userDto = userMapper.toDto(savedUser);

    eventPublisher.publishEvent(
        new UserCreatedEvent(
            userDto,
            user.getCreatedAt()
        ));

    return userDto;
  }


  // Read
  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new UserNotFoundException(id)
    );

    return userMapper.toDto(user);
  }

  // 모든 사용자를 조회
  @Override
  @Transactional(readOnly = true)
  // (value만 있을 경우 생략 가능 - JLS(Java Language Specification)의 9.7.3 Single-Element Annotations)
  @Cacheable("users")
  public List<UserDto> findAll() {
    log.info("DB 사용자 목록 조회 실행");

    return userRepository.findAll().stream()
        .map(userMapper::toDto).toList();
  }


  // Update
  // 같은 키, 다른 Value를 put 하면 키는 그대로, Value만 갱신된다.
  @Override
  @Transactional
  @PreAuthorize("#id == authentication.principal.userDto.id")
  @CacheEvict(value = "users", allEntries = true)
  public UserDto update(UUID id, UserUpdateRequest dto, MultipartFile profile) {

    // 비밀번호는 X
    log.debug("[USER_UPDATE_START] 유저 수정 시작 - 수정할 유저 ID={}, 요청한 유저 이름={}, 요청한 유저 이메일={}",
        id, dto.newUsername(), dto.newEmail());

    User user = userRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 존재하지 않음 - 유저 ID={}", id);
          return new UserNotFoundException(id);
        }
    );

    UserDto beforeUser = userMapper.toDto(user);

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (dto.newUsername() != null && userRepository.existsByUsernameAndIdNot(dto.newUsername(),
        id)) {
      log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 중복된 이름 - 유저 이름={}", dto.newUsername());
      throw new UsernameAlreadyExistException(dto.newUsername());
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (dto.newEmail() != null && userRepository.existsByEmailAndIdNot(dto.newEmail(), id)) {
      log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 중복된 이메일 - 유저 이메일={}", dto.newEmail());
      throw new UserEmailAlreadyExistsException(dto.newEmail());
    }

    // 프로필 이미지 수정(선택)
    if (profile != null && !profile.isEmpty()) {
      log.debug("[USER_UPDATE_PROFILE_START] 유저 프로필 이미지 수정 시작 - 수정할 프로필 이미지 이름={}",
          profile.getOriginalFilename());
      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      BinaryContent savedProfileImage = binaryContentRepository.save(profileImage);
      user.updateProfile(savedProfileImage);

      // 기존 BinaryContentStorage.put 메서드를 이벤트로 처리
      // 이벤트 리스너에서 AFTER_COMMIT 옵션으로 트랜잭션 커밋 후 전달받은 이벤트를 처리하기 때문에 DB 커넥션 점유 시간 감소
      eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(
              savedProfileImage,
              savedProfileImage.getCreatedAt(),
              getBytes(profile),
              null,
              user.getId()
          )
      );

      log.debug("[USER_UPDATE_PROFILE_SUCCESS] 유저 프로필 이미지 수정 완료 - 프로필 이미지 ID={}",
          profileImage.getId());
    }

    // 이름, 이메일, 패스워드 update
    if (dto.newUsername() != null) {
      user.updateName(dto.newUsername());
    }
    if (dto.newEmail() != null) {
      user.updateEmail(dto.newEmail());
    }
    if (dto.newPassword() != null) {
      user.updatePassword(passwordEncoder.encode(dto.newPassword()));
    }

    UserDto afterUser = userMapper.toDto(user);

    eventPublisher.publishEvent(new UserUpdatedEvent(
        beforeUser,
        afterUser,
        Instant.now()
    ));

    log.info("[USER_UPDATE_SUCCESS] 유저 수정 완료 - 수정한 유저 ID={}", id);

    return afterUser;
  }

  // Delete
  // 기존 User만 삭제
  // 고도화 이후 : User, UserStatus, 프로필 이미지 삭제
  // 2차 고도화 이후 : User, 프로필 이미지 삭제
  @Override
  @Transactional
  @PreAuthorize("#id == authentication.principal.userDto.id")
  @CacheEvict(value = "users", allEntries = true)
  public void delete(UUID id) {
    log.debug("[USER_DELETE_START] 유저 삭제 시작 - 삭제할 유저 ID={}", id);

    User user = userRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[USER_DELETE_FAILED] 유저 삭제 실패 - 존재하지 않음 - 유저 ID={}", id);
          return new UserNotFoundException(id);
        }
    );

    UserDto userDto = userMapper.toDto(user);

    // user의 프로필 이미지 삭제
    if (user.getProfile() != null) {
      log.debug("[USER_DELETE_PROFILE_START] 유저 프로필 이미지 삭제 시작 - 프로필 이미지 ID={}",
          user.getProfile().getId());

      binaryContentRepository.deleteById(user.getProfile().getId());

      log.debug("[USER_DELETE_PROFILE_SUCCESS] 유저 프로필 이미지 삭제 완료 - 프로필 이미지 ID={}",
          user.getProfile().getId());
    }

    // user 삭제(cascade에 의해 자동 삭제)
    userRepository.delete(user);

    eventPublisher.publishEvent(new UserDeletedEvent(
        userDto,
        Instant.now()
    ));

    log.info("[USER_DELETE_SUCCESS] 유저 삭제 완료 - 유저 ID={}", id);
  }

  private byte[] getBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
}
