package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (userRepository.existsByUsername(dto.username())) {
      throw new IllegalArgumentException("이미 존재하는 이름입니다.");
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (userRepository.existsByEmail(dto.email())) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    // 유저 생성(이름, 이메일 비밀번호)
    User user = User.create(dto.username(), dto.email(), dto.password());

    // User -> BinaryContent(ProfileImage) -> UserStatus 순으로 생성
    userRepository.save(user);

    // 프로필 이미지 등록(선택)
    if (profile != null && !profile.isEmpty()) {
      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      binaryContentRepository.save(profileImage);
      user.updateProfile(profileImage);
    }

    // 유저 상태 생성
    // UserStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 UserStatusService.create 사용 X
    UserStatus userStatus = new UserStatus(user, Instant.now());

    userStatusRepository.save(userStatus); // cascade에 의해 UserStatus도 자동 저장
    return user;
  }


  // Read
  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 User입니다. id : " + id)
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
    User user = userRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 User입니다. id : " + id)
    );

    // 이름 중복체크 -> UserName이 존재하지만 UserID가 같지 않을 때(다른 사람이 UserName을 가지고 있을 때)
    if (dto.newUsername() != null && userRepository.existsByUsernameAndIdNot(dto.newUsername(),
        id)) {
      throw new IllegalArgumentException("이미 존재하는 이름입니다.");
    }

    // 이메일 중복체크 -> Email이 존재하지만 UserId가 같지 않을 때(다른 사람이 Email을 가지고 있을 때)
    if (dto.newEmail() != null && userRepository.existsByEmailAndIdNot(dto.newEmail(), id)) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    // 프로필 이미지 수정(선택)
    if (profile != null && !profile.isEmpty()) {
      BinaryContent profileImage = BinaryContent.of(
          profile.getOriginalFilename(), profile.getSize(),
          profile.getContentType());

      binaryContentRepository.save(profileImage);
      user.updateProfile(profileImage);
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

    return user;
  }

  // Delete
  @Override
  @Transactional
  // 기존 User만 삭제
  // 고도화 이후 : User, UserStatus, 프로필 이미지 삭제
  public void delete(UUID id) {
    User user = userRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 User입니다. id : " + id)
    );

    // user의 프로필 이미지 삭제
    if (user.getProfile() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    // user 삭제(cascade에 의해 자동 삭제)
    userRepository.delete(user);
  }
}
