package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.exception.service.WrongChannelTypeException;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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
  public ResponseEntity<CreatedChannelInfo> createPublicChannel(
      @RequestBody CreatePublicChannel createPublicChannel) {

    CreatedChannelInfo channelInfo = channelService.createPublic(createPublicChannel);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("{id}")
        .buildAndExpand(channelInfo.id())
        .toUri();

    return ResponseEntity.created(uri).body(channelInfo);

  }


  @ApiResponse(responseCode = "201", description = "비공개 채널 생성")
  @PostMapping(value = "private")
  public ResponseEntity<CreatedChannelInfo> createPrivateChannel(
      @RequestBody CreatePrivateChannel createPrivateChannel) {

    CreatedChannelInfo channelInfo = channelService.createPrivate(createPrivateChannel);
    return ResponseEntity.status(HttpStatus.CREATED).body(channelInfo);

  }


  @GetMapping
  public ResponseEntity<List<ChannelInfo>> readAllChannelById(@RequestParam UUID userId) {

    List<ChannelInfo> channels = channelService.findAllById(userId);

    return ResponseEntity.status(HttpStatus.OK).body(channels);

  }

  @PatchMapping(value = "/{channelId}")
  public ResponseEntity<CreatedChannelInfo> updatePublicChannel(@PathVariable UUID channelId,
      @RequestBody UpdateChannel updateChannel) {

    CreatedChannelInfo channelInfoDto = channelService.updateChannel(channelId, updateChannel);

    return ResponseEntity.status(HttpStatus.OK).body(channelInfoDto);

  }


  @ApiResponse(responseCode = "204", description = "채널 삭제")
  @DeleteMapping(value = "/{channelId}")
  public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {

    channelService.deleteChannel(channelId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

  }


  @ExceptionHandler
  public ResponseEntity<ExceptionDto> wrongChannelTypeHandler(WrongChannelTypeException e,
      HttpServletRequest request) {

    ExceptionDto exceptionDto = ExceptionDto.of(
        HttpStatus.BAD_REQUEST,
        e.getMessage(),
        request.getRequestURI()
    );

    return ResponseEntity.status(exceptionDto.code()).body(exceptionDto);


  }

}
