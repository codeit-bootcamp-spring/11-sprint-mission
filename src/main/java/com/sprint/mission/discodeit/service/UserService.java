package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserDto create(UserCreateRequest userCreateRequest, MultipartFile file);

  UserDto find(UUID userId);

  List<UserDto> findAll();

  UserDto updateUser(UUID userId, UserUpdateRequest userUpdateRequest, MultipartFile file);

  UserDto updateUserRole(UUID userId, User.Role role);

  boolean delete(UUID userId);


}
