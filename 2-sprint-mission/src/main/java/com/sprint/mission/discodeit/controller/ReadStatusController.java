package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatusDto.Response> create(
      @Valid @RequestBody ReadStatusDto.CreateRequest request) {
    ReadStatusDto.Response response = readStatusService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto.Response> update(
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusDto.UpdateRequest request) {
    ReadStatusDto.Response response = readStatusService.update(readStatusId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusDto.Response>> findAllByUserId(
      @RequestParam UUID userId) {
    List<ReadStatusDto.Response> responseList = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(responseList);
  }
}