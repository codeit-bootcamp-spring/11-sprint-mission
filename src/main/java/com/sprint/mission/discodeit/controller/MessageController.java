package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  public MessageDto sendMessage(@RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    return messageService.create(request, attachments);
  }

  @PatchMapping("/{messageId}")
  public void updateMessage(@PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request) {
    messageService.update(messageId, request);
  }

  @DeleteMapping("/{messageId}")
  public void deleteMessage(@PathVariable UUID messageId) {
    messageService.delete(messageId);
  }

  @GetMapping
  public List<MessageDto> getMessageByChannelId(@RequestParam("channelId") UUID channelId) {
    return messageService.findAllByChannelId(channelId);
  }
}
