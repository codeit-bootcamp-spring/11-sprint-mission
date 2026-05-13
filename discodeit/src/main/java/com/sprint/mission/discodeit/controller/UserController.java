package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.util.MultipartFileUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
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

@Slf4j
@RequestMapping("/api/users")
@RequiredArgsConstructor
@RestController
public class UserController implements UserApi {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> create(
      @Valid @RequestPart(value = "userCreateRequest") UserCreateRequest userCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.info("user create request: request={}, profile={}", userCreateRequest, profile != null);
    Optional<BinaryContentCreateRequest> profileRequest = MultipartFileUtil.toCreateRequest(
        profile);

    UserResponse createdUser = this.userService.createUser(userCreateRequest, profileRequest);

    log.debug("user create response: {}", createdUser);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdUser);
  }

  @PatchMapping(path = "{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> update(
      @PathVariable UUID userId,
      @Valid @RequestPart(value = "userUpdateRequest", required = false) UserUpdateRequest userUpdateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.info("user update request: id={}, request={}, profile={}", userId, userUpdateRequest, profile != null);
    Optional<BinaryContentCreateRequest> profileRequest = MultipartFileUtil.toCreateRequest(
        profile);

    UserResponse updatedUser = this.userService.updateUser(userId, userUpdateRequest,
        profileRequest);

    log.debug("user update response: {}", updatedUser);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUser);
  }

  @DeleteMapping(path = "{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    log.info("user delete request: id={}", userId);
    this.userService.deleteUser(userId);

    log.debug("user delete response: no-content");
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> findAll() {
    log.info("user find-all request");
    List<UserResponse> users = this.userService.findAll();

    log.debug("user find-all response: count={}", users.size());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(users);
  }

  @PatchMapping(path = "{userId}/user-status")
  public ResponseEntity<UserStatusResponse> updateUserStatus(
      @PathVariable UUID userId,
      @Valid @RequestBody UserStatusUpdateRequest userStatusUpdateRequest
  ) {
    log.info("user-status update-user-status request: id={}, request={}", userId, userStatusUpdateRequest);
    UserStatusResponse updatedUserStatus = this.userStatusService.updateUserStatusByUserId(userId,
        userStatusUpdateRequest);

    log.debug("user-status update-user-status response: {}", updatedUserStatus);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUserStatus);
  }
}
