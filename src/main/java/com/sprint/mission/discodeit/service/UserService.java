package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userdto.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  CreatedUserDto create(CreateUserDto createUserDTO, MultipartFile file);

  CreatedUserDto find(UUID userId);

  List<UserInfoDto> findAll();

  CreatedUserDto updateUser(UUID userId, UpdateUserDto updateUserDto, MultipartFile file);

  boolean delete(UUID userId);


}
