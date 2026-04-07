package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequestDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserResponseDto;
import com.sprint.mission.discodeit.dto.UserStatusResponseDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequestDto;
import com.sprint.mission.discodeit.service.UserService;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @GetMapping
  public ResponseEntity<List<UserDto>> findAllUsers() {
    List<UserDto> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  // 1. 사용자 등록
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponseDto> createUser(
      @RequestPart("userCreateRequest") UserCreateRequestDto requestDto,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    try {
      if (profile != null) {
        requestDto.setProfileImageContent(profile.getBytes());
        requestDto.setProfileImageContentType(profile.getContentType());
      }
      UserResponseDto response = userService.create(requestDto);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // 2. 사용자 정보 수정
  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateUser(
      @PathVariable UUID id,
      @RequestBody UserUpdateRequestDto updateRequestDto) {
    try {
      UserResponseDto response = userService.update(id, updateRequestDto);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 3. 사용자 삭제
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/userStatus")
  public ResponseEntity<UserStatusResponseDto> updateUserStatus(
      @PathVariable UUID id,
      @RequestBody UserStatusResponseDto dto) {
    try {
      UserStatusResponseDto response = userService.updateOnlineStatus(id, dto);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 5. 사용자의 온라인 상태 업데이트
  @PatchMapping("/{id}/online")
  public ResponseEntity<UserStatusResponseDto> updateOnlineStatus(
      @PathVariable UUID id,
      @RequestBody UserStatusResponseDto statusUpdateRequestDto) {
    try {
      UserStatusResponseDto response = userService.updateOnlineStatus(id, statusUpdateRequestDto);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
