package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.USER)
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService; // 특정 유저 생성, 모든 유저 조회, 특정 유저 업데이트, 특정 유저 삭제

  // 특정 사용자 등록
  @Override
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest dto,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.info("[USER_CREATE_REQUEST] 유저 생성 요청 - 유저 이름={}, 유저 이메일={}", dto.username(), dto.email());
    UserDto user = userService.create(dto, profile);
    log.info("[USER_CREATE_RESPONSE] 유저 생성 응답 - 유저 ID={}", user.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  // 특정 사용자 정보 수정
  @Override
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @PathVariable("userId") UUID id,
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest dto,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    log.info("[USER_UPDATE_REQUEST] 유저 수정 요청 - 유저 ID={}", id);
    UserDto user = userService.update(id, dto, profile);
    log.info("[USER_UPDATE_RESPONSE] 유저 수정 응답 - 유저 ID={}", id);
    return ResponseEntity.ok(user);
  }

  // 특정 사용자 삭제
  @Override
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
      @PathVariable("userId") UUID id) {
    log.info("[USER_DELETE_REQUEST] 유저 삭제 요청 - 유저 ID={}", id);
    userService.delete(id);
    log.info("[USER_DELETE_RESPONSE] 유저 삭제 응답 - 유저 ID={}", id);
    return ResponseEntity.noContent().build();
  }

  // 모든 사용자 조회, 심화 요구사항 추가
  @Override
  @ResponseStatus(HttpStatus.OK)
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

}
