package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

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
    @PostMapping
    public ReadStatusDto create(@Valid @RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    @PatchMapping(value = "/{readStatusId}")
    public ReadStatusDto update(@PathVariable UUID readStatusId,
                                @Valid @RequestBody ReadStatusUpdateRequest request) {
        return readStatusService.update(
                new ReadStatusUpdateParam(readStatusId, request)
        );
    }

    @GetMapping
    public List<ReadStatusDto> findAllByUserId(@RequestParam UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}