// UserController.java
package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitIdMismatchException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;
  private final BinaryContentService binaryContentService;

  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public ResponseEntity<UserDto> createMultipart(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) throws IOException {
    UserDto createdUser = userService.create(request);

    if (profile != null && !profile.isEmpty()) {
      if (profile.getOriginalFilename() == null || profile.getOriginalFilename().isBlank()) {
        throw DiscodeitInvalidInputException.blankField("fileName");
      }

      binaryContentService.create(new BinaryContentCreateRequest(
          createdUser.id(),
          null,
          profile.getOriginalFilename(),
          profile.getBytes(),
          profile.getContentType()
      ));
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.find(createdUser.id()));
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public ResponseEntity<UserDto> updateMultipart(
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) throws IOException {
    if (request.getId() != null && !userId.equals(request.getId())) {
      throw DiscodeitIdMismatchException.generic("id", userId, request.getId());
    }

    userService.update(new UserUpdateRequest(
        userId,
        request.getNewUsername(),
        request.getNewEmail(),
        request.getNewPassword()
    ));

    if (profile != null && !profile.isEmpty()) {
      if (profile.getOriginalFilename() == null || profile.getOriginalFilename().isBlank()) {
        throw DiscodeitInvalidInputException.blankField("fileName");
      }

      binaryContentService.create(new BinaryContentCreateRequest(
          userId,
          null,
          profile.getOriginalFilename(),
          profile.getBytes(),
          profile.getContentType()
      ));
    }

    return ResponseEntity.ok(userService.find(userId));
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDto> update(
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

    return ResponseEntity.ok(userService.find(userId));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> find(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.find(userId));
  }

  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserDto> updateStatus(
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  ) {
    userStatusService.update(new UserStatusUpdateRequest(userId, request.getNewLastActiveAt()));
    return ResponseEntity.ok(userService.find(userId));
  }
}
