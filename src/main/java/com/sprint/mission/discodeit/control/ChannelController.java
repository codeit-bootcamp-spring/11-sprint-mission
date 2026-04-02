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

  @RequestMapping(value = "/public", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPublicChannel(@RequestBody PublicChannelRequest request) {
    Channel savedPublicChannel = channelService.createPublicChannel(request);
    ChannelDto response = ChannelDto.from(savedPublicChannel);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @RequestMapping(value = "/private", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @RequestBody PrivateChannelRequest request) {
    Channel savedPrivateChannel = channelService.createPrivateChannel(request);
    ChannelDto response = ChannelDto.from(savedPrivateChannel);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<ChannelResponse> readChannel(@PathVariable UUID id) {
    ChannelResponse response = channelService.readChannel(id);

    return ResponseEntity.ok(response);
  }

  @RequestMapping(method = RequestMethod.GET, params = "userId")
  public ResponseEntity<List<ChannelResponse>> readAllByUserId(@RequestParam UUID userId) {
    List<ChannelResponse> responses = channelService.findAllByUserId(userId);

    return ResponseEntity.ok(responses);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID id) {
    channelService.deleteChannel(id);

    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public ResponseEntity<Void> updateChannel(@PathVariable UUID id,
      @RequestBody ChannelUpdate request) {
    channelService.updateChannel(id, request);
    return ResponseEntity.ok().build();
  }
}
