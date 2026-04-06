package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, UserStatusService userStatusService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.userStatusService = userStatusService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserCreateMultipartDoc.class)
                    )
            )
    )

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto create(
            @Parameter(hidden = true)
            @RequestPart("userCreateRequest") String userCreateRequestJson,
            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) {
        try {
            UserCreatePart part = objectMapper.readValue(userCreateRequestJson, UserCreatePart.class);

            UserCreateRequest newRequest = new UserCreateRequest(
                    part.username(),
                    part.email(),
                    part.password(),
                    toBinaryContentCreateRequest(profile));

            return userService.create(newRequest);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 요청 형식입니다.");
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<UserDto> findAll() {
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

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto update(
            @PathVariable UUID userId,
            @Parameter(hidden = true)
            @RequestPart("userUpdateRequest") String userUpdateRequestJson,
            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile)
    {
        try {
            UserUpdatePart part = objectMapper.readValue(userUpdateRequestJson, UserUpdatePart.class);

            UserUpdateRequest newRequest = new UserUpdateRequest(
                    part.newUsername(),
                    part.newEmail(),
                    part.newPassword(),
                    toBinaryContentCreateRequest(profile));
            return userService.update(new UserUpdateParam(userId, newRequest));
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 요청 형식입니다.");
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public UserStatusDto updateStatus(@PathVariable UUID userId,
                                      @RequestBody UserStatusUpdateRequest request) {
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
            throw new IllegalArgumentException("파일 처리 중 오류가 발생했습니다.");
        }
    }
}