package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
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

  // create public
  @PostMapping("/public")
  public ResponseEntity<ChannelResponse> createPublic(
      @Valid @RequestBody PublicChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPublicChannel(request));
  }

  // create private
  @PostMapping("/private")
  public ResponseEntity<ChannelResponse> createPrivate(
      @Valid @RequestBody PrivateChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPrivateChannel(request));
  }

  // read
  @GetMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> read(@PathVariable UUID channelId) {
    return ResponseEntity.ok(channelService.read(channelId));
  }

  // readAllByUserId
  @GetMapping
  public ResponseEntity<List<ChannelResponse>> readAllByUserId(@RequestParam UUID userId) {
    return ResponseEntity.ok(channelService.readAllByUserId(userId));
  }

  // update
  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> update(
      @PathVariable UUID channelId,
      @Valid @RequestBody PublicChannelUpdateRequest request
  ) {
    channelService.update(new ChannelUpdateRequest(
        channelId,
        request.getNewName(),
        request.getNewDescription()
    ));
    return ResponseEntity.ok(channelService.read(channelId));
  }

  // delete
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }
}
