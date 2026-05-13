package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;

  // Create
  @Override
  @Transactional
  public User create(UserCreateRequest dto, MultipartFile profile) {

    log.debug("[USER_CREATE_START] 유저 생성 시작 - 유저 이름={}, 유저 이메일={}", dto.username(), dto.email());

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (userRepository.existsByUsername(dto.username())) {
      log.warn("[USER_CREATE_FAILED] 유저 생성 실패 - 이름 중복 - 유저 이름={}", dto.username());
      throw new DiscodeitException(ErrorCode.DUPLICATE_USERNAME);
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (userRepository.existsByEmail(dto.email())) {
      log.warn("[USER_CREATE_FAILED] 유저 생성 실패 - 이메일 중복 - 유저 이메일={}", dto.email());
      throw new DiscodeitException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 유저 생성(이름, 이메일 비밀번호)
    User user = User.create(dto.username(), dto.email(), dto.password());

    // User -> BinaryContent(ProfileImage) -> UserStatus 순으로 생성
    userRepository.save(user);

    // 프로필 이미지 등록(선택)
    if (profile != null && !profile.isEmpty()) {
      log.debug("[USER_CREATE_PROFILE_START] 유저 프로필 이미지 등록 시작 - 프로필 이미지 이름={}",
          profile.getOriginalFilename());

      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      binaryContentRepository.save(profileImage);
      user.updateProfile(profileImage);
      log.debug("[USER_CREATE_PROFILE_SUCCESS] 유저 프로필 이미지 등록 완료 - 프로필 ID={}", profileImage.getId());
    }

    // 유저 상태 생성
    // UserStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 UserStatusService.create 사용 X
    UserStatus userStatus = new UserStatus(user, Instant.now());

    userStatusRepository.save(userStatus); // cascade에 의해 UserStatus도 자동 저장
    log.info("[USER_CREATE_SUCCESS] 유저 생성 완료 - 유저 ID={}, 유저 이름={}", user.getId(),
        user.getUsername());
    return user;
  }


  // Read
  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new DiscodeitException(ErrorCode.USER_NOT_FOUND)
    );

    return userMapper.toDto(user);
  }

  // 모든 사용자를 조회
  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto).toList();
  }


  // Update
  // 같은 키, 다른 Value를 put 하면 키는 그대로, Value만 갱신된다.
  @Override
  @Transactional
  public User update(UUID id, UserUpdateRequest dto, MultipartFile profile) {

    // 비밀번호는 X
    log.debug("[USER_UPDATE_START] 유저 수정 시작 - 수정할 유저 ID={}, 요청한 유저 이름={}, 요청한 유저 이메일={}",
        id, dto.newUsername(), dto.newEmail());

    User user = userRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 존재하지 않음 - 유저 ID={}", id);
          return new DiscodeitException(ErrorCode.USER_NOT_FOUND);
        }
    );

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (dto.newUsername() != null && userRepository.existsByUsernameAndIdNot(dto.newUsername(),
        id)) {
      log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 중복된 이름 - 유저 이름={}", dto.newUsername());
      throw new DiscodeitException(ErrorCode.DUPLICATE_USERNAME);
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (dto.newEmail() != null && userRepository.existsByEmailAndIdNot(dto.newEmail(), id)) {
      log.warn("[USER_UPDATE_FAILED] 유저 수정 실패 - 중복된 이메일 - 유저 이메일={}", dto.newEmail());
      throw new DiscodeitException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 프로필 이미지 수정(선택)
    if (profile != null && !profile.isEmpty()) {
      log.debug("[USER_UPDATE_PROFILE_START] 유저 프로필 이미지 수정 시작 - 수정할 프로필 이미지 이름={}",
          profile.getOriginalFilename());
      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      binaryContentRepository.save(profileImage);
      user.updateProfile(profileImage);
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
      user.updatePassword(dto.newPassword());
    }

    userRepository.save(user);

    log.info("[USER_UPDATE_SUCCESS] 유저 수정 완료 - 수정한 유저 ID={}", id);

    return user;
  }

  // Delete
  @Override
  @Transactional
  // 기존 User만 삭제
  // 고도화 이후 : User, UserStatus, 프로필 이미지 삭제
  public void delete(UUID id) {
    log.debug("[USER_DELETE_START] 유저 삭제 시작 - 삭제할 유저 ID={}", id);

    User user = userRepository.findById(id).orElseThrow(
        () -> {
          log.warn("[USER_DELETE_FAILED] 유저 삭제 실패 - 존재하지 않음 - 유저 ID={}", id);
          return new DiscodeitException(ErrorCode.USER_NOT_FOUND);
        }
    );

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

    log.info("[USER_DELETE_SUCCESS] 유저 삭제 완료 - 유저 ID={}", id);
  }
}
