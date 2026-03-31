package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusControl {

  private final ReadStatusService readStatusService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ReadStatus> createReadStatus(@RequestBody ReadStatusCreateDto request) {
    ReadStatus createdStatus = readStatusService.createReadStatus(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdStatus);
  }

  @GetMapping(params = "userId")
  public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam UUID userId) {
    List<ReadStatus> responses = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(responses);
  }

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatus> updateReadStatus(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateDto request
  ) {
    ReadStatus updatedStatus = readStatusService.updateStatus(readStatusId, request);
    return ResponseEntity.ok(updatedStatus);
  }
}
