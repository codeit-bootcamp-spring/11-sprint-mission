package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserReadDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController // Json 반환을 위해 @Controller 대신 @ResponseBody를 포함한 @RestController 사용
@RequestMapping(EndPoints.USER)
@Tag(name = "User", description = "User API")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService; // 특정 유저 생성, 모든 유저 조회, 특정 유저 업데이트, 특정 유저 삭제
  private final UserStatusService userStatusService; // 특정 유저의 상태 업데이트

  // 특정 사용자 등록
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "User 등록")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username을 사용하는 User가 이미 존재함")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<User> create(
      @Parameter(description = "User 생성 정보")
      @RequestPart("userCreateRequest") UserCreateRequest dto,
      @Parameter(description = "User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto, profile));
  }

  // 특정 사용자 정보 수정
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "User 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username을 사용하는 User가 이미 존재함"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<User> update(
      @Parameter(description = "수정할 User ID")
      @PathVariable("userId") UUID id,
      @Parameter(description = "수정할 User 정보")
      @RequestPart("userUpdateRequest") UserUpdateRequest dto,
      @Parameter(description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return ResponseEntity.ok(userService.update(id, dto, profile));
  }

  // 특정 사용자 삭제
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "User 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID")
      @PathVariable("userId") UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // 모든 사용자 조회, 심화 요구사항 추가
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "전체 User 목록 조회")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<UserReadDto>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }
//    @RequestMapping(method = RequestMethod.GET)
//    public ResponseEntity<List<UserReadDto>> readAll() {
//        return ResponseEntity.ok(userService.findAll());
//    }

  // 특정 사용자의 상태(온/오프라인) 업데이트
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "User 온라인 상태 업데이트")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 온라인 상태가 성공적으로 업데이트됨"),
      @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음")
  })
  @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
  public ResponseEntity<UserStatus> updateStatus(
      @Parameter(description = "상태를 변경할 User ID")
      @PathVariable("userId") UUID id) {
    return ResponseEntity.ok(userStatusService.updateByUserId(id));
  }


}
