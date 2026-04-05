package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class MessageReceiptController {

    private final ReadStatusService readStatusService;

    public MessageReceiptController(ReadStatusService readStatusService) {
        this.readStatusService = readStatusService;
    }

    @RequestMapping(value = "/channels/{channelId}/message-receipts", method = RequestMethod.POST)
    public ReadStatusDto create(@PathVariable UUID channelId,
                                @RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(
                new ReadStatusCreateRequest(
                        request.userId(),
                        channelId,
                        request.lastReadAt()
                )
        );
    }

    @RequestMapping(value = "/channels/{channelId}/message-receipts/{messageReceiptId}", method = RequestMethod.PUT)
    public ReadStatusDto update(@PathVariable UUID channelId,
                                @PathVariable UUID messageReceiptId,
                                @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(
                new ReadStatusUpdateParam(messageReceiptId, request)
        );
    }

    @RequestMapping(value = "/users/{userId}/message-receipts", method = RequestMethod.GET)
    public List<ReadStatusDto> findAllByUserId(@PathVariable UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}