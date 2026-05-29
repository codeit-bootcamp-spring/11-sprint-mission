package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
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
@RequestMapping(EndPoints.READ_STATUS)
@RequiredArgsConstructor
public class ReadStatusController implements ReadStatusApi {

  private final ReadStatusService readStatusService;

  // 특정 채널의 메시지 수신 정보 생성
  @Override
  @PostMapping
  public ResponseEntity<ReadStatusDto> create(@Valid @RequestBody ReadStatusCreateRequest dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(readStatusService.create(dto));
  }

  // 특정 채널의 메시지 수신 정보 수정
  @Override
  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> update(
      @PathVariable("readStatusId") UUID id,
      @Valid @RequestBody ReadStatusUpdateRequest dto) {
    return ResponseEntity.ok(readStatusService.update(id, dto));
  }

  // 특정 사용자의 메시지 수신 정보 조회
  @Override
  @GetMapping
  public ResponseEntity<List<ReadStatusDto>> readMessageByUserId(
      @RequestParam("userId") List<UUID> userIds) {
    return ResponseEntity.ok(readStatusService.findAllByUserId(userIds));
  }
}
