package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
    BinaryContentDto.CreateRequest profileImageRequest = convertToProfileImageDto(profileImage);

    UserDto.Response response = userService.create(request, profileImageRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto.Response> update(
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserDto.UpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profileImage) {
    BinaryContentDto.CreateRequest profileImageRequest = convertToProfileImageDto(profileImage);

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

  // 파일을 DTO 형태로 변환
  private BinaryContentDto.CreateRequest convertToProfileImageDto(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    try {
      return BinaryContentDto.CreateRequest.builder()
          .fileName(file.getOriginalFilename())
          .size(file.getSize())
          .contentType(file.getContentType())
          .bytes(file.getBytes())
          .build();
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_READ_FAILED);
    }
  }
}
