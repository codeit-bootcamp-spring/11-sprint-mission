package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> create(
            @RequestPart("userCreateRequest") @Valid UserCreateRequest dto,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.debug("User create request received. username={}, email={}, hasProfile={}",
                dto.username(), dto.email(), profile != null && !profile.isEmpty());
        UserDto result = userService.create(dto, profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") @Valid UserUpdateRequest dto,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.debug("User update request received. userId={}, hasProfile={}",
                userId, profile != null && !profile.isEmpty());
        userService.update(userId, dto, profile);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId
    ) {
        log.debug("User delete request received. userId={}", userId);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> result = userService.findAll();
        return ResponseEntity.ok(result);
    }

    @PatchMapping(value = "/{userId}/userStatus")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest dto
    ) {
        userStatusService.updateByUserId(userId, dto);
        return ResponseEntity.ok().build();
    }
}
