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
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public User createUser(UserCreateRequest request) {
    String name = request.username().trim();
    String email = request.email().trim();
    String password = request.password().trim();
    if (userRepository.findByUserName(name).isPresent()) {
      throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
    }
    if (userRepository.findByEmail(email).isPresent()) {
      throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);
    }
    User newUser = new User(name, email, password, null);
    UserStatus newStatus = new UserStatus(newUser.getId());
    try {
      userRepository.save(newUser);
      userStatusRepository.save(newStatus);
    } catch (Exception e) {
      throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
    }

    return newUser;
  }

  @Override
  public UserDto readUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    UserStatus status = userStatusRepository.findByUserId(user.getId())
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_STATUS_NOT_FOUND));

    return UserDto.from(user, status);
  }

  @Override
  public List<UserDto> allReadUser() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> {
          UserStatus status = userStatusRepository.findByUserId(user.getId())
              .orElseGet(() -> new UserStatus(user.getId()));
          return UserDto.from(user, status);
        })
        .collect(Collectors.toList());
  }

  @Override
  public void deleteUser(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

    if (user.getProfileId() != null) {
      binaryContentRepository.delete(user.getProfileId());
    }
    userStatusRepository.deleteByUserId(id);
    userRepository.delete(id);
  }

  @Override
  public void updateUser(UUID id, UserUpdateRequest request, MultipartFile profile) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    String name = user.getUsername();
    if (request.newUsername() != null) {
      userRepository.findByUserName(request.newUsername())
          .filter(u -> !u.getId().equals(id))
          .ifPresent(u -> {
            throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
          });
      name = request.newUsername();
    }
    String email = user.getEmail();
    if (request.newEmail() != null) {
      userRepository.findByEmail(request.newEmail())
          .filter(u -> !u.getId().equals(id))
          .ifPresent(u -> {
            throw new BusinessLogicException(ExceptionCode.EMAIL_EXISTS);
          });
      email = request.newEmail();
    }

    String password = (request.newPassword() != null) ? request.newPassword() : user.getPassword();

    if (profile != null && !profile.isEmpty()) {
      if (user.getProfileId() != null) {
        binaryContentRepository.delete(user.getProfileId());
      }
      try {
        BinaryContent newProfile = new BinaryContent(
            profile.getBytes(),
            profile.getOriginalFilename(),
            profile.getContentType()
        );
        binaryContentRepository.save(newProfile);
        user.setProfileId(newProfile.getId());
      } catch (IOException e) {
        throw new BusinessLogicException(ExceptionCode.INTERNAL_SERVER_ERROR);
      }
    }

    user.update(name, email, password);
    userRepository.save(user);
  }
}
