package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.DiscodeitIdMismatchException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
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

  // public, private 한번에 create
  @PostMapping
  public ResponseEntity<ChannelResponse> create(@Valid @RequestBody ChannelCreateRequest request) {
    if (request.channelType() == ChannelType.PUBLIC) {
      if (request.channelName() == null || request.channelName().isBlank()) {
        throw new DiscodeitInvalidInputException("PUBLIC");
      }
      ChannelResponse response = channelService.createPublicChannel(
          new PublicChannelCreateRequest(request.channelName(), request.channelDescription())
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    if (request.channelType() == ChannelType.PRIVATE) {
      if (request.userIds() == null || request.userIds().isEmpty()) {
        throw new DiscodeitInvalidInputException("PRIVATE");
      }
      ChannelResponse response = channelService.createPrivateChannel(
          new PrivateChannelCreateRequest(request.userIds())
      );
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    throw new IllegalArgumentException("지원하지 않는 채널 타입 입니다.");
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
    if (!id.equals(request.getChannelId())) {
      throw DiscodeitIdMismatchException.channel(id, request.getChannelId());
    }

    channelService.update(new ChannelUpdateRequest(
        id,
        request.getChannelName(),
        request.getChannelDescription()
    ));
    return ResponseEntity.noContent().build();
  }
}
