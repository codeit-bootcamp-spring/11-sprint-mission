package com.sprint.mission.discodeit.control;


import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
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

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
    UserDto createdUser = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @RequestMapping(value = "/findAll", method = RequestMethod.GET)
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.allReadUser();
    return ResponseEntity.ok(users);
  }

  @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
  public ResponseEntity<UserStatus> getUserStatus(@PathVariable UUID id) {
    UserStatus status = userStatusService.findUserStatus(id);
    return ResponseEntity.ok(status);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @RequestParam String password) {
    userService.deleteUser(id, password);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public ResponseEntity<Void> updateUser(@PathVariable UUID id,
      @RequestBody UserUpdateRequest request) {
    userService.updateUser(id, request);
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
  public ResponseEntity<UserStatus> updateOnlineStatus(@PathVariable UUID id) {
    UserStatus updatedStatus = userStatusService.updateUserIdStatus(id);
    return ResponseEntity.ok(updatedStatus);
  }
}
