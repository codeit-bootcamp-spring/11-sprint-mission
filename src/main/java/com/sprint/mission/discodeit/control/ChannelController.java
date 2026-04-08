package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.entity.Channel;
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

  @PostMapping("/public")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ChannelDto> createPublicChannel(@RequestBody PublicChannelRequest request) {
    Channel savedPublicChannel = channelService.createPublicChannel(request);
    ChannelDto response = ChannelDto.from(savedPublicChannel);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/private")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @RequestBody PrivateChannelRequest request) {
    Channel savedPrivateChannel = channelService.createPrivateChannel(request);
    ChannelDto response = ChannelDto.from(savedPrivateChannel);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping(params = "userId")
  public ResponseEntity<List<ChannelResponse>> readAllByUserId(@RequestParam UUID userId) {
    List<ChannelResponse> responses = channelService.findAllByUserId(userId);

    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{channelId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
    channelService.deleteChannel(channelId);

    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<Void> updateChannel(@PathVariable UUID channelId,
      @RequestBody ChannelUpdate request) {
    channelService.updateChannel(channelId, request);
    return ResponseEntity.ok().build();
  }
}
