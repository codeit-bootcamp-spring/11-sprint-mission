package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequestMapping("/api/read-statuses")
@RequiredArgsConstructor
@RestController
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    @RequestMapping(
            method = RequestMethod.POST
    )
    public ResponseEntity<ReadStatusResponse> create(
            @RequestBody ReadStatusCreateRequest readStatusCreateRequest
    ) {
        ReadStatusResponse createdReadStatusResponse = this.readStatusService.createReadStatus(readStatusCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReadStatusResponse);
    }

    @RequestMapping(
            path = "{readStatusId}",
            method = RequestMethod.PATCH
    )
    public ResponseEntity<ReadStatusResponse> update(
            @PathVariable UUID readStatusId
    ) {
        ReadStatusResponse updatedReadStatusResponse = this.readStatusService.updateReadStatus(readStatusId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedReadStatusResponse);
    }
}
