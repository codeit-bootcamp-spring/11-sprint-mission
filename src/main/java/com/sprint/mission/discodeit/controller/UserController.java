package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @Operation(summary = "User 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
            @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함")
    })
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> create(
            @Parameter(description = "User 생성 정보")
            @RequestPart("userCreateRequest") @Valid UserCreateRequest dto,
            @Parameter(description = "User 프로필 이미지")
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        UserDto result = userService.create(dto, profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "User 정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
            @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함"),
            @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
    })
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> update(
            @Parameter(description = "수정할 User ID")
            @PathVariable UUID userId,
            @Parameter(description = "수정할 User 정보")
            @RequestPart("userUpdateRequest") @Valid UserUpdateRequest dto,
            @Parameter(description = "수정할 User 프로필 이미지")
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        userService.update(userId, dto, profile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "User 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
            @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")
    })
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 User ID")
            @PathVariable UUID userId
    ) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 User 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> result = userService.findAll();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "User 온라인 상태 업데이트", operationId = "updateUserStatusByUserId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User 온라인 상태가 성공적으로 업데이트됨"),
            @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음")
    })
    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "상태를 변경할 User ID")
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest dto
    ) {
        userStatusService.updateByUserId(userId, dto);
        return ResponseEntity.ok().build();
    }
}
