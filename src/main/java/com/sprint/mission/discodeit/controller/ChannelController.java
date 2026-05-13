package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Channel")
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨")
  @PostMapping("/public")
  @ResponseStatus(HttpStatus.CREATED)
  public ChannelDto createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest request) {
    log.debug("POST /api/channels/public 요청 - name: {}", request.name());
    ChannelDto response = channelService.createPublic(request);
    log.info("POST /api/channels/public 정상 처리 완료 - channelId: {}", response.id());
    return response;
  }

  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  @PostMapping("/private")
  @ResponseStatus(HttpStatus.CREATED)
  public ChannelDto createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest request) {
    log.debug("POST /api/channels/private 요청");
    ChannelDto response = channelService.createPrivate(request);
    log.info("POST /api/channels/private 정상 처리 완료 - channelId: {}", response.id());
    return response;
  }

  @GetMapping
  public List<ChannelDto> getChannelByUserId(
      @RequestParam UUID userId) {
    return channelService.findAllByUserId(userId);
  }

  @PatchMapping("/{channelId}")
  public ChannelDto updateChannel(
      @PathVariable UUID channelId,
      @Valid @RequestBody ChannelUpdateRequest request) {
    log.debug("PATCH /api/channels/{} 요청", channelId);
    ChannelDto response = channelService.update(channelId, request);
    log.info("PATCH /api/channels/{} 정상 처리 완료", channelId);
    return response;
  }

  @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨")
  @DeleteMapping("/{channelId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteChannel(
      @PathVariable UUID channelId) {
    log.debug("DELETE /api/channels/{} 요청", channelId);
    channelService.delete(channelId);
    log.info("DELETE /api/channels/{} 정상 처리 완료", channelId);
  }
}
