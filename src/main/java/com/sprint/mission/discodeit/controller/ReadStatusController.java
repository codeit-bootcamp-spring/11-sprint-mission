package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/readStatus")
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    public ReadStatusController(ReadStatusService readStatusService) {
        this.readStatusService = readStatusService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusDto createReadStatus(
            @RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PUT)
    public void updateReadStatus(@PathVariable UUID readStatusId,
                                 @RequestBody ReadStatusUpdateRequest request) {
        readStatusService.update(readStatusId, request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatusDto> getReadStatusByUserId(@RequestParam UUID userId) {
        return readStatusService.findAllByUserId(userId);
    }
}
