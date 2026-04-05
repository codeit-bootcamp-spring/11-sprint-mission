package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/read-statuses")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatusDto.Response> create(
            @Valid @RequestBody ReadStatusDto.CreateRequest request) {
        ReadStatusDto.Response response = readStatusService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ReadStatusDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReadStatusDto.UpdateRequest request) {
        ReadStatusDto.Response response = readStatusService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusDto.Response>> findAllByUserId(
            @RequestParam UUID userId) {
        List<ReadStatusDto.Response> responseList = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(responseList);
    }
}