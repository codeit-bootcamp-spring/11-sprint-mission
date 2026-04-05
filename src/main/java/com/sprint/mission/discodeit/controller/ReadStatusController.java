package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @GetMapping
    public ResponseEntity<List<ReadStatus>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ReadStatus> createReadStatus(
            @Valid @RequestBody ReadStatusCreateRequest request
    ) {
        ReadStatus createdStatus = readStatusService.createReadStatus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStatus);
    }

    @PatchMapping("/{readStatusId}")
    public ResponseEntity<ReadStatus> updateReadStatus(
            @PathVariable UUID readStatusId,
            @RequestParam UUID requestUserId,
            @Valid @RequestBody ReadStatusUpdateRequest request
    ) {
        ReadStatus updatedStatus = readStatusService.updateReadStatus(requestUserId, readStatusId, request);
        return ResponseEntity.ok(updatedStatus);
    }
}