package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.dto.MessageUpdateApiRequest;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.service.dto.message.MessageDto;
import com.sprint.mission.discodeit.service.dto.message.UpdateMessageRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public PageResponse<MessageDto> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(defaultValue = "50") int size
    ) {
        return messageService.findAllByChannelId(channelId, cursor, size);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageDto create(
            @Valid @RequestPart("messageCreateRequest") CreateMessageRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        log.debug("메시지 생성 요청: channelId={}", messageCreateRequest.channelId());
        return messageService.create(messageCreateRequest, attachments);
    }

    @PatchMapping("/{messageId}")
    public MessageDto update(
            @PathVariable UUID messageId,
            @Valid @RequestBody MessageUpdateApiRequest request
    ) {
        log.debug("메시지 수정 요청: messageId={}", messageId);
        return messageService.update(new UpdateMessageRequest(messageId, request.newContent()));
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID messageId) {
        log.debug("메시지 삭제 요청: messageId={}", messageId);
        messageService.delete(messageId);
    }
}
