package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public UserDto create(@RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @ResponseBody
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @ResponseBody
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public UserDto findById(@PathVariable UUID userId) {
        return userService.findById(userId);
    }

    @ResponseBody
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public UserDto update(@PathVariable UUID userId,
                          @RequestBody UserUpdateRequest request) {
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                userId,
                request.userName(),
                request.email(),
                request.password(),
                request.statusMessage(),
                request.profileImage()
        );
        return userService.update(updateRequest);
    }

    @ResponseBody
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @ResponseBody
    @RequestMapping(value = "/{userId}/status", method = RequestMethod.PUT)
    public UserDto updateStatus(@PathVariable UUID userId,
                                @RequestBody UserStatusUpdateRequest request) {
        return userService.updateStatus(userId, request);
    }
}