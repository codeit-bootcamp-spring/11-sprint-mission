package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;


  @PostMapping
  @ApiResponse(responseCode = "201")
  public ResponseEntity<ReadStatusDto> createReadStatus(
      @RequestBody ReadStatusCreateRequest readStatusDto
  ) {

    ReadStatusDto readStatusInfoDto = readStatusService.create(readStatusDto);
    return ResponseEntity.status(201).body(readStatusInfoDto);


  }


  @GetMapping
  public ResponseEntity<List<ReadStatusDto>> getReadStatusById(
      @RequestParam UUID userId) {

    List<ReadStatusDto> readStatusInfoList = readStatusService.findAllByUserId(userId);

    return ResponseEntity.status(200).body(readStatusInfoList);

  }


  @PatchMapping(value = "/{readStatusId}")
  ResponseEntity<ReadStatusDto> updateReadStatus(
      @PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest readStatusUpdateRequestDto) {
    ReadStatusDto readStatusDto = readStatusService.update(readStatusId,
        readStatusUpdateRequestDto);
    return ResponseEntity.status(200).body(readStatusDto);

  }
}
