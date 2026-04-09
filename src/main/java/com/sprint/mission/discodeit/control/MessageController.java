package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdate;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import java.net.URI;
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
public class MessageController {

  private final MessageService messageService;

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<MessageDto> createMessage(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid @RequestPart("messageCreateRequest") CreateMessageRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    MessageDto createdMessage = messageService.createMessage(request, attachments);
    URI location = URI.create("/api/messages/" + createdMessage.id());
    return ResponseEntity.created(location).body(createdMessage);
  }

  @GetMapping(params = "channelId")
  public ResponseEntity<List<MessageDto>> readMessageByChannelId(
      @RequestParam UUID channelId) {
    List<MessageDto> messages = messageService.readMessagesByChannel(channelId);
    return ResponseEntity.ok(messages);
  }

  @DeleteMapping("/{messageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
    messageService.deleteMessage(messageId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto> updateMessage(@PathVariable UUID messageId,
      @RequestBody MessageUpdate request) {
    MessageDto updatedMessage = messageService.updateMessage(messageId, request);
    return ResponseEntity.ok(updatedMessage);
  }
}
