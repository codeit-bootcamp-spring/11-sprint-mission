package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 메세지 전송
    @RequestMapping(method = RequestMethod.POST)
    public ApiResponse<MessageResponseDTO> sendMessage(
            @Valid @RequestBody SendMessageRequestDTO dto
    ) {
        return ApiResponse.success(messageService.sendMessage(dto));
    }

    // 메세지 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ApiResponse<MessageResponseDTO> updateMessage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMessageRequestDTO dto
    ) {
        return ApiResponse.success(messageService.updateMessage(id, dto));
    }

    // 메세지 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<Void> deleteMessage(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteMessageRequestDTO dto
    ) {
        messageService.deleteMessage(id, dto);
        return ApiResponse.success();
    }

    // 특정 채널의 메시지 목록을 조회
    @RequestMapping(value = "/channel/{channelId}", method = RequestMethod.GET)
    public ApiResponse<GetAllMessagesResponseDTO> findByChannelId(
            @PathVariable UUID channelId,
            @RequestParam UUID userId
    ) {
        return ApiResponse.success(messageService.getMessagesByChannel(userId, channelId));
    }
}
