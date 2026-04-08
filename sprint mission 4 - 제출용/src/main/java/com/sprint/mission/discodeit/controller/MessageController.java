package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
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

  // POST /api/messages - 201 Created
  // Json만 입력받기
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Message> create(@Valid @RequestBody MessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request));
  }

  // POST /api/messages - 201 Created
  // multipart/form-data만 입력받기
  // Postman에서는 Json파일로 넘겨야 작동함.
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> createMultipart(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request));
  }

  // PUT /api/messages/{messageId} - 200 OK
  @PutMapping(value = "/{messageId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Message> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request
  ) {
    messageService.update(new MessageUpdateRequest(messageId, request.getNewContent()));
    return ResponseEntity.ok(messageService.read(messageId));
  }

  // GET /api/messages/{messageId} - 200 OK
  @GetMapping("/{messageId}")
  public ResponseEntity<Message> read(@PathVariable UUID messageId) {
    return ResponseEntity.ok(messageService.read(messageId));
  }

  // GET /api/messages?channelId=123 - 200 OK
  @GetMapping
  public ResponseEntity<List<Message>> readAllByChannelId(@RequestParam UUID channelId) {
    return ResponseEntity.ok(messageService.readAllByChannelId(channelId));
  }

  // DELETE /api/messages/{messageId} - 204 No Content
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build();
  }
}