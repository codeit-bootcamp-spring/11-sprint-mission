package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  // 코드 탬플릿에 맞게 create 메서드 수정
//    User create(String name, String email, String password);
  User create(UserCreateRequest dto, MultipartFile profile);

  UserDto find(UUID id);

  List<UserDto> findAll();

  User update(UUID id, UserUpdateRequest dto, MultipartFile profile);

  void delete(UUID id);
}
