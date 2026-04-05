package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateParam;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    public UserController(UserService userService, UserStatusService userStatusService) {
        this.userService = userService;
        this.userStatusService = userStatusService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public UserDto create(@RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public UserDto update(@PathVariable UUID userId,
                          @RequestBody UserUpdateRequest request) {
        return userService.update(new UserUpdateParam(userId, request));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @RequestMapping(value = "/{userId}/status", method = RequestMethod.PUT)
    public UserStatusDto updateStatus(@PathVariable UUID userId,
                                      @RequestBody UserStatusUpdateRequest request) {
        return userStatusService.updateByUserId(userId, request);
    }

    @RequestMapping(value = "/api/user/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAllForPage() {
        return ResponseEntity.ok(userService.findAll());
    }
}