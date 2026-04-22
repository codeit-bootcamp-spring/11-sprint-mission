package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto createUser(UserCreateRequest request, MultipartFile profile);

  UserDto readUser(UUID id);

  List<UserDto> allReadUser();

  void deleteUser(UUID id);

  UserDto updateUser(UUID id, UserUpdateRequest request, MultipartFile profile);
}
