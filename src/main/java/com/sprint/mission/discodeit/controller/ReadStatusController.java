package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
}
