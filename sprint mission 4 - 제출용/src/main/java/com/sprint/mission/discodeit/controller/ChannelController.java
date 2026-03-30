package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  // public create
  @PostMapping("/public")
  public ResponseEntity<ChannelResponse> createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPublicChannel(request));
  }

  // private create
  @PostMapping("/private")
  public ResponseEntity<ChannelResponse> createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(channelService.createPrivateChannel(request));
  }

  // read
  @GetMapping("/{id}")
  public ResponseEntity<ChannelResponse> read(@PathVariable UUID id) {
    return ResponseEntity.ok(channelService.read(id));
  }

  // readAllByUserId
  @GetMapping
  public ResponseEntity<List<ChannelResponse>> readAllByUserId(@RequestParam UUID userId) {
    return ResponseEntity.ok(channelService.readAllByUserId(userId));
  }

  // delete
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    channelService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // update
  @PutMapping("/{id}")
  public ResponseEntity<Void> update(@PathVariable UUID id,
      @Valid @RequestBody ChannelUpdateRequest request) {
    channelService.update(request);
    return ResponseEntity.ok().build();
  }

  // restore
  @PostMapping("/{id}/restore")
  public ResponseEntity<Void> restore(@PathVariable UUID id) {
    channelService.restore(id);
    return ResponseEntity.ok().build();
  }

}
