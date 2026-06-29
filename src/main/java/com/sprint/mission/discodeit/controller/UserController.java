package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Operation(summary = "User 등록", operationId = "create")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @Valid
      @RequestPart("userCreateRequest") UserCreateRequest dto,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    UserDto userDto = userService.create(dto,
        BinaryContentCreateRequest.resolveAttachmentRequest(profile));

    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }

  @Operation(summary = "User 정보 수정", operationId = "update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(@Valid @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest dto,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    UserDto userDto = userService.update(userId, dto,
        BinaryContentCreateRequest.resolveAttachmentRequest(profile));

    return ResponseEntity.ok(userDto);

  }

  @Operation(summary = "User 삭제", operationId = "delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
  })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  // 단건 조회 추가

  @Operation(summary = "전체 User 목록 조회", operationId = "findAll")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> userDtoList = userService.findAll();
    return ResponseEntity.ok(userDtoList);
  }

  // create()에서 파일을 받아 BinaryContentCreateRequest에 담아주는 메서드
  // Optional 말고 @Nullable 사용 고려
//  private Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile multipartFile) {
//    if (multipartFile == null || multipartFile.isEmpty()) {
//      return Optional.empty();
//    } else {
//      try {
//        BinaryContentCreateRequest dto = new BinaryContentCreateRequest(
//            multipartFile.getOriginalFilename(),
//            multipartFile.getContentType(),
//            multipartFile.getBytes()
//        );
//        return Optional.of(dto);
//      } catch (IOException e) {
//        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
//            "Failed to process the uploaded file", e);
//      }
//    }
//  }
}
