package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "ReadStatus")
@RestController
@RequestMapping("/api/readStatuses")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @ApiResponse(responseCode = "201", description = "Message 읽음 상태가 성공적으로 생성됨")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReadStatusDto createReadStatus(
      @RequestBody ReadStatusCreateRequest request) {
    return readStatusService.create(request);
  }

  @PatchMapping("/{readStatusId}")
  public ReadStatusDto updateReadStatus(@PathVariable UUID readStatusId,
      @RequestBody ReadStatusUpdateRequest request) {
    return readStatusService.update(readStatusId, request);
  }

  @GetMapping
  public List<ReadStatusDto> getReadStatusByUserId(@RequestParam UUID userId) {
    return readStatusService.findAllByUserId(userId);
  }
}
