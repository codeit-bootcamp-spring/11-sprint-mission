package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/read-statuses")
@RequiredArgsConstructor
@RestController
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatusResponse> create(
      @Valid @RequestBody ReadStatusCreateRequest readStatusCreateRequest) {
    log.info("read-status create request: {}", readStatusCreateRequest);
    ReadStatusResponse createdReadStatusResponse = this.readStatusService.createReadStatus(
        readStatusCreateRequest);

    log.debug("read-status create response: {}", createdReadStatusResponse);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdReadStatusResponse);
  }

  @PatchMapping(path = "{readStatusId}")
  public ResponseEntity<ReadStatusResponse> update(
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusUpdateRequest readStatusUpdateRequest
  ) {
    log.info("read-status update request: id={}, request={}", readStatusId,
        readStatusUpdateRequest);
    ReadStatusResponse updatedReadStatusResponse = this.readStatusService.updateReadStatus(
        readStatusId, readStatusUpdateRequest);

    log.debug("read-status update response: {}", updatedReadStatusResponse);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedReadStatusResponse);
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusResponse>> findAllByUserId(
      @RequestParam UUID userId) {
    log.info("read-status find-all-by-user-id request: userId={}", userId);
    List<ReadStatusResponse> readStatusResponses = this.readStatusService.findAllByUserId(userId);

    log.debug("read-status find-all-by-user-id response: count={}", readStatusResponses.size());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(readStatusResponses);
  }
}
