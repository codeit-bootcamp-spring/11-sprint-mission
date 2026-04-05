package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatusdto.CreateReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusInfoDto;
import com.sprint.mission.discodeit.dto.readstatusdto.UpdateReadStatus;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/readStatuses")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;


  @PostMapping
  @ApiResponse(responseCode = "201")
  public ResponseEntity<ReadStatusInfoDto> createReadStatus(
      @RequestBody CreateReadStatusDto readStatusDto
  ) {

    ReadStatusInfoDto readStatusInfoDto = readStatusService.create(readStatusDto);
    return ResponseEntity.status(201).body(readStatusInfoDto);


  }


  @GetMapping
  public ResponseEntity<List<ReadStatusInfoDto>> getReadStatusById(
      @RequestParam UUID userId) {

    List<ReadStatusInfoDto> readStatusInfoList = readStatusService.findAllById(userId);

    return ResponseEntity.status(200).body(readStatusInfoList);

  }


  @PatchMapping(value = "/{readStatusId}")
  ResponseEntity<ReadStatusInfoDto> updateReadStatus(
      @PathVariable UUID readStatusId,
      @RequestBody UpdateReadStatus updateReadStatusDto) {
    ReadStatusInfoDto readStatusInfoDto = readStatusService.update(readStatusId,
        updateReadStatusDto);
    return ResponseEntity.status(200).body(readStatusInfoDto);

  }
}
