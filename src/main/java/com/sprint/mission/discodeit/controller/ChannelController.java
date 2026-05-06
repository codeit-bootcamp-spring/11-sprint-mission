package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@RestController
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @PostMapping(path = "public")
  public ResponseEntity<ChannelResponse> create(
      @Valid @RequestBody PublicChannelCreateRequest publicChannelCreateRequest) {
    log.info("channel create-public request: {}", publicChannelCreateRequest);
    ChannelResponse createdChannel = this.channelService.createPublicChannel(
        publicChannelCreateRequest);

    log.debug("channel create-public response: {}", createdChannel);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdChannel);
  }

  @PostMapping(path = "private")
  public ResponseEntity<ChannelResponse> create(
      @Valid @RequestBody PrivateChannelCreateRequest privateChannelCreateRequest) {
    log.info("channel create-private request: {}", privateChannelCreateRequest);
    ChannelResponse createdChannel = this.channelService.createPrivateChannel(
        privateChannelCreateRequest);

    log.debug("channel create-private response: {}", createdChannel);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdChannel);
  }

  @PatchMapping(path = "{channelId}")
  public ResponseEntity<ChannelResponse> update(
      @PathVariable UUID channelId,
      @RequestBody PublicChannelUpdateRequest publicChannelUpdateRequest
  ) {
    log.info("channel update request: id={}, request={}", channelId, publicChannelUpdateRequest);
    ChannelResponse updatedChannel = this.channelService.updateChannel(channelId,
        publicChannelUpdateRequest);

    log.debug("channel update response: {}", updatedChannel);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedChannel);
  }

  @DeleteMapping(path = "{channelId}")
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    log.info("channel delete request: id={}", channelId);
    this.channelService.deleteChannel(channelId);

    log.debug("channel delete response: no-content");
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping
  public ResponseEntity<List<ChannelResponse>> findAllByUserId(
      @RequestParam(value = "userId") UUID userId) {
    log.info("channel find-all-by-user-id request: userId={}", userId);
    List<ChannelResponse> channels = this.channelService.findAllByUserId(userId);

    log.debug("channel find-all-by-user-id response: count={}", channels.size());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(channels);
  }
}
