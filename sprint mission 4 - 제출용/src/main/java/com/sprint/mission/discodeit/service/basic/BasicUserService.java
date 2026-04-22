package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitDuplicateException;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(UserCreateRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw DiscodeitDuplicateException.user(request.getUsername());
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw DiscodeitDuplicateException.email(request.getEmail());
    }

    User user = new User(
        request.getUsername(),
        request.getEmail(),
        request.getPassword()
    );

    UserStatus userStatus = new UserStatus(user, Instant.now());
    user.setStatus(userStatus);

    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto find(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.user(id));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void update(UserUpdateRequest request) {
    User user = userRepository.findById(request.getId())
        .orElseThrow(() -> DiscodeitNotFoundException.user(request.getId()));

    String newUsername = request.getNewUsername();
    String newEmail = request.getNewEmail();
    String newPassword = request.getNewPassword();

    if (newUsername == null || newUsername.isBlank()) {
      newUsername = user.getUsername();
    }
    if (newEmail == null || newEmail.isBlank()) {
      newEmail = user.getEmail();
    }
    if (newPassword == null || newPassword.isBlank()) {
      newPassword = user.getPassword();
    }

    if (userRepository.existsByUsernameAndIdNot(newUsername, request.getId())) {
      throw DiscodeitDuplicateException.user(newUsername);
    }
    if (userRepository.existsByEmailAndIdNot(newEmail, request.getId())) {
      throw DiscodeitDuplicateException.email(newEmail);
    }

    user.updateUser(newUsername, newEmail, newPassword);
    // 변경감지
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> DiscodeitNotFoundException.user(id));

    // cascade delete - 일괄 삭제
    userRepository.delete(user);
  }
}
