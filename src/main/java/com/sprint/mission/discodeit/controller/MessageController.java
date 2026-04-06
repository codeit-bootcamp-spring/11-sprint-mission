package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public MessageController(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MessageCreateMultipartDoc.class)
                    )
            )
    )

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageDto create(
            @Parameter(hidden = true)
            @RequestPart("messageCreateRequest") String messageCreateRequestJson,
            @Parameter(hidden = true)
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        MessageCreatePart part;
        try {
            part = objectMapper.readValue(messageCreateRequestJson, MessageCreatePart.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 형식의 요청입니다.");
        }

        MessageCreateRequest request = new MessageCreateRequest(
                part.authorId(),
                part.channelId(),
                part.content(),
                toBinaryContentCreateRequests(attachments));

        return messageService.create(request);
    }


    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public MessageDto update(@PathVariable UUID messageId,
                             @RequestBody MessageUpdateRequest request) {
        return messageService.update(new MessageUpdateParam(messageId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<MessageDto> findAllByChannelId(@RequestParam UUID channelId) {
        return messageService.findAllByChannelId(channelId);
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
                        throw new IllegalArgumentException("파일 처리 중 오류가 발생했습니다.");
                    }
                })
                .toList();
    }
}