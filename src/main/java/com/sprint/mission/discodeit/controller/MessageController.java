package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.exception.InvalidException;
import com.sprint.mission.discodeit.service.MessageService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MessageCreateMultipartDoc.class)
                    )
            )
    )

    @Timed("message.create.async")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto create(
            @Parameter(hidden = true)
            @Valid
            @RequestPart("messageCreateRequest") MessageCreatePart part,
            @Parameter(hidden = true)
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        log.info("메시지 생성 API 요청: authorId={}, channelId={}",
                part.authorId(),
                part.channelId()
        );

        MessageCreateRequest request = new MessageCreateRequest(
                part.authorId(),
                part.channelId(),
                part.content(),
                toBinaryContentCreateRequests(attachments));

        return messageService.create(request);
    }


    @PatchMapping(value = "/{messageId}")
    public MessageDto update(@PathVariable UUID messageId,
                             @Valid @RequestBody MessageUpdateRequest request) {
        log.info("메시지 수정 API 요청: messageId={}", messageId);
        return messageService.update(new MessageUpdateParam(messageId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{messageId}")
    public void delete(@PathVariable UUID messageId) {
        log.info("메시지 삭제 API 요청: messageId={}", messageId);
        messageService.delete(messageId);
    }

    @GetMapping
    public PageResponse<MessageDto> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size
    ) {
        log.debug("채널 메시지 목록 조회 API 요청: channelId={}, cursor={}, size={}",
                channelId,
                cursor,
                size
        );
        return messageService.findAllByChannelId(channelId, cursor, size);
    }

    private List<BinaryContentCreateRequest> toBinaryContentCreateRequests(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return null;
        }

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    try {
                        return new BinaryContentCreateRequest(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes()
                        );
                    } catch (Exception e) {
                        log.error("메시지 첨부파일 처리 실패: fileName={}", file.getOriginalFilename(), e);
                        throw new InvalidException("파일 처리 중 오류가 발생했습니다.");
                    }
                })
                .toList();
    }
}