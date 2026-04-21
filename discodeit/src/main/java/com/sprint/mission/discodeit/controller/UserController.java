package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.dto.UserStatusUpdateApiRequest;
import com.sprint.mission.discodeit.controller.dto.UserUpdateApiRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.service.dto.userstatus.UserStatusDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.GET)
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto create(
            @RequestPart("userCreateRequest") CreateUserRequest userCreateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        return userService.create(userCreateRequest, profile);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") UserUpdateApiRequest userUpdateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        return userService.update(userId, userUpdateRequest, profile);
    }

    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public UserStatusDto updateOnlineStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateApiRequest request
    ) {
        return userStatusService.updateByUserId(userId, request.newLastActiveAt());
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }
}
