package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
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
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 1. Channel의 Message 목록 조회
    @GetMapping
    public ResponseEntity<List<MessageDto>> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam UUID requestUserId // 권한 확인을 위한 요청자 ID 파라미터 추가
    ) {
        return ResponseEntity.ok(messageService.findAllByChannelId(requestUserId, channelId));
    }

    // 2. Message 생성 (Multipart 요청)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto> createMessage(
            @RequestPart("messageCreateRequest") @Valid MessageCreateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        // 요청 바디(request) 안에 authorId가 포함되어 있으므로 별도의 requestUserId를 받지 않음
        MessageDto createdMessage = messageService.createMessage(request, attachments);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    // 3. Message 내용 수정
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID requestUserId, // 작성자 확인을 위한 요청자 ID 파라미터 추가
            @Valid @RequestBody MessageUpdateRequest request
    ) {
        MessageDto updatedMessage = messageService.updateMessage(requestUserId, messageId, request);
        return ResponseEntity.ok(updatedMessage);
    }

    // 4. Message 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @RequestParam UUID requestUserId // 작성자 확인을 위한 요청자 ID 파라미터 추가
    ) {
        messageService.deleteMessage(requestUserId, messageId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}