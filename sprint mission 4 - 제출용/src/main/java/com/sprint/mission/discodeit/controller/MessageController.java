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

  // create
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Message> create(@Valid @RequestBody MessageCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request));  // 201 Created
  }

  // create (multipart/form-data) - 첨부파일 업로드 지원 (Postman form-data)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> createMultipart(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(messageService.create(request));  // 201 Created
  }

  // update
  @PutMapping(value = "/{messageId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Message> update(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request
  ) {
    messageService.update(new MessageUpdateRequest(messageId, request.getNewContent()));
    return ResponseEntity.ok(messageService.read(messageId)); // 200 OK
  }

  // read
  @GetMapping("/{messageId}")
  public ResponseEntity<Message> read(@PathVariable UUID messageId) {
    return ResponseEntity.ok(messageService.read(messageId)); // 200 OK
  }

  // readAllByChannelId
  @GetMapping
  public ResponseEntity<List<Message>> readAllByChannelId(@RequestParam UUID channelId) {
    return ResponseEntity.ok(messageService.readAllByChannelId(channelId)); // 200 OK
  }

  // delete
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build();  // 204 No Content
  }
}