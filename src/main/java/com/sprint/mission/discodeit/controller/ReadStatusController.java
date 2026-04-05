package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ReadStatusApi;
import com.sprint.mission.discodeit.dto.common.RestResponse;
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
public class ReadStatusController implements ReadStatusApi {
    private final ReadStatusService readStatusService;

    @PostMapping
    public ResponseEntity<RestResponse<ReadStatusResponse>> create(@RequestBody ReadStatusCreateRequest readStatusCreateRequest) {
        ReadStatusResponse createdReadStatusResponse = this.readStatusService.createReadStatus(readStatusCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponse.ok(createdReadStatusResponse));
    }

    @PatchMapping(path = "{readStatusId}")
    public ResponseEntity<RestResponse<ReadStatusResponse>> update(@PathVariable UUID readStatusId) {
        ReadStatusResponse updatedReadStatusResponse = this.readStatusService.updateReadStatus(readStatusId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(updatedReadStatusResponse));
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<ReadStatusResponse>>> findAllByUserId(@RequestParam UUID userId) {
        List<ReadStatusResponse> readStatusResponses = this.readStatusService.findAllByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(readStatusResponses));
    }
}
