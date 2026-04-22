package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userdto.*;

import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto create(UserCreateRequest userCreateRequest, MultipartFile file);

  UserDto find(UUID userId);

  List<UserDto> findAll();

  UserDto updateUser(UUID userId, UpdateUserDto updateUserDto, MultipartFile file);

  boolean delete(UUID userId);


}
