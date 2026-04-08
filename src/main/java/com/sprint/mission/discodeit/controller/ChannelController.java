package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  // 공개 채널 생성
  @PostMapping("/public")
  public ResponseEntity<ChannelResponse> createPublicChannel(
      @RequestBody PublicChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPublicChannel(request)); // 201 반환
  }

  // 비공개 채널 생성
  @PostMapping("/private")
  public ResponseEntity<ChannelResponse> createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPrivateChannel(request));
  }

  @GetMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> findById(@PathVariable UUID channelId) {
    return ResponseEntity.ok(channelService.findById(channelId));
  }

  @GetMapping
  public ResponseEntity<List<ChannelResponse>> findAllByUserId(
      @RequestParam UUID userId
  ) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }

  @PutMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> update(
      @PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest request
  ) {
    return ResponseEntity.ok(channelService.update(channelId, request));
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build(); // 204 반환
  }
}
