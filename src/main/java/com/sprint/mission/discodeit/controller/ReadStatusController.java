package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EndPoints.READ_STATUS)
@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  // 특정 채널의 메시지 수신 정보 생성
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Message 읽음 상태 생성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "이미 읽음 상태가 존재함")
  })
  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(readStatusService.create(dto));
  }

  // 특정 채널의 메시지 수신 정보 수정
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Message 읽음 상태 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message 읽음 상태가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message 읽음 상태를 찾을 수 없음")
  })
  @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
  public ResponseEntity<ReadStatus> update(
      @Parameter(description = "수정할 읽음 상태 ID")
      @PathVariable("readStatusId") UUID id,
      @RequestBody ReadStatusUpdateRequest dto) {
    return ResponseEntity.ok(readStatusService.update(id, dto));
  }

  // 특정 사용자의 메시지 수신 정보 조회
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "User의 Message 읽음 상태 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 읽음 상태 목록 조회 성공")
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ReadStatusDto>> readMessageByUserId(
      @Parameter(description = "조회할 User ID")
      @RequestParam("userId") List<UUID> userIds) {
    return ResponseEntity.ok(readStatusService.findAllByUserId(userIds));
  }
}
