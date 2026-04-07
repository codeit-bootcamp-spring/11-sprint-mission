package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<ReadStatusResponseDto> createReadStatus(
      @RequestBody ReadStatusCreateRequestDto dto) {
    try {
      return ResponseEntity.ok(readStatusService.create(dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ReadStatusResponseDto> updateReadStatus(
      @PathVariable UUID id,
      @RequestBody ReadStatusUpdateRequestDto dto) {
    try {
      dto.setId(id); // id를 dto에 주입
      return ResponseEntity.ok(readStatusService.update(dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusResponseDto>> findAllByUserId(
      @RequestParam UUID userId) {
    return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
  }
}
