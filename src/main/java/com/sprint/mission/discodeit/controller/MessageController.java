package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.common.PageResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.MultipartFileUtil;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@RestController
public class MessageController implements MessageApi {

  private final MessageService messageService;

  @Timed("message.create.async")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponse> create(
      @Valid @RequestPart(value = "messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    log.info("message create request: request={}, attachments-count={}", messageCreateRequest,
        attachments != null ? attachments.size() : 0);
    List<BinaryContentCreateRequest> attachmentsRequest = MultipartFileUtil.toCreateRequests(
        attachments);

    MessageResponse createdMessage = this.messageService.createMessage(messageCreateRequest,
        attachmentsRequest);

    log.debug("message create response: {}", createdMessage);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdMessage);
  }

  @PatchMapping(path = "{messageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponse> update(
      @PathVariable UUID messageId,
      @RequestPart(value = "messageUpdateRequest", required = false) MessageUpdateRequest messageUpdateRequest,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    log.info("message update request: id={}, request={}, attachments-count={}", messageId,
        messageUpdateRequest, attachments != null ? attachments.size() : 0);
    List<BinaryContentCreateRequest> attachmentsRequest = MultipartFileUtil.toCreateRequests(
        attachments);

    MessageResponse updatedMessage = this.messageService.updateMessage(messageId,
        messageUpdateRequest, attachmentsRequest);

    log.debug("message update response: {}", updatedMessage);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedMessage);
  }

  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    log.info("message delete request: id={}", messageId);
    this.messageService.deleteMessage(messageId);

    log.debug("message delete response: no-content");
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageResponse>> findAllByChannelId(
      @RequestParam(value = "channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor,
      Pageable pageable
  ) {
    log.info("message find-all-by-channel-id request: channelId={}, cursor={}, pageable={}",
        channelId, cursor, pageable);
    PageResponse<MessageResponse> messages = this.messageService.findAllByChannelId(channelId,
        cursor, pageable);

    log.debug("message find-all-by-channel-id response: size={}, hasNext={}",
        messages.content().size(), messages.hasNext());
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messages);
  }
}
