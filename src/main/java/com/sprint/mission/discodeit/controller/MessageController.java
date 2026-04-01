package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public MessageDto sendMessage(@RequestBody MessageCreateRequest request) {
        return messageService.create(request);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT)
    public void updateMessage(@PathVariable UUID messageId,
                              @RequestBody MessageUpdateRequest request) {
        messageService.update(messageId, request);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<MessageDto> getMessageByChannelId(@RequestParam UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }
}
