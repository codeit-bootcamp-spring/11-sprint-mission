package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/read-status")
@RequiredArgsConstructor
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusResponse create(@RequestBody ReadStatusCreateRequest request) {
        return readStatusService.create(request);
    }

    @ResponseBody
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.GET)
    public ReadStatusResponse findById(@PathVariable UUID readStatusId) {
        return readStatusService.findById(readStatusId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public List<ReadStatusResponse> findAllByUserId(@RequestParam UUID readStatusId) {
        return readStatusService.findAllByUserId(readStatusId);
    }

    @ResponseBody
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PUT)
    public ReadStatusResponse update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    ) {
        return readStatusService.update(readStatusId, request);
    }

    @ResponseBody
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID readStatusId) {
        readStatusService.delete(readStatusId);
    }
}
