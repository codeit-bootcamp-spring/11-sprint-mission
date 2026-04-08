package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/read-statuses")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatusResponse> create(@RequestBody ReadStatusCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(readStatusService.create(request)); // 201 반환
  }

  @GetMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusResponse> findById(
      @PathVariable UUID readStatusId) {
    return ResponseEntity.ok(readStatusService.findById(readStatusId));
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
      @RequestParam UUID userId) { // 파라미터명 readStatusId -> userId
    // 읽는 사람 입장에서는 userId값을 받아서 진행중인데, readStatusId인가? 착각가능
    return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
  }

  @PutMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusResponse> update(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  ) {
    return ResponseEntity.ok(readStatusService.update(readStatusId, request));
  }

  @DeleteMapping("/{readStatusId}")
  public ResponseEntity<Void> delete(@PathVariable UUID readStatusId) {
    readStatusService.delete(readStatusId);
    return ResponseEntity.noContent().build(); // 204 반환
  }
}
