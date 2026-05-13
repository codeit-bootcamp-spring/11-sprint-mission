package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
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
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  @PostMapping("/public")
  public ResponseEntity<ChannelDto.Response> create(
      @Valid @RequestBody ChannelDto.CreatePublicRequest request) {
    log.info("퍼블릭 채널 생성 요청: name={}", request.name());
    ChannelDto.Response response = channelService.createPublicChannel(request);

    log.debug("퍼블릭 채널 생성 응답: {}", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/private")
  public ResponseEntity<ChannelDto.Response> create(
      @Valid @RequestBody ChannelDto.CreatePrivateRequest request) {
    log.info("프라이빗 채널 생성 요청");
    ChannelDto.Response response = channelService.createPrivateChannel(request);

    log.debug("프라이빗 채널 생성 응답: {}", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto.Response> update(
      @PathVariable UUID channelId,
      @Valid @RequestBody ChannelDto.UpdateRequest request) {
    log.info("채널 업데이트 요청: channelId={}", channelId);
    ChannelDto.Response response = channelService.update(channelId, request);

    log.debug("채널 업데이트 응답: {}", response);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID channelId) {
    log.info("채널 삭제 요청: channelId={}", channelId);
    channelService.delete(channelId);

    log.debug("채널 삭제 응답 완료");
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto.Response>> findAllByUserid(
      @RequestParam UUID userId) {
    log.debug("사용자 소속 채널 목록 조회 요청: userId={}", userId);
    List<ChannelDto.Response> responseList = channelService.findAllByUserId(userId);

    log.debug("사용자 소속 채널 목록 조회 응답: {}건", responseList.size());
    return ResponseEntity.ok(responseList);
  }

}
