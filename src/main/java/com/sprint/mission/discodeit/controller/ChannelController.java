package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChanelUpdateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;

import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  @ApiResponse(responseCode = "201", description = "공개 채널 생성")
  @PostMapping(value = "public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @Valid @RequestBody PublicChannelCreateRequest publicChannelCreateRequest) {

    ChannelDto channelInfo = channelService.createPublic(publicChannelCreateRequest);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("{id}")
        .buildAndExpand(channelInfo.id())
        .toUri();

    return ResponseEntity.created(uri).body(channelInfo);

  }


  @ApiResponse(responseCode = "201", description = "비공개 채널 생성")
  @PostMapping(value = "private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @Valid @RequestBody PrivateChannelCreateRequest privateChannelCreateRequest) {

    ChannelDto channelInfo = channelService.createPrivate(privateChannelCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(channelInfo);

  }


  @GetMapping
  public ResponseEntity<List<ChannelDto>> readAllChannelById(@RequestParam UUID userId) {

    List<ChannelDto> channels = channelService.findAllByUserId(userId);

    return ResponseEntity.status(HttpStatus.OK).body(channels);

  }

  @PatchMapping(value = "/{channelId}")
  public ResponseEntity<ChannelDto> updatePublicChannel(@PathVariable UUID channelId,
      @Valid @RequestBody PublicChanelUpdateRequest publicChanelUpdateRequest) {

    ChannelDto channelInfoDto = channelService.updateChannel(channelId,
        publicChanelUpdateRequest);

    return ResponseEntity.status(HttpStatus.OK).body(channelInfoDto);

  }


  @ApiResponse(responseCode = "204", description = "채널 삭제")
  @DeleteMapping(value = "/{channelId}")
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {

    channelService.deleteChannel(channelId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

  }


}
