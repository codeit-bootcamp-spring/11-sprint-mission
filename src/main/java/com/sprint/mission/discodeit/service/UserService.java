package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  User createUser(UserCreateRequest request);

  UserDto readUser(UUID id);

  List<UserDto> allReadUser();

  void deleteUser(UUID id);

  void updateUser(UUID id, UserUpdateRequest request, MultipartFile profile);
}
