package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> create(@RequestBody UserCreateRequest request) {
    UserDto result = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.findById(userId));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<UserDto> update(@PathVariable UUID userId,
      @RequestBody UserUpdateRequest request) {
    UserUpdateRequest updateRequest = new UserUpdateRequest(
        userId,
        request.userName(),
        request.email(),
        request.password(),
        request.statusMessage(),
        request.profileImage()
    );
    return ResponseEntity.ok(userService.update(updateRequest));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build(); // 204 반환
  }
  
  @PatchMapping("/{userId}/status")
  public ResponseEntity<UserDto> updateStatus(@PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request) {
    return ResponseEntity.ok(userService.updateStatus(userId, request));
  }
}