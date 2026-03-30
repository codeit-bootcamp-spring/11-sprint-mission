package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    //유저 등록
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {
        UUID userId = userService.create(request);
        return ResponseEntity.created(URI.create("/api/user/" + userId)).build();
    }

    //유저 정보 수정
    @RequestMapping(method = RequestMethod.PUT, value = "/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request
    ) {
        userService.update(userId, request);
        return ResponseEntity.ok().build();
    }

    //유저 삭제
    @RequestMapping(method = RequestMethod.DELETE, value = "/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    //모든 유저 조회
    @RequestMapping(method = RequestMethod.GET, value = "/findAll")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.readAll());
    }

    //유저 온라인 상태 업데이트
    @RequestMapping(method = RequestMethod.PUT, value = "/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable UUID userId) {
        userStatusService.updateByUserId(userId);
        return ResponseEntity.ok().build();
    }
}
