package com.sprint.mission.discodeit.control;


import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<UserDto> createUser(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    UserDto createdUser = userService.createUser(request, profile);
    URI location = URI.create("/api/users/" + createdUser.id());
    return ResponseEntity.created(location).body(createdUser);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.allReadUser();
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    UserDto updatedUser = userService.updateUser(id, request, profile);
    return ResponseEntity.ok(updatedUser);
  }

  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateOnlineStatus(
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateDto request
  ) {
    UserStatusDto updatedStatus = userStatusService.updateUserIdStatus(userId, request);
    return ResponseEntity.ok(updatedStatus);
  }
}
