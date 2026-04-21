package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserLoginRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto create(UserCreateRequest request, MultipartFile profile);

  UserDto findById(UUID id);

  List<UserDto> findAll();

  UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile);

  void delete(UUID id);

  UserDto login(UserLoginRequest request);
}