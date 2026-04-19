// MessageController.java
package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;
  private final BinaryContentService binaryContentService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageDto> create(@Valid @RequestBody MessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Transactional
  public ResponseEntity<MessageDto> createMultipart(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) throws IOException {
    MessageDto createdMessage = messageService.create(request);

    if (attachments != null) {
      for (MultipartFile attachment : attachments) {
        if (attachment.isEmpty()) {
          continue;
        }

        if (attachment.getOriginalFilename() == null || attachment.getOriginalFilename()
            .isBlank()) {
          throw DiscodeitInvalidInputException.blankField("fileName");
        }

        binaryContentService.create(new BinaryContentCreateRequest(
            null,
            createdMessage.id(),
            attachment.getOriginalFilename(),
            attachment.getBytes(),
            attachment.getContentType()
        ));
      }
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.find(createdMessage.id()));
  }

  @PutMapping(value = "/{messageId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageDto> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request
  ) {
    messageService.update(new MessageUpdateRequest(messageId, request.getNewContent()));
    return ResponseEntity.ok(messageService.find(messageId));
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<MessageDto> find(@PathVariable UUID messageId) {
    return ResponseEntity.ok(messageService.find(messageId));
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam UUID channelId,
      @RequestParam(defaultValue = "0") int page
  ) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId, page));
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build();
  }
}
