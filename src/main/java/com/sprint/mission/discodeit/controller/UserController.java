package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusByUserIdResponseDTO;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAllUserDtos());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> create(
            @RequestPart("userCreateRequest") @Valid UserCreateRequest request,
            @RequestPart(value = "profileId", required = false) MultipartFile profile
    ) {
        User createdUser = userService.createUser(request, profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") @Valid UserUpdateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        User updatedUser = userService.updateUser(userId, request, profile);
        return ResponseEntity.ok(updatedUser);
    }

    // 여기 추후 리팩토링 필요
    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserStatus> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserStatus updatedStatus = userStatusService.updateByUserId(userId).userStatus();
        return ResponseEntity.ok(updatedStatus);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
