package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
      @RequestBody PublicChannelCreateRequest request) {
    return channelService.createPublic(request);
  }

  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  @PostMapping("/private")
  @ResponseStatus(HttpStatus.CREATED)
  public ChannelDto createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest request) {
    return channelService.createPrivate(request);
  }

  @GetMapping
  public List<ChannelDto> getChannelByUserId(
      @RequestParam UUID userId) {
    return channelService.findAllByUserId(userId);
  }

  @PatchMapping("/{channelId}")
  public ChannelDto updateChannel(
      @PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest request) {
    return channelService.update(channelId, request);
  }

  @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨")
  @DeleteMapping("/{channelId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteChannel(
      @PathVariable UUID channelId) {
    channelService.delete(channelId);
  }
}
