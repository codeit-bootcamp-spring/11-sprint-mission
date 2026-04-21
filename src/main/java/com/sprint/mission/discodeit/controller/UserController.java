package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto registerUser(
      @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return userService.create(request, profile);
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UserDto updateUser(
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return userService.update(userId, request, profile);
  }

  @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨")
  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
  }

  @GetMapping
  public List<UserDto> getAllUsers() {
    return userService.findAll();
  }

  @PatchMapping(value = "/{userId}/userStatus")
  public UserStatusDto updateOnlineStatus(
      @PathVariable UUID userId, @RequestBody UserStatusUpdateRequest request) {
    return userStatusService.updateByUserId(userId, request);
  }
}
