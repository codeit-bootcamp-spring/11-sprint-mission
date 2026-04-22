package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/readStatuses")
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @Operation(summary = "Message 읽음 상태 생성", operationId = "create_1")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "이미 읽음 상태가 존재함"),
      @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨")
  })
  @PostMapping
  public ResponseEntity<ReadStatusDto> create(@Valid @RequestBody ReadStatusCreateRequest dto) {
    ReadStatusDto readStatusDto = readStatusService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(readStatusDto);
  }

  @Operation(summary = "Message 읽음 상태 수정", operationId = "update_1")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음")
  })
  @PatchMapping("/{readStatusId}")
  public ResponseEntity<ReadStatusDto> update(@Valid @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest dto) {
    ReadStatusDto readStatusDto = readStatusService.update(readStatusId, dto);
    return ResponseEntity.ok(readStatusDto);
  }

  @Operation(summary = "User의 Message 읽음 상태 목록 조회", operationId = "findAllByUserId")
  @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<ReadStatusDto>> findAllByUserId(@RequestParam("userId") UUID userId) {
    List<ReadStatusDto> ReadStatusDtoList = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(ReadStatusDtoList);
  }
}
