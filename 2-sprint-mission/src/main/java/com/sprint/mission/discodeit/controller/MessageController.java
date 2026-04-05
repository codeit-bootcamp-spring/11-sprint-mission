package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto.Response> create(
            @Valid @RequestPart("request") MessageDto.CreateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        List<BinaryContentDto.CreateRequest> fileRequests = convertToFileDtos(attachments);
        MessageDto.Response response = messageService.create(request, fileRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<MessageDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody MessageDto.UpdateRequest request) {
        MessageDto.Response response = messageService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageDto.Response>> findAllByChannelId(
            @RequestParam UUID channelId) {
        List<MessageDto.Response> responseList = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(responseList);
    }

    // 파일을 DTO 형태로 변환
    private List<BinaryContentDto.CreateRequest> convertToFileDtos(List<MultipartFile> attachments) {
        return Optional.ofNullable(attachments)
                .orElse(Collections.emptyList())
                .stream()
                .map(file -> {
                    try {
                        return BinaryContentDto.CreateRequest.builder()
                                .fileName(file.getOriginalFilename())
                                .size(file.getSize())
                                .contentType(file.getContentType())
                                .bytes(file.getBytes())
                                .build();
                    } catch (IOException e) {
                        throw new BusinessException(ErrorCode.FILE_READ_FAILED);
                    }
                })
                .toList();
    }
}
