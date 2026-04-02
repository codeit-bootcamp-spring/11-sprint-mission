package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.ReadStatusCreateDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/read-status")
@RequiredArgsConstructor
public class ReadStatusController {

  private final ReadStatusService readStatusService;

  @RequestMapping(value = "/channels/{channelId}", method = RequestMethod.POST)
  public ResponseEntity<Void> createReadStatus(
      @PathVariable UUID channelId,
      @RequestBody ReadStatusCreateDto request
  ) {
    if (!channelId.equals(request.channelId())) {
      throw new IllegalArgumentException("요청 경로의 채널 ID와 데이터의 채널 ID가 일치하지 않습니다.");
    }
    readStatusService.createReadStatus(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @RequestMapping(method = RequestMethod.GET, params = "userId")
  public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam UUID userId) {
    List<ReadStatus> responses = readStatusService.findAllByUserId(userId);
    return ResponseEntity.ok(responses);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteReadStatus(@PathVariable UUID id) {
    readStatusService.deleteReadStatus(id);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/channels/{channelId}", method = RequestMethod.PATCH)
  public ResponseEntity<ReadStatus> updateReadStatus(
      @PathVariable UUID channelId,
      @RequestParam UUID userId,
      @RequestBody ReadStatusUpdateDto request
  ) {
    ReadStatus updatedStatus = readStatusService.updateStatus(userId, channelId, request);
    return ResponseEntity.ok(updatedStatus);
  }
}
