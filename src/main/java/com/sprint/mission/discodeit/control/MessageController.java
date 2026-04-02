package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdate;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<MessageResponseDto> createMessage(
      @RequestBody CreateMessageRequest request) {
    MessageResponseDto createdMessage = messageService.createMessage(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity<MessageResponseDto> readMessage(@PathVariable UUID id) {
    Message message = messageService.readMessage(id);
    return ResponseEntity.ok(MessageResponseDto.from(message));
  }

  @RequestMapping(method = RequestMethod.GET, params = "channelId")
  public ResponseEntity<List<MessageResponseDto>> readMessageByChannelId(
      @RequestParam UUID channelId) {
    List<MessageResponseDto> messages = messageService.readMessagesByChannel(channelId)
        .stream()
        .map(MessageResponseDto::from)
        .toList();
    return ResponseEntity.ok(messages);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
    messageService.deleteMessage(id);
    return ResponseEntity.noContent().build();
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
  public ResponseEntity<Void> updateMessage(@PathVariable UUID id,
      @RequestBody MessageUpdate request) {
    messageService.updateMessage(id, request);
    return ResponseEntity.ok().build();
  }
}
