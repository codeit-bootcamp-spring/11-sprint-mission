package com.sprint.mission.discodeit.control;


import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserControl {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateRequest request) {
        UserDto createdUser = userService.createUser(request);
        userStatusService.createUserStatus(new UserStatusCreateDto(createdUser.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserResponseWithStatus>> getAllUsers() {
        List<UserDto> users = userService.allReadUser();
        List<UserStatus> statuses = userStatusService.findAllUserStatus();

        List<UserResponseWithStatus> response = users.stream().map(user -> {
            UserStatus status = statuses.stream()
                    .filter(s -> s.getUserId().equals(user.id()))
                    .findFirst()
                    .orElse(null);
            return new UserResponseWithStatus(user, status);
        }).toList();

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}/status", method = RequestMethod.GET)
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable UUID id) {
        UserStatus status = userStatusService.findUserStatus(id);
        return ResponseEntity.ok(status);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @RequestParam String password) {
        userStatusService.deleteUserStatus(id);
        userService.deleteUser(id, password);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatus> updateOnlineStatus(@PathVariable UUID id, @RequestBody UserStatusUpdateDto request) {
        UserStatus updatedStatus = userStatusService.updateUserIdStatus(request);
        return ResponseEntity.ok(updatedStatus);
    }
}
