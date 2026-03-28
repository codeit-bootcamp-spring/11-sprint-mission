package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserResponse> create(
            @RequestPart(value = "userCreateRequest") UserCreateRequest userCreateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        Optional<BinaryContentCreateRequest> profileRequest = BinaryContentCreateRequest.from(profile);

        UserResponse createdUser = this.userService.createUser(userCreateRequest, profileRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @RequestMapping(
            path = "{userId}",
            method = RequestMethod.PATCH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @RequestPart(value = "userUpdateRequest", required = false) UserUpdateRequest userUpdateRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        Optional<BinaryContentCreateRequest> profileRequest = BinaryContentCreateRequest.from(profile);

        UserResponse updatedUser = this.userService.updateUser(userId, userUpdateRequest, profileRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedUser);
    }

    @RequestMapping(
            path = "{userId}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId
    ) {
        this.userService.deleteUser(userId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @RequestMapping(
            method = RequestMethod.GET
    )
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = this.userService.findAll();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(users);
    }

    @RequestMapping(
            path = "{userId}/user-status",
            method = RequestMethod.PATCH
    )
    public ResponseEntity<UserStatusResponse> updateUserStatus(
            @PathVariable UUID userId
    ) {
        UserStatusResponse updatedUserStatus = this.userStatusService.updateUserStatusByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedUserStatus);
    }
}
