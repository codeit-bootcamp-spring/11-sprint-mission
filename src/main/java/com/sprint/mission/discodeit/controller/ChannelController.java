package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EndPoints.CHANNEL)
@Tag(name = "Channel", description = "Channel API")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  // 공개 채널 생성
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Public Channel 생성")
  @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨")
  @RequestMapping(value = "/public", method = RequestMethod.POST)
  public ResponseEntity<Channel> createPublic(@RequestBody ChannelCreatePublicRequest dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPublic(dto));
  }

  // 비공개 채널 생성
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Private Channel 생성")
  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
  @RequestMapping(value = "/private", method = RequestMethod.POST)
  public ResponseEntity<Channel> createPrivate(@RequestBody ChannelCreatePrivateRequest dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPrivate(dto));
  }

  // 특정 공개 채널의 정보 수정(channels/{channelId}?)
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Channel 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "Private Channel은 수정할 수 없음"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음")
  })
  @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
  public ResponseEntity<Channel> updatePublic(
      @Parameter(description = "수정할 Channel ID")
      @PathVariable("channelId") UUID id,
      @RequestBody ChannelUpdateRequest dto) {
    return ResponseEntity.ok(channelService.update(id, dto));
  }

  // 특정 채널 삭제(channels/{channelId})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Channel 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음")
  })
  @RequestMapping(value = "/{channel-id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Channel ID")
      @PathVariable("channel-id") UUID id) {
    channelService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // 특정 사용자가 속한 모든 채널 목록 조회(channels?user-id=...)
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @ApiResponse(responseCode = "200", description = "Channel 목록 조회 성공")
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ChannelDto>> readAllByUser(
      @Parameter(description = "조회할 User ID")
      @RequestParam("userId") UUID userId) {
    return ResponseEntity.ok(channelService.findAllByUserId(userId));
  }
}
