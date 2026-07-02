package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
public interface UserApi {

  @Operation(summary = "User 등록")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username을 사용하는 User가 이미 존재함")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<UserDto> create(
      @Parameter(description = "User 생성 정보")
      @RequestPart("userCreateRequest") UserCreateRequest dto,
      @Parameter(description = "User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile);

  @Operation(summary = "User 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username을 사용하는 User가 이미 존재함"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<UserDto> update(
      @Parameter(description = "수정할 User ID")
      @PathVariable("userId") UUID id,
      @Parameter(description = "수정할 User 정보")
      @RequestPart("userUpdateRequest") UserUpdateRequest dto,
      @Parameter(description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile);

  @Operation(summary = "User 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID")
      @PathVariable("userId") UUID id);

  @Operation(summary = "전체 User 목록 조회")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<UserDto>> findAll();

}
