package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.userstatusdto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusDto;

import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {


  private final UserService userService;
  private final UserStatusService userStatusService;

  @PostMapping(consumes = "multipart/form-data")
  @ApiResponse(description = "유저 생성", responseCode = "201")
  public ResponseEntity<UserDto> createUser(@Valid @RequestPart UserCreateRequest userCreateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    UserDto userInfo = userService.create(userCreateRequest, profile);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(userInfo.id())
        .toUri();

    return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);

  }


  @GetMapping(value = "/{userId}")
  @ApiResponse(description = "유저 1명 정보 가져오기", responseCode = "200")
  public ResponseEntity<UserDto> readUser(@PathVariable("userId") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> readAllUser() {

    return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
  }


  @ApiResponse(responseCode = "200")
  @PatchMapping(value = "/{userId}", consumes = "multipart/form-data")
  public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
      @Valid @RequestPart UserUpdateRequest userUpdateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    UserDto userInfo = userService.updateUser(userId, userUpdateRequest, profile);
    return ResponseEntity.status(HttpStatus.OK).body(userInfo);
  }


  //유저 상태 업데이트
  @ApiResponse(responseCode = "200")
  @PatchMapping(value = "/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateUserStatus(@PathVariable UUID userId,
      @Valid @RequestBody UserStatusUpdateRequest userStatusUpdateRequest) {

    userStatusService.update(userId, userStatusUpdateRequest);

    return ResponseEntity.status(HttpStatus.OK).body(userStatusService.find(userId));

  }

  //멤버 삭제
  @ApiResponse(responseCode = "204", description = "멤버 삭제")
  @DeleteMapping(value = "/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }


}
