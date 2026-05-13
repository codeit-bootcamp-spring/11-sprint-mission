package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping(EndPoints.CHANNEL)
@RequiredArgsConstructor
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  // 공개 채널 생성
  @Override
  @PostMapping("/public")
  public ResponseEntity<Channel> createPublic(@Valid @RequestBody ChannelCreatePublicRequest dto) {
    log.info("[CHANNEL_CREATE_PUBLIC_REQUEST] PUBLIC 채널 생성 요청 - 채널 이름={}", dto.name());
    Channel channel = channelService.createPublic(dto);
    log.info("[CHANNEL_CREATE_PUBLIC_RESPONSE] PUBLIC 채널 생성 응답 - 채널 ID={}", channel.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(channel);
  }

  // 비공개 채널 생성
  @Override
  @PostMapping("/private")
  public ResponseEntity<Channel> createPrivate(
      @Valid @RequestBody ChannelCreatePrivateRequest dto) {
    log.info("[CHANNEL_CREATE_PRIVATE_REQUEST] PRIVATE 채널 생성 요청 - 참여자 수={}",
        dto.participantIds().size());
    Channel channel = channelService.createPrivate(dto);
    log.info("[CHANNEL_CREATE_PRIVATE_RESPONSE] PRIVATE 채널 생성 응답 - 채널 ID={}", channel.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(channel);
  }

  // 특정 공개 채널의 정보 수정(channels/{channelId}?)
  @Override
  @PatchMapping("/{channelId}")
  public ResponseEntity<Channel> updatePublic(
      @PathVariable("channelId") UUID id,
      @Valid @RequestBody ChannelUpdateRequest dto) {
    log.info("[CHANNEL_UPDATE_REQUEST] 채널 수정 요청 - 채널 ID={}", id);
    Channel channel = channelService.update(id, dto);
    log.info("[CHANNEL_UPDATE_RESPONSE] 채널 수정 응답 - 채널 ID={}", id);

    return ResponseEntity.ok(channel);
  }

  // 특정 채널 삭제(channels/{channelId})
  @Override
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(
      @PathVariable("channelId") UUID id) {
    log.info("[CHANNEL_DELETE_REQUEST] 채널 삭제 요청 - 채널 ID={}", id);
    channelService.delete(id);
    log.info("[CHANNEL_DELETE_RESPONSE] 채널 삭제 응답 - 채널 ID={}", id);
    return ResponseEntity.noContent().build();
  }

  // 특정 사용자가 속한 모든 채널 목록 조회(channels?user-id=...)
  @Override
  @GetMapping
  public ResponseEntity<List<ChannelDto>> readAllByUser(
      @RequestParam("userId") UUID userId) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }
}
