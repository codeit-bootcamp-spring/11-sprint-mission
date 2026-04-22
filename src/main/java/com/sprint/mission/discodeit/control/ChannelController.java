package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.net.URI;
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
  public ResponseEntity<ChannelDto> createPublicChannel(
      @Valid @RequestBody PublicChannelRequest request) {
    ChannelDto response = channelService.createPublicChannel(request);
    URI location = URI.create("/api/channels/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @PostMapping("/private")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @Valid @RequestBody PrivateChannelRequest request) {
    ChannelDto response = channelService.createPrivateChannel(request);
    URI location = URI.create("/api/channels/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping(params = "userId")
  public ResponseEntity<List<ChannelDto>> readAllByUserId(@RequestParam UUID userId) {
    List<ChannelDto> responses = channelService.findAllByUserId(userId);

    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{channelId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
    channelService.deleteChannel(channelId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto> updateChannel(@PathVariable UUID channelId,
      @Valid @RequestBody ChannelUpdateRequest request) {
    ChannelDto updatedChannel = channelService.updateChannel(channelId, request);
    return ResponseEntity.ok(updatedChannel);
  }
}
