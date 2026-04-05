package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @PostMapping
  public UserDto registerUser(@RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return userService.create(request, profile);
  }

  @PatchMapping("/{userId}")
  public void updateUser(
      @PathVariable UUID userId, @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    userService.update(userId, request, profile);
  }

  @DeleteMapping("/{userId}")
  public void deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.findAll();
    return ResponseEntity.ok(users);
  }

  @PatchMapping(value = "/{userId}/userStatus")
  public void updateOnlineStatus(
      @PathVariable UUID userId, @RequestBody UserStatusUpdateRequest request) {
    userStatusService.updateByUserId(userId, request);
  }
}
