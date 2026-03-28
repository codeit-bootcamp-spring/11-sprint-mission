package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.binaryContent.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.user.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.user.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> create(
            @RequestPart("request") CreateUserRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
        CreateBinaryContentRequest profileRequest = null;
        if (profile != null && !profile.isEmpty()) {
            profileRequest = new CreateBinaryContentRequest(
                    profile.getOriginalFilename(),
                    profile.getSize(),
                    profile.getContentType(),
                    profile.getBytes()
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request, profileRequest));
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,          // 추가
            @RequestPart("request") UpdateUserRequest request,  // Update로 변경
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
        CreateBinaryContentRequest profileRequest = null;
        if (profile != null && !profile.isEmpty()) {
            profileRequest = new CreateBinaryContentRequest(
                    profile.getOriginalFilename(),
                    profile.getSize(),
                    profile.getContentType(),
                    profile.getBytes()
            );
        }
        return ResponseEntity.ok(userService.updateUser(userId, request, profileRequest));
    }
}