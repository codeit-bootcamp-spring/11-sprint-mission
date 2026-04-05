package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  @PostMapping("/public")
  public ChannelDto createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
    return channelService.createPublic(request);
  }

  @PostMapping("/private")
  public ChannelDto createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
    return channelService.createPrivate(request);
  }

  @GetMapping
  public List<ChannelDto> getChannelByUserId(@RequestParam UUID userId) {
    return channelService.findAllByUserId(userId);
  }

  @PatchMapping("/{channelId}")
  public void updateChannel(@PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest request) {
    channelService.update(channelId, request);
  }

  @DeleteMapping("/{channelId}")
  public void deleteChannel(@PathVariable UUID channelId) {
    channelService.delete(channelId);
  }
}
