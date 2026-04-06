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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatuses")
public class MessageReceiptController {

    private final ReadStatusService readStatusService;

    public MessageReceiptController(ReadStatusService readStatusService) {
        this.readStatusService = readStatusService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusDto create(@RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    public ReadStatusDto update(@PathVariable UUID readStatusId,
                                @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(
                new ReadStatusUpdateParam(readStatusId, request)
        );
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatusDto> findAllByUserId(@RequestParam UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}