package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto.Response> create(
            @Valid @RequestPart("request") UserDto.CreateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        BinaryContentDto.CreateRequest profileImageRequest = convertToProfileImageDto(profileImage);

        UserDto.Response response = userService.create(request, profileImageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestPart("request")  UserDto.UpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        BinaryContentDto.CreateRequest profileImageRequest = convertToProfileImageDto(profileImage);

        UserDto.Response response = userService.update(id, request, profileImageRequest);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDto.Response>> findAll() {
        List<UserDto.Response> responseList = userService.findAll();
        return ResponseEntity.ok(responseList);
    }

    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusDto.Response> updateUserStatus(
            @PathVariable UUID id) {

        UserStatusDto.Response response = userStatusService.updateByUserId(id);
        return ResponseEntity.ok(response);
    }

    // 파일을 DTO 형태로 변환
    private BinaryContentDto.CreateRequest convertToProfileImageDto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return BinaryContentDto.CreateRequest.builder()
                    .fileName(file.getOriginalFilename())
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .bytes(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED);
        }
    }
}
