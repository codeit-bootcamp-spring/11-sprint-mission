package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @PostMapping("/public")
  @Override
  public ResponseEntity<ChannelDto> create(PublicChannelCreateRequest request) {
    ChannelDto createdChannel = channelService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  @PostMapping("/private")
  @Override
  public ResponseEntity<ChannelDto> create(PrivateChannelCreateRequest request) {
    ChannelDto createdChannel = channelService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  @GetMapping("/{channelId}")
  @Override
  public ResponseEntity<ChannelDto> find(@PathVariable("channelId") UUID channelId) {
    ChannelDto channel = channelService.find(channelId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(channel);
  }

  @GetMapping
  @Override
  public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam("userId") UUID userId) {
    List<ChannelDto> channels = channelService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(channels);
  }

  @PatchMapping("/public/{channelId}")
  @Override
  public ResponseEntity<ChannelDto> update(
          @PathVariable("channelId") UUID channelId,
          @RequestBody PublicChannelUpdateRequest request
  ) {
    ChannelDto updatedChannel = channelService.update(channelId, request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedChannel);
  }

  @DeleteMapping("/{channelId}")
  @Override
  public ResponseEntity<Void> delete(@PathVariable("channelId") UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }
}