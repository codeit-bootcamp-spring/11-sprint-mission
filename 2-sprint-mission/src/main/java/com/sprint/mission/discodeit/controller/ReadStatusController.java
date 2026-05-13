package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
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
@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @PostMapping
  public ResponseEntity<ReadStatusDto.Response> create(
      @Valid @RequestBody ReadStatusDto.CreateRequest request) {
    log.info("읽음 상태 생성 요청: {}", request);
    ReadStatusDto.Response response = readStatusService.create(request);

    log.debug("읽음 상태 생성 응답: {}", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto.Response> update(
      @PathVariable UUID readStatusId,
      @Valid @RequestBody ReadStatusDto.UpdateRequest request) {
    log.info("읽음 상태 업데이트 요청: id={}, request={}", readStatusId, request);
    ReadStatusDto.Response response = readStatusService.update(readStatusId, request);

    log.debug("읽음 상태 업데이트 응답: {}", response);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ReadStatusDto.Response>> findAllByUserId(
      @RequestParam UUID userId) {
    log.debug("사용자 ID 기반 읽음 상태 목록 조회 요청: userId={}", userId);
    List<ReadStatusDto.Response> responseList = readStatusService.findAllByUserId(userId);

    log.debug("사용자 ID 기반 읽음 상태 목록 조회 응답: {}건", responseList.size());
    return ResponseEntity.ok(responseList);
  }
}