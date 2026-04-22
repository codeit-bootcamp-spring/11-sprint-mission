package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto.Response> create(
      @Valid @RequestPart("userCreateRequest") UserDto.CreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profileImage) {

    BinaryContentDto.CreateRequest profileImageRequest = BinaryContentDto.CreateRequest.of(
        profileImage);
    UserDto.Response response = userService.create(request, profileImageRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto.Response> update(
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserDto.UpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profileImage) {

    BinaryContentDto.CreateRequest profileImageRequest = BinaryContentDto.CreateRequest.of(
        profileImage);
    UserDto.Response response = userService.update(userId, request, profileImageRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping(path = "/{userId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<UserDto.Response>> findAll() {
    List<UserDto.Response> responseList = userService.findAll();
    return ResponseEntity.ok(responseList);
  }

  @PatchMapping(path = "/{userId}/userStatus")
  public ResponseEntity<UserStatusDto.Response> updateUserStatus(
      @PathVariable UUID userId,
      @RequestBody @Valid UserStatusDto.UpdateRequest request) {

    UserStatusDto.Response response = userStatusService.updateByUserId(userId, request);
    return ResponseEntity.ok(response);
  }
}
