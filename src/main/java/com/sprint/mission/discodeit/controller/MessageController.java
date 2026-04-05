package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageDto>> findAllByChannelId(
            @RequestParam UUID channelId
    ) {
        return ResponseEntity.ok(messageService.findAllByChannelId(getLoginUserId(), channelId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto> createMessage(
            @RequestPart("messageCreateRequest") @Valid MessageCreateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        MessageDto createdMessage = messageService.createMessage(request, attachments);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable UUID messageId,
            @Valid @RequestBody MessageUpdateRequest request
    ) {
        MessageDto updatedMessage = messageService.updateMessage(getLoginUserId(), messageId, request);
        return ResponseEntity.ok(updatedMessage);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId
    ) {
        messageService.deleteMessage(getLoginUserId(), messageId);
        return ResponseEntity.noContent().build();
    }

    private UUID getLoginUserId() {
        return UUID.fromString("4073c64c-d65b-44f3-946a-e883a9799022");
    }
}