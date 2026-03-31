package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitIdMismatchException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.io.IOException;
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

  // create (JSON)
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request)); // 201 Created
  }

  // create (multipart/form-data)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> createMultipart(
      @RequestParam String userName,
      @RequestParam String userEmail,
      @RequestParam String userPassword,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    String fileName = null;
    byte[] fileContent = null;
    String contentType = null;

    if (profile != null && !profile.isEmpty()) {
      try {
        fileName = profile.getOriginalFilename();
        fileContent = profile.getBytes();
        contentType = profile.getContentType();
      } catch (IOException e) {
        throw new DiscodeitInvalidInputException("profile 파일을 읽을 수 없습니다.");
      }
    }

    UserCreateRequest request = new UserCreateRequest(
        userName,
        userEmail,
        userPassword,
        fileName,
        fileContent,
        contentType
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  // update (multipart) - 프로필 사진 변경 가능
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> updateMultipart(
      @PathVariable UUID id,
      @RequestParam String userName,
      @RequestParam String userEmail,
      @RequestParam String userPassword,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    String fileName = null;
    byte[] fileContent = null;
    String contentType = null;

    if (profile != null && !profile.isEmpty()) {
      try {
        fileName = profile.getOriginalFilename();
        fileContent = profile.getBytes();
        contentType = profile.getContentType();
      } catch (IOException e) {
        throw new DiscodeitInvalidInputException("profile 파일을 읽을 수 없습니다.");
      }
    }

    UserUpdateRequest request = new UserUpdateRequest(
        id,
        userName,
        userEmail,
        userPassword,
        fileName,
        fileContent,
        contentType
    );

    userService.update(request);
    return ResponseEntity.ok(userService.read(id));
  }

  // update
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserResponse> update(@PathVariable UUID id,
      @Valid @RequestBody UserUpdateRequest request) {

    if (!id.equals(request.getId())) {
      throw DiscodeitIdMismatchException.generic("id", id, request.getId());
    }

    userService.update(new UserUpdateRequest(
        id,
        request.getUserName(),
        request.getUserEmail(),
        request.getUserPassword(),
        request.getFileName(),
        request.getFileContent(),
        request.getContentType()
    ));

    return ResponseEntity.ok(userService.read(id));
  }

  // delete
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
  }

  // read
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> read(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.read(id)); // 200 OK
  }

  // readAllDto
  @GetMapping
  public ResponseEntity<List<UserDto>> readAllDto() {
    return ResponseEntity.ok(userService.readAllDto()); // 200 OK
  }

  // UserStatusUpdate
  @PutMapping("/{id}/userStatus")
  public ResponseEntity<UserResponse> updateStatus(@PathVariable UUID id,
      @Valid @RequestBody UserStatusUpdateRequest request) {
    if (!id.equals(request.getUserId())) {
      throw DiscodeitIdMismatchException.generic("userId", id, request.getUserId());
    }

    userStatusService.update(new UserStatusUpdateRequest(id, request.getLastOnlineAt()));
    UserResponse updatedUserStatus = userService.read(id);

    return ResponseEntity.ok(userService.read(id));  // 200 OK
  }
}
