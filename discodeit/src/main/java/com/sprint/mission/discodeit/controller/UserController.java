package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.dto.UserStatusUpdateApiRequest;
import com.sprint.mission.discodeit.controller.dto.UserUpdateApiRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.service.dto.userstatus.UserStatusDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto create(
            @Valid @RequestPart("userCreateRequest") CreateUserRequest userCreateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.debug("사용자 생성 요청: username={}", userCreateRequest.username());
        return userService.create(userCreateRequest, profile);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto update(
            @PathVariable UUID userId,
            @Valid @RequestPart("userUpdateRequest") UserUpdateApiRequest userUpdateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.debug("사용자 수정 요청: userId={}", userId);
        return userService.update(userId, userUpdateRequest, profile);
    }

    @PatchMapping("/{userId}/userStatus")
    public UserStatusDto updateOnlineStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateApiRequest request
    ) {
        return userStatusService.updateByUserId(userId, request.newLastActiveAt());
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID userId) {
        log.debug("사용자 삭제 요청: userId={}", userId);
        userService.delete(userId);
    }
}
