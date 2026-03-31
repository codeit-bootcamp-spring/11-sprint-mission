package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageControl {

  private final MessageService messageService;

  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<MessageResponseDto> createMessage(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @RequestPart("messageCreateRequest") CreateMessageRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    MessageResponseDto createdMessage = messageService.createMessage(request, attachments);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
  }

  @GetMapping(params = "channelId")
  public ResponseEntity<List<MessageResponseDto>> readMessageByChannelId(
      @RequestParam UUID channelId) {
    List<MessageResponseDto> messages = messageService.readMessagesByChannel(channelId)
        .stream()
        .map(MessageResponseDto::from)
        .toList();
    return ResponseEntity.ok(messages);
  }

  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
    messageService.deleteMessage(messageId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<Void> updateMessage(@PathVariable UUID messageId,
      @RequestBody MessageUpdate request) {
    messageService.updateMessage(messageId, request);
    return ResponseEntity.ok().build();
  }
}
