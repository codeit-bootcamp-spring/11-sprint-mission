package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import java.net.URI;
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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ReadStatusDto> createReadStatus(
      @Valid @RequestBody ReadStatusCreateRequest request) {
    ReadStatusDto createdStatus = readStatusService.createReadStatus(request);
    URI location = URI.create("/api/readStatuses/" + createdStatus.id());
    return ResponseEntity.created(location).body(createdStatus);
  }

  @GetMapping(params = "userId")
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam UUID userId) {
    List<ReadStatusDto> responses = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(responses);
  }

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> updateReadStatus(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request
  ) {
    ReadStatusDto updatedStatus = readStatusService.updateStatus(readStatusId, request);
    return ResponseEntity.ok(updatedStatus);
  }
}
