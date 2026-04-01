package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitIdMismatchException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

  // readAllDto
  @GetMapping
  public ResponseEntity<List<UserDto>> readAllDto() {
    return ResponseEntity.ok(userService.readAllDto()); // 200 OK
  }

  // create (JSON)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request)); // 201 Created
  }

  // create (multipart/form-data)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> createMultipart(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request)); // 201 Created
  }

  // update (multipart) - 프로필 사진 변경 가능
  // PATCH /api/users/{userId} + multipart
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> updateMultipart(
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    if (request.getId() != null && !userId.equals(request.getId())) {
      throw DiscodeitIdMismatchException.generic("id", userId, request.getId());
    }

    userService.update(new UserUpdateRequest(
        userId,
        request.getNewUsername(),
        request.getNewEmail(),
        request.getNewPassword()
    ));
    return ResponseEntity.ok(userService.read(userId)); // 200 OK
  }

  // update
  @PatchMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> update(
      @PathVariable UUID userId,
      @RequestBody UserUpdateRequest request
  ) {
    if (request.getId() != null && !userId.equals(request.getId())) {
      throw DiscodeitIdMismatchException.generic("id", userId, request.getId());
    }

    userService.update(new UserUpdateRequest(
        userId,
        request.getNewUsername(),
        request.getNewEmail(),
        request.getNewPassword()
    ));
    return ResponseEntity.ok(userService.read(userId)); // 200 OK
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();  // 204 No Content
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponse> read(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.read(userId)); // 200 OK
  }

  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserResponse> updateStatus(
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  ) {
    userStatusService.update(new UserStatusUpdateRequest(userId, request.getNewLastActiveAt()));
    return ResponseEntity.ok(userService.read(userId)); // 200 OK
  }
}
