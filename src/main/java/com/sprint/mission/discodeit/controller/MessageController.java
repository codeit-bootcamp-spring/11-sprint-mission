package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createMessage(@RequestBody MessageCreateRequest request) {
        messageService.create(request);
        return ResponseEntity.created(URI.create("/api/message")).build();
    }

    // 메시지 수정
    @RequestMapping(method = RequestMethod.PUT, value = "/{messageId}")
    public ResponseEntity<Void> updateMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        messageService.update(messageId, request);
        return ResponseEntity.ok().build();
    }

    // 메시지 삭제
    @RequestMapping(method = RequestMethod.DELETE, value = "/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    //특정 채널의 메시지 목록을 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getMessagesByChannel(@RequestParam UUID channelId) {
        List<Message> messages = messageService.readAllByChannelId(channelId);
        return ResponseEntity.ok(messages);
    }
}
