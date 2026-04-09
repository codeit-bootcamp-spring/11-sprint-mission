package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.BusinessLogicException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  @Transactional
  public User createUser(UserCreateRequest request, MultipartFile profile) {
    String name = request.username().trim();
    String email = request.email().trim();
    String password = request.password().trim();
    if (userRepository.findByUsername(name).isPresent()) {
      throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);
    }

    BinaryContent profileEntity = null;
    if (profile != null && !profile.isEmpty()) {
      try {
        profileEntity = new BinaryContent(profile.getBytes(), profile.getOriginalFilename(),
            profile.getContentType());
        binaryContentRepository.save(profileEntity);
      } catch (Exception e) {
        throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
      }
    }

    User newUser = new User(name, email, password, profileEntity);
    UserStatus newStatus = new UserStatus(newUser);

    newUser.initStatus(newStatus);
    return userRepository.save(newUser);
  }

  @Override
  public UserDto readUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    return UserDto.from(user, user.getStatus());
  }

  @Override
  public List<UserDto> allReadUser() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> UserDto.from(user, user.getStatus()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    BinaryContent profile = user.getProfile();
    user.setProfile(null);
    userRepository.save(user);
    if (profile != null) {
      binaryContentRepository.delete(profile);
    }
    userRepository.delete(user);
  }

  @Override
  @Transactional
  public void updateUser(UUID id, UserUpdateRequest request, MultipartFile profile) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    String name = (request.newUsername() != null) ? request.newUsername() : user.getUsername();
    if (request.newUsername() != null && !name.equals(user.getUsername())) {
      userRepository.findByUsername(name)
          .ifPresent(u -> {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
          });
    }
    String email = (request.newEmail() != null) ? request.newEmail() : user.getEmail();
    if (request.newEmail() != null && !email.equals(user.getEmail())) {
      userRepository.findByEmail(email)
          .ifPresent(u -> {
            throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);
          });
    }

    String password = (request.newPassword() != null) ? request.newPassword() : user.getPassword();

    BinaryContent currentProfile = user.getProfile();
    if (profile != null && !profile.isEmpty()) {
      if (currentProfile != null) {
        binaryContentRepository.delete(currentProfile);
      }
      try {
        currentProfile = new BinaryContent(
            profile.getBytes(),
            profile.getOriginalFilename(),
            profile.getContentType()
        );
        binaryContentRepository.save(currentProfile);
      } catch (IOException e) {
        throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
      }
    }

    user.update(name, email, password, currentProfile);
  }
}
