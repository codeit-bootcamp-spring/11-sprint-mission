package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto registerUser(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @Valid @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.debug("POST /api/users 요청 - username: {}", request.username());
    UserDto response = userService.create(request, profile);
    log.info("POST /api/users 정상 처리 완료 - userId: {}", response.id());
    return response;
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UserDto updateUser(
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @Valid @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.debug("PATCH /api/users/{} 요청", userId);
    UserDto response = userService.update(userId, request, profile);
    log.info("PATCH /api/users/{} 정상 처리 완료", userId);
    return response;
  }

  @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨")
  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable UUID userId) {
    log.debug("DELETE /api/users/{} 요청", userId);
    userService.delete(userId);
    log.info("DELETE /api/users/{} 정상 처리 완료", userId);
  }

  @GetMapping
  public List<UserDto> getAllUsers() {
    return userService.findAll();
  }

  @PatchMapping(value = "/{userId}/userStatus")
  public UserStatusDto updateOnlineStatus(
      @PathVariable UUID userId, @RequestBody UserStatusUpdateRequest request) {
    return userStatusService.updateByUserId(userId, request);
  }
}
