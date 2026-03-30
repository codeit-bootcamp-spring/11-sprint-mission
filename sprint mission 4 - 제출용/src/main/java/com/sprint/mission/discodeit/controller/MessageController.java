package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
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

  // create
  @PostMapping
  public ResponseEntity<Message> create(@Valid @RequestBody MessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(request));
  }

  // read
  @GetMapping("/{id}")
  public ResponseEntity<Message> read(@PathVariable UUID id) {
    return ResponseEntity.ok(messageService.read(id));
  }

  // readAllByChannelId
  @GetMapping
  public ResponseEntity<List<Message>> readAllByChannelId(@RequestParam UUID channelId) {
    return ResponseEntity.ok(messageService.readAllByChannelId(channelId));
  }

  // update
  @PutMapping("/{id}")
  public ResponseEntity<Void> update(@PathVariable UUID id,
      @Valid @RequestBody MessageUpdateRequest request) {
    messageService.update(request);
    return ResponseEntity.ok().build();
  }

  // delete
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    messageService.delete(id);
    return ResponseEntity.noContent().build();
  }
}