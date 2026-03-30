package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
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
  private final UserStatusService userStatusService;

  // create
  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(request)); // 201 Created
  }

  // read
  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> read(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.read(id)); // 200 OK
  }

  // readAll
  @GetMapping
  public ResponseEntity<List<UserResponse>> readAll() {
    return ResponseEntity.ok(userService.readAll());  // 200 OK
  }

  // update
  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(@PathVariable UUID id,
      @Valid @RequestBody UserUpdateRequest request) {
    userService.update(request);
    UserResponse updatedUser = userService.read(id);
    return ResponseEntity.ok(updatedUser);  // 200 OK
  }

  // delete
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
  }

  // restore
  @PostMapping("/{id}/restore")
  public ResponseEntity<Void> restore(@PathVariable UUID id) {
    userService.restore(id);
    return ResponseEntity.ok().build();
  }

  // UserStatusUpdate
  @PutMapping("/{id}/status")
  public ResponseEntity<UserResponse> updateStatus(@PathVariable UUID id,
      @Valid @RequestBody UserStatusUpdateRequest request) {
    userStatusService.update(request);
    UserResponse updatedUserStatus = userService.read(id);

    return ResponseEntity.ok(updatedUserStatus);  // 200 OK
  }

  // readAllDto
  @GetMapping("/readAllDto")
  public ResponseEntity<List<UserDto>> readAllDto() {
    return ResponseEntity.ok(userService.readAllDto()); // 200 OK
  }
}
