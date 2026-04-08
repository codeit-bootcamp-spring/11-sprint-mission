package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
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

  // POST /api/readStatuses - 201 Created
  @PostMapping
  public ResponseEntity<ReadStatus> create(@Valid @RequestBody ReadStatusCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(readStatusService.create(request));
  }

  // GET /api/readStatuses?userId=123 - 200 OK
  @GetMapping
  public ResponseEntity<List<ReadStatus>> readAllByUserId(@RequestParam UUID userId) {
    return ResponseEntity.ok(readStatusService.readAllByUserId(userId));
  }

  // POST /api/readStatuses/{readStatusId} - 200 OK
  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatus> update(
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusUpdateRequest request) {
    readStatusService.update(new ReadStatusUpdateRequest(readStatusId, request.getNewLastReadAt())
    );
    return ResponseEntity.ok(readStatusService.read(readStatusId));
  }
}
