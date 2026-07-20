package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @Timed("message.create.async")
  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<MessageDto> createMessage(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("messageCreateRequest") CreateMessageRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    log.info("메시지 생성 요청 수신 - channelId: {}, attachmentCount: {}", request.channelId(),
        attachments != null ? attachments.size() : 0);
    MessageDto createdMessage = messageService.createMessage(request, attachments);
    URI location = URI.create("/api/messages/" + createdMessage.id());
    log.info("메시지 생성 응답 완료 - messageId: {}", createdMessage.id());
    return ResponseEntity.created(location).body(createdMessage);
  }

  @GetMapping(params = "channelId")
  public ResponseEntity<PageResponse<MessageDto>> readMessageByChannelId(
      @RequestParam UUID channelId,
      @RequestParam(required = false) Instant cursor,
      @RequestParam(defaultValue = "50") int size) {
    PageResponse<MessageDto> messages = messageService.readMessagesByChannel(channelId, cursor,
        size);
    return ResponseEntity.ok(messages);
  }

  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
    log.info("메시지 삭제 요청 수신 - messageId: {}", messageId);
    messageService.deleteMessage(messageId);
    log.info("메시지 삭제 응답 완료 - messageId: {}", messageId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto> updateMessage(@PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request) {
    log.info("메시지 업데이트 요청 수신 - messageId: {}", messageId);
    MessageDto updatedMessage = messageService.updateMessage(messageId, request);
    log.info("메시지 업데이트 응답 완료 - messageId: {}", messageId);
    return ResponseEntity.ok(updatedMessage);
  }
}
