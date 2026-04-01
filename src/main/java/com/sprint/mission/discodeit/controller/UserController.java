package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    public UserController(UserService userService, UserStatusService userStatusService) {
        this.userService = userService;
        this.userStatusService = userStatusService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public UserDto registerUser(@RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public void updateUser(
            @PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        userService.update(userId, request);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void  deleteUser(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @RequestMapping(value = "/{userId}/online-status", method = RequestMethod.PUT)
    public void updateOnlineStatus(
            @PathVariable UUID userId, @RequestBody UserStatusUpdateRequest request) {
        userStatusService.updateByUserId(userId);
    }
}
