package com.sprint.mission.discodeit.control;


import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final RememberMeServices rememberMeServices;

  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<UserDto> createUser(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.info("사용자 생성 요청 수신 - username: {}, email: {}, hasProfile: {}",
        request.username(), request.email(), profile != null && !profile.isEmpty());
    UserDto createdUser = userService.createUser(request, profile);
    URI location = URI.create("/api/users/" + createdUser.id());
    log.info("사용자 생성 응답 완료 - userId: {}, location: {}", createdUser.id(), location);
    return ResponseEntity.created(location).body(createdUser);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.allReadUser();
    return ResponseEntity.ok(users);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    log.info("사용자 삭제 요청 수신 - userId: {}", id);
    userService.deleteUser(id);
    log.info("사용자 삭제 응답 완료 - userId: {}", id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID id,
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    log.info("사용자 수정 요청 수신 - userId: {}, hasProfile: {}", id,
        profile != null && !profile.isEmpty());
    UserDto updatedUser = userService.updateUser(id, request, profile);

    if (request.newPassword() != null) {
      rememberMeServices.loginFail(httpRequest, httpResponse);
    }

    log.info("사용자 수정 응답 완료 - userId: {}", id);
    return ResponseEntity.ok(updatedUser);
  }
}
