package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public Message create(@RequestBody MessageCreateRequest request) {
        return messageService.create(request, List.of());
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT)
    public Message update(@PathVariable UUID messageId,
                          @RequestBody MessageUpdateRequest request) {
        return messageService.update(messageId, request);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId){
        messageService.delete(messageId);
    }

    @RequestMapping(value = "/channel/{channelId}", method = RequestMethod.GET)
    public List<Message> findByChannel(@PathVariable UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }

    @RequestMapping(value = "/read-status{id}", method = RequestMethod.POST)
    public ReadStatus createReadStatus(@RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    @RequestMapping(value = "/read-status/{id}", method = RequestMethod.PUT)
    public ReadStatus updateReadStatus(@PathVariable UUID id,
                                       @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(id, request);
    }

    @RequestMapping(value = "/read-status/user/{userId}", method = RequestMethod.GET)
    public List<ReadStatus> findReadStatus(@PathVariable UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}
