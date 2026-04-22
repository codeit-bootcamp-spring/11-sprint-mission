package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
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

  // POST /api/channels/public - 201 Created
  @PostMapping("/public")
  public ResponseEntity<ChannelDto> createPublic(
      @Valid @RequestBody PublicChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPublicChannel(request));
  }

  // POST /api/channels/private - 201 Created
  @PostMapping("/private")
  public ResponseEntity<ChannelDto> createPrivate(
      @Valid @RequestBody PrivateChannelCreateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPrivateChannel(request));
  }

  // GET /api/channels/{channelId} - 200 OK
  @GetMapping("/{channelId}")
  public ResponseEntity<ChannelDto> find(@PathVariable UUID channelId) {
    return ResponseEntity.ok(channelService.find(channelId));
  }

  // GET /api/channels?userId=123 - 200 OK
  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }

  // PATCH /api/channels/{channelId} - 200 OK
  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto> update(
      @PathVariable UUID channelId,
      @Valid @RequestBody PublicChannelUpdateRequest request
  ) {
    channelService.update(new ChannelUpdateRequest(
        channelId,
        request.getNewName(),
        request.getNewDescription()
    ));
    return ResponseEntity.ok(channelService.find(channelId));
  }

  // DELETE /api/channels/{channelId} - 204 No Content
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }
}
