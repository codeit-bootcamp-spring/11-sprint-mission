package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(EndPoints.MESSAGE)
@RequiredArgsConstructor
public class MessageController implements MessageApi {

  private final MessageService messageService;

  // 메시지 보내기(생성)
  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> create(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest dto,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    log.info("[MESSAGE_CREATE_REQUEST] 메시지 생성 요청 - 채널 ID={}, 작성자 ID={}, 첨부파일 수={}",
        dto.channelId(), dto.authorId(), attachments != null ? attachments.size() : 0);
    Message message = messageService.create(dto, attachments);
    log.info("[MESSAGE_CREATE_RESPONSE] 메시지 생성 응답 - 메시지 ID={}", message.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }

  // 메시지 수정
  @Override
  @PatchMapping(value = "/{messageId}")
  public ResponseEntity<Message> update(
      @PathVariable("messageId") UUID id,
      @Valid @RequestBody MessageUpdateRequest dto) {
    log.info("[MESSAGE_UPDATE_REQUEST] 메시지 수정 요청 - 메시지 ID={}", id);
    Message message = messageService.update(id, dto);
    log.info("[MESSAGE_UPDATE_RESPONSE] 메시지 수정 응답 - 메시지 ID={}", id);
    return ResponseEntity.ok(message);
  }

  // 메시지 삭제
  @Override
  @DeleteMapping(value = "/{messageId}")
  public ResponseEntity<Void> delete(
      @PathVariable("messageId") UUID id) {
    log.info("[MESSAGE_DELETE_REQUEST] 메시지 삭제 요청 - 메시지 ID={}", id);
    messageService.delete(id);
    log.info("[MESSAGE_DELETE_RESPONSE] 메시지 삭제 응답 - 메시지 ID={}", id);
    return ResponseEntity.noContent().build();
  }

  // 특정 채널의 모든 메시지를 조회
  @Override
  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> readAllByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId, cursor));
  }
}