package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.messagedto.MessageDto;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.messagedto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @ApiResponse(responseCode = "201", description = "메시지 전송")
  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<MessageDto> sendMessage(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(required = false) List<MultipartFile> attachments) {

    MessageDto messageDto = messageService.create(messageCreateRequest, attachments);

    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("{id}")
        .buildAndExpand(messageDto.id())
        .toUri();
    return ResponseEntity.created(uri).body(messageDto);
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> getMessage(
      @RequestParam UUID channelId,
      @RequestParam(required = false) Instant cursor,
      Pageable pageable
  ) {

    return ResponseEntity.status(HttpStatus.OK)
        .body(messageService.findAllByChannelId(channelId, pageable, cursor));
  }

  @ApiResponse(responseCode = "200", description = "메시지 수정")
  @PatchMapping(value = "/{messageId}")
  public ResponseEntity<MessageDto> updateMessage(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest messageUpdateRequest
  ) {
    messageService.update(messageId, messageUpdateRequest);
    return ResponseEntity.status(HttpStatus.OK)
        .body(messageService.find(messageId));
  }

  @ApiResponse(responseCode = "204", description = "메시지 삭제")
  @DeleteMapping(value = "/{messageId}")
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {

    messageService.delete(messageId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }


}
