package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponseDto> createMessage(
      @RequestPart("messageCreateRequest") MessageCreateRequestDto dto,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    try {
      MessageResponseDto response = messageService.create(dto.getChannelId(), dto, attachments);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<MessageResponseDto>> findAllByChannelId(
      @RequestParam UUID channelId) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId));
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageResponseDto> updateMessage(
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequestDto dto) {
    try {
      return ResponseEntity.ok(messageService.update(null, messageId, dto));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
    messageService.delete(messageId, messageId);
    return ResponseEntity.noContent().build();
  }
}
