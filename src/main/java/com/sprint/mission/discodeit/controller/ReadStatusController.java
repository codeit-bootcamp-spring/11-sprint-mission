package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.common.ApiResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<ApiResponse> create(
            @RequestBody ReadStatusCreateRequest readStatusCreateRequest
    ) {
        ReadStatusResponse createdReadStatusResponse = this.readStatusService.createReadStatus(readStatusCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(createdReadStatusResponse));
    }

    @RequestMapping(
            path = "{readStatusId}",
            method = RequestMethod.PATCH
    )
    public ResponseEntity<ApiResponse> update(
            @PathVariable UUID readStatusId
    ) {
        ReadStatusResponse updatedReadStatusResponse = this.readStatusService.updateReadStatus(readStatusId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(updatedReadStatusResponse));
    }

    @RequestMapping(
            method = RequestMethod.GET
    )
    public ResponseEntity<ApiResponse> findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ReadStatusResponse> readStatusResponses = this.readStatusService.findAllByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.ok(readStatusResponses));
    }
}
