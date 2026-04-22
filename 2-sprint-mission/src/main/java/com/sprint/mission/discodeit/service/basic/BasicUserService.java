package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  public UserDto.Response create(UserDto.CreateRequest request,
      BinaryContentDto.CreateRequest profileImageRequest) {
    if (userRepository.existsByUsername(request.username())) {
      throw new BusinessException(ErrorCode.DUPLICATE_NAME);
    }

    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }

    BinaryContent profile = profileImageRequest != null ? profileImageRequest.toEntity() : null;
    User user = request.toEntity(profile);

    userRepository.save(user);

    if (profile != null) {
      binaryContentStorage.put(profile.getId(), profileImageRequest.bytes());
    }

    return userMapper.toDto(user);
  }

  @Override
  public UserDto.Response findById(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto.Response> findAll() {
    return userRepository.findAllWithProfileAndStatus().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public UserDto.Response update(UUID id, UserDto.UpdateRequest request,
      BinaryContentDto.CreateRequest profileImageRequest) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (profileImageRequest != null) {
      BinaryContent newImage = profileImageRequest.toEntity();

      binaryContentRepository.save(newImage);
      binaryContentStorage.put(newImage.getId(), profileImageRequest.bytes());

      user.updateProfileImage(newImage);
    }
    // 사용자명 수정
    Optional.ofNullable(request.newUsername())
        .filter(newUsername -> !newUsername.equals(user.getUsername()))
        .ifPresent(newUsername -> {
          if (userRepository.existsByUsername(newUsername)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NAME);
          }
          user.changeUsername(newUsername);
        });

    // 이메일 수정
    Optional.ofNullable(request.newEmail())
        .filter(newEmail -> !newEmail.equals(user.getEmail()))
        .ifPresent(newEmail -> {
          if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
          }
          user.changeEmail(newEmail);
        });

    // 비밀번호 수정
    Optional.ofNullable(request.newPassword())
        .ifPresent(user::changePassword);

    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    userRepository.delete(user);
  }
}