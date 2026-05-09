package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.exception.InvalidException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    public UserController(UserService userService, UserStatusService userStatusService) {
        this.userService = userService;
        this.userStatusService = userStatusService;
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserCreateMultipartDoc.class)
                    )
            )
    )

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(
            @Parameter(hidden = true)
            @Valid
            @RequestPart("userCreateRequest") UserCreatePart part,
            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.info("사용자 생성 API 요청: username={}, email={}, hasProfile={}",
                part.username(), part.email(), profile != null && !profile.isEmpty()
        );

        UserCreateRequest request = new UserCreateRequest(
                part.username(),
                part.email(),
                part.password(),
                toBinaryContentCreateRequest(profile)
        );
        return userService.create(request);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.debug("사용자 목록 조회 API 요청");
        return userService.findAll();
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserUpdateMultipartDoc.class)
                    )
            )
    )

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto update(
            @PathVariable UUID userId,
            @Parameter(hidden = true)
            @Valid
            @RequestPart("userUpdateRequest") UserUpdatePart part,
            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        log.info("사용자 수정 API 요청: userId={}, hasprofile={}",
                userId, profile != null && !profile.isEmpty()
        );

        UserUpdateRequest request = new UserUpdateRequest(
                part.newUsername(),
                part.newEmail(),
                part.newPassword(),
                toBinaryContentCreateRequest(profile)
        );
        return userService.update(new UserUpdateParam(userId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{userId}")
    public void delete(@PathVariable UUID userId) {
        log.info("사용자 삭제 API 요청: userId={}", userId);
        userService.delete(userId);
    }

    @PatchMapping(value = "/{userId}/userStatus")
    public UserStatusDto updateStatus(@PathVariable UUID userId,
                                      @Valid @RequestBody UserStatusUpdateRequest request) {
        log.debug("사용자 상태수정 API 요청: userId={}, newLastActiveAt={}",
                userId,
                request.newLastActiveAt()
        );
        return userStatusService.updateByUserId(userId, request);
    }

    private BinaryContentCreateRequest toBinaryContentCreateRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
        } catch (Exception e) {
            log.error("사용자 프로필 파일 수정 실패: fileName={}",
                    file.getOriginalFilename(), e);
            throw new InvalidException("파일 처리 중 오류가 발생했습니다.");
        }
    }
}