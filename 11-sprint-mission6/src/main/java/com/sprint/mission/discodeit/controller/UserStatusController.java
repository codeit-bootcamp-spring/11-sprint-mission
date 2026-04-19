package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserStatusApi;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/userStatuses")
public class UserStatusController implements UserStatusApi {

    private final UserStatusService userStatusService;

    @PostMapping
    @Override
    public ResponseEntity<UserStatusDto> create(@RequestBody UserStatusCreateRequest request) {
        UserStatusDto createdUserStatus = userStatusService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUserStatus);
    }

    @GetMapping(path = "{userStatusId}")
    @Override
    public ResponseEntity<UserStatusDto> find(@PathVariable("userStatusId") UUID userStatusId) {
        UserStatusDto userStatus = userStatusService.find(userStatusId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userStatus);
    }

    @GetMapping
    @Override
    public ResponseEntity<List<UserStatusDto>> findAll() {
        List<UserStatusDto> userStatuses = userStatusService.findAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userStatuses);
    }

    @PatchMapping(path = "{userStatusId}")
    @Override
    public ResponseEntity<UserStatusDto> update(
            @PathVariable("userStatusId") UUID userStatusId,
            @RequestBody UserStatusUpdateRequest request
    ) {
        UserStatusDto updatedUserStatus = userStatusService.update(userStatusId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedUserStatus);
    }

    @DeleteMapping(path = "{userStatusId}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable("userStatusId") UUID userStatusId) {
        userStatusService.delete(userStatusId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}