package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestBody MessageCreateRequest request) {
        return messageService.create(request);
    }

    @ResponseBody
    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    public MessageResponse findById(@PathVariable UUID messageId) {
        return messageService.findById(messageId);
    }

    @ResponseBody
    @RequestMapping(value = "/channels/{channelId}", method = RequestMethod.GET)
    public List<MessageResponse> findAllByChannelId(@PathVariable UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }

    @ResponseBody
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT)
    public MessageResponse update(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        return messageService.update(messageId, request);
    }

    @ResponseBody
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }
}
