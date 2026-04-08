package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
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

  @PostMapping
  public ResponseEntity<MessageResponse> create(@RequestBody MessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request)); // 201 반환
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<MessageResponse> findById(@PathVariable UUID messageId) {
    return ResponseEntity.ok(messageService.findById(messageId));
  }

  @GetMapping("/channels/{channelId}")
  public ResponseEntity<List<MessageResponse>> findAllByChannelId(@PathVariable UUID channelId) {
    return ResponseEntity.ok(messageService.findAllByChannelId(channelId));
  }

  @PutMapping("/{messageId}")
  public ResponseEntity<MessageResponse> update(
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request
  ) {
    return ResponseEntity.ok(messageService.update(messageId, request));
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build(); // 204 반환
  }
}
