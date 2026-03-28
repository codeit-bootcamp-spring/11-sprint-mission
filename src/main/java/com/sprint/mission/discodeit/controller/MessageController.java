package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@RestController
public class MessageController {
    private final MessageService messageService;

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MessageResponse> create(
            @RequestPart(value = "messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<BinaryContentCreateRequest> attachmentsRequest = BinaryContentCreateRequest.listFrom(attachments);

        MessageResponse createdMessage = this.messageService.createMessage(messageCreateRequest, attachmentsRequest);
        URI location = MvcUriComponentsBuilder
                .fromController(MessageController.class)
                .path("/{id}")
                .buildAndExpand(createdMessage.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdMessage);
    }

    @RequestMapping(
            path = "{messageId}",
            method = RequestMethod.PATCH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MessageResponse> update(
            @PathVariable UUID messageId,
            @RequestPart(value = "messageUpdateRequest", required = false) MessageUpdateRequest messageUpdateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        List<BinaryContentCreateRequest> attachmentsRequest = BinaryContentCreateRequest.listFrom(attachments);

        MessageResponse updatedMessage = this.messageService.updateMessage(messageId, messageUpdateRequest, attachmentsRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedMessage);
    }

    @RequestMapping(
            path = "{messageId}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId
    ) {
        this.messageService.deleteMessage(messageId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
