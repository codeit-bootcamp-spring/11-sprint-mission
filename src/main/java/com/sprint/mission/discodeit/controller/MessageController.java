package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(EndPoints.MESSAGE)
@Tag(name = "Message", description = "Message API")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  // 메시지 보내기(생성)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Message 생성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음")
  })
  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> create(
      @Parameter(description = "Message 생성 정보")
      @RequestPart("messageCreateRequest") MessageCreateRequest dto,
      @Parameter(description = "Message 첨부 파일들")
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(dto, attachments));
  }

  // 메시지 수정
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Message 내용 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수행됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음")
  })
  @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
  public ResponseEntity<Message> update(
      @Parameter(description = "수정할 Message ID")
      @PathVariable("messageId") UUID id,
      @RequestBody MessageUpdateRequest dto) {
    return ResponseEntity.ok(messageService.update(id, dto));
  }

  // 메시지 삭제
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Message 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음")
  })
  @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Message ID")
      @PathVariable("messageId") UUID id) {
    messageService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // 특정 채널의 모든 메시지를 조회
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary = "Channel의 Message 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 목록 조회 성공")
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<PageResponse<MessageDto>> readAllByChannelId(
      @Parameter(description = "조회할 Channel ID")
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId, cursor));
  }
}