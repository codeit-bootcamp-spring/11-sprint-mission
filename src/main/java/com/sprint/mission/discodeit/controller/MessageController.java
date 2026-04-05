package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequestDto;
import com.sprint.mission.discodeit.dto.MessageResponseDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequestDto;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/channels/{channelId}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final BasicMessageService messageService;

    // 1. 메시지 목록 조회(특정 채널)
    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponseDto> findAllByChannelId(@PathVariable UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }

    // 2. 메시지 전송(첨부 포함)
    @RequestMapping(method = RequestMethod.POST)
    public MessageResponseDto createMessage(
            @PathVariable UUID channelId,
            @RequestBody MessageCreateRequestDto dto) {
        return messageService.create(channelId, dto);
    }

    // 3. 메시지 수정
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT)
    public MessageResponseDto updateMessage(
            @PathVariable UUID channelId,
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequestDto dto) {
        return messageService.update(channelId, messageId, dto);
    }

    // 4. 메시지 삭제
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void deleteMessage(
            @PathVariable UUID channelId,
            @PathVariable UUID messageId) {
        messageService.delete(channelId, messageId);
    }
}

