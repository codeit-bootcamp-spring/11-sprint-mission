package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UpdateUserStatusDto;
import com.sprint.mission.discodeit.dto.userstatusdto.UserStatusInfoDto;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.exception.service.DupEmailException;
import com.sprint.mission.discodeit.exception.service.DupNameException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.Binder;
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
  public ResponseEntity<CreatedUserDto> createUser(@RequestPart CreateUserDto userCreateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    CreatedUserDto userInfo = userService.create(userCreateRequest, profile);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(userInfo.id())
        .toUri();

    return ResponseEntity.status(HttpStatus.CREATED).body(userInfo);

  }


  @GetMapping(value = "/{userId}")
  @ApiResponse(description = "유저 1명 정보 가져오기", responseCode = "200")
  public ResponseEntity<CreatedUserDto> readUser(@PathVariable("userId") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK).body(userService.find(userId));
  }

  @GetMapping
  public ResponseEntity<List<UserInfoDto>> readAllUser() {

    return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
  }


  @ApiResponse(responseCode = "200")
  @PatchMapping(value = "/{userId}", consumes = "multipart/form-data")
  public ResponseEntity<CreatedUserDto> updateUser(@PathVariable UUID userId,
      @RequestPart UpdateUserDto userUpdateRequest,
      @RequestPart(required = false) MultipartFile profile) {

    CreatedUserDto userInfo = userService.updateUser(userId, userUpdateRequest, profile);
    return ResponseEntity.status(HttpStatus.OK).body(userInfo);
  }


  //유저 상태 업데이트
  @ApiResponse(responseCode = "200")
  @PatchMapping(value = "/{userId}/userStatus")
  public ResponseEntity<UserStatusInfoDto> updateUserStatus(@PathVariable UUID userId,
      @RequestBody UpdateUserStatusDto updateUserStatusDto) {

    userStatusService.update(userId, updateUserStatusDto);

    return ResponseEntity.status(HttpStatus.OK).body(userStatusService.find(userId));

  }

  //멤버 삭제
  @ApiResponse(responseCode = "204", description = "멤버 삭제")
  @DeleteMapping(value = "/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  //----------Exception handler-----------------------

  @ExceptionHandler(DiffPasswordException.class)
  public ResponseEntity<ExceptionDto> diffPasswordHandler(DiffPasswordException e,
      HttpServletRequest request) {

    ExceptionDto exceptionDto = ExceptionDto.of(
        HttpStatus.UNAUTHORIZED,
        e.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(401).body(exceptionDto);

  }

  @ExceptionHandler(DupNameException.class)
  public ResponseEntity<ExceptionDto> dupNameHandler(DupNameException e,
      HttpServletRequest request) {

    ExceptionDto exceptionDto = ExceptionDto.of(
        HttpStatus.CONFLICT,
        e.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(409).body(exceptionDto);
  }


  @ExceptionHandler(DupEmailException.class)
  public ResponseEntity<ExceptionDto> dupEmailHandler(DupEmailException e,
      HttpServletRequest request) {

    ExceptionDto exceptionDto = ExceptionDto.of(
        HttpStatus.CONFLICT,
        e.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(409).body(exceptionDto);

  }


}
