package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  // 1. 공개 채널 생성
  @PostMapping("/public")
  public ResponseEntity<ChannelResponseDto> createPublicChannel(
      @RequestBody ChannelCreateRequestDto dto) {
    try {
      return ResponseEntity.ok(channelService.createPublic(dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 2. 비공개 채널 생성
  @PostMapping("/private")
  public ResponseEntity<ChannelResponseDto> createPrivateChannel(
      @RequestBody ChannelCreateRequestDto dto) {
    try {
      return ResponseEntity.ok(channelService.createPrivate(dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 3. 채널 정보 수정
  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelResponseDto> updateChannel(
      @PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequestDto dto) {
    try {
      return ResponseEntity.ok(channelService.update(channelId, dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 4. 채널 삭제
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }

  // 5. 채널 목록 조회
  @GetMapping
  public ResponseEntity<List<ChannelResponseDto>> findAllChannels(
      @RequestParam UUID userId) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }
}
