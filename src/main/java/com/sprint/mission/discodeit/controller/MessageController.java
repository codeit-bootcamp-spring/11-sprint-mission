package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.MessageUpdateParam;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @RequestMapping(value = "/messages", method = RequestMethod.POST)
    public MessageDto create(@RequestBody MessageCreateRequest request) {
        return messageService.create(request);
    }

    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.PUT)
    public MessageDto update(@PathVariable UUID messageId,
                             @RequestBody MessageUpdateRequest request) {
        return messageService.update(new MessageUpdateParam(messageId, request));
    }

    @RequestMapping(value = "/messages/{messageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @RequestMapping(value = "/channels/{channelId}/messages", method = RequestMethod.GET)
    public List<MessageDto> findAllByChannelId(@PathVariable UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }
}