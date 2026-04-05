package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.MultipartFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@RestController
public class MessageController {
    private final MessageService messageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse> create(
            @RequestPart(value = "messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<BinaryContentCreateRequest> attachmentsRequest = MultipartFileUtil.toCreateRequests(attachments);

        MessageResponse createdMessage = this.messageService.createMessage(messageCreateRequest, attachmentsRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponse.ok(createdMessage));
    }

    @PatchMapping(path = "{messageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestResponse> update(
            @PathVariable UUID messageId,
            @RequestPart(value = "messageUpdateRequest", required = false) MessageUpdateRequest messageUpdateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<BinaryContentCreateRequest> attachmentsRequest = MultipartFileUtil.toCreateRequests(attachments);

        MessageResponse updatedMessage = this.messageService.updateMessage(messageId, messageUpdateRequest, attachmentsRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(updatedMessage));
    }

    @DeleteMapping(path = "{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        this.messageService.deleteMessage(messageId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping
    public ResponseEntity<RestResponse> findAllByChannelId(@RequestParam(value = "channelId") UUID channelId) {
        List<MessageResponse> messages = this.messageService.findAllByChannelId(channelId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(messages));
    }
}
