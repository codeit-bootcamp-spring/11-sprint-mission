package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/readStatuses")
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  @PostMapping
  @Override
  public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request) {
    ReadStatus createdReadStatus = readStatusService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdReadStatus);
  }

  @GetMapping(path = "{readStatusId}")
  @Override
  public ResponseEntity<ReadStatusDto> find(@PathVariable("readStatusId") UUID readStatusId) {
    ReadStatusDto readStatus = readStatusService.find(readStatusId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(readStatus);
  }

  @GetMapping
  @Override
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam("userId") UUID userId) {
    List<ReadStatusDto> readStatuses = readStatusService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(readStatuses);
  }

  @PatchMapping(path = "{readStatusId}")
  @Override
  public ResponseEntity<ReadStatus> update(
          @PathVariable("readStatusId") UUID readStatusId,
          @RequestBody ReadStatusUpdateRequest request
  ) {
    ReadStatus updatedReadStatus = readStatusService.update(readStatusId, request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedReadStatus);
  }

  @DeleteMapping(path = "{readStatusId}")
  @Override
  public ResponseEntity<Void> delete(@PathVariable("readStatusId") UUID readStatusId) {
    readStatusService.delete(readStatusId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }
}