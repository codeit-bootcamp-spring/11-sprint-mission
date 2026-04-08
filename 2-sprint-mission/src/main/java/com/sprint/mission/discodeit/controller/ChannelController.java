package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
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

  @PostMapping("/public")
  public ResponseEntity<ChannelDto.Response> create(
      @Valid @RequestBody ChannelDto.CreatePublicRequest request) {
    ChannelDto.Response response = channelService.createPublicChannel(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/private")
  public ResponseEntity<ChannelDto.Response> create(
      @Valid @RequestBody ChannelDto.CreatePrivateRequest request) {
    ChannelDto.Response response = channelService.createPrivateChannel(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto.Response> update(
      @PathVariable UUID channelId,
      @Valid @RequestBody ChannelDto.UpdateRequest request) {
    ChannelDto.Response response = channelService.update(channelId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto.Response>> findAllByUserid(
      @RequestParam UUID userId) {
    List<ChannelDto.Response> responseList = channelService.findAllByUserId(userId);
    return ResponseEntity.ok(responseList);
  }

}
