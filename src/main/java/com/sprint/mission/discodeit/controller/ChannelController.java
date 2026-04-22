package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<ChannelDto> createPublic(
      @RequestBody PublicChannelCreateRequest request) {
    ChannelDto response = channelService.createPublic(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 2. 비공개 채널 생성
  @PostMapping("/private")
  public ResponseEntity<ChannelDto> createPrivate(
      @RequestBody PrivateChannelCreateRequest request) {
    ChannelDto response = channelService.createPrivate(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // 3. 채널 정보 수정
  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto> update(
      @PathVariable UUID channelId,
      @RequestBody PublicChannelUpdateRequest request) {
    ChannelDto response = channelService.update(channelId, request);
    return ResponseEntity.ok(response);
  }

  // 4. 채널 삭제
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }

  // 5. 채널 목록 조회
  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(
      @RequestParam UUID userId) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }
}
