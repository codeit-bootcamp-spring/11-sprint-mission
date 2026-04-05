package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest dto) {
        User u = userService.create(dto);
        UserResponse userResponse = new UserResponse(u.getId(), u.getCreatedAt(), u.getUpdatedAt(), u.getName(), u.getEmail(), u.getProfileImageId(), true);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<UserResponse> update(@PathVariable UUID id,
                                               @RequestBody UserUpdateRequest dto) {
        User u = userService.update(id, dto);
        UserResponse userResponse = new UserResponse(u.getId(), u.getCreatedAt(), u.getUpdatedAt(), u.getName(), u.getEmail(), u.getProfileImageId(), true);

        return ResponseEntity.ok(userResponse);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 단건 조회 추가

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> userResponseList = userService.findAll();
        return ResponseEntity.ok(userResponseList);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatus> updateStatus(@PathVariable("id") UUID userId) {
        UserStatus userStatus = userStatusService.updateByUserId(userId);
        return ResponseEntity.ok(userStatus);
    }
}
