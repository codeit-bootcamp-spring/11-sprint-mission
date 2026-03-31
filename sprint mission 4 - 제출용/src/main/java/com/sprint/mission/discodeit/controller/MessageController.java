package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.DiscodeitIdMismatchException;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.io.IOException;
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
    return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(request));
  }

  // create (multipart/form-data) - 첨부파일 업로드 지원 (Postman form-data)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> createMultipart(
      @RequestParam String content,
      @RequestParam UUID authorId,
      @RequestParam UUID channelId,
      @RequestParam(required = false) UUID receiverId,
      @RequestParam(value = "file", required = false) MultipartFile file
  ) {
    String fileName = null;
    byte[] fileContent = null;
    String contentType = null;

    if (file != null && !file.isEmpty()) {
      try {
        fileName = file.getOriginalFilename();
        fileContent = file.getBytes();
        contentType = file.getContentType();
      } catch (IOException e) {
        throw new DiscodeitInvalidInputException("첨부파일을 읽을 수 없습니다.");
      }
    }

    MessageCreateRequest request = new MessageCreateRequest(
        content,
        authorId,
        channelId,
        receiverId,
        fileName,
        fileContent,
        contentType
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(request));
  }

  // update
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> update(@PathVariable UUID id,
      @Valid @RequestBody MessageUpdateRequest request) {
    if (!id.equals(request.getMessageId())) {
      throw DiscodeitIdMismatchException.message(id, request.getMessageId());
    }
    messageService.update(new MessageUpdateRequest(id, request.getMessageContent()));
    return ResponseEntity.ok().build();
  }

  // read
  @GetMapping("/{id}")
  public ResponseEntity<Message> read(@PathVariable UUID id) {
    return ResponseEntity.ok(messageService.read(id));
  }

  // readAllByChannelId
  @GetMapping
  public ResponseEntity<List<Message>> readAllByChannelId(@PathVariable UUID channelId) {
    return ResponseEntity.ok(messageService.readAllByChannelId(channelId));
  }

  // delete
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    messageService.delete(id);
    return ResponseEntity.noContent().build();
  }
}