package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/readStatus")
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest dto) {
        ReadStatus rs = readStatusService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rs);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<ReadStatus> update(@RequestBody ReadStatusUpdateRequest dto) {
        ReadStatus rs = readStatusService.update(dto);
        return ResponseEntity.ok(rs);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> findAllByUserId(@RequestParam("userId") UUID userId) {
        List<ReadStatus> readStatusList = readStatusService.findAllByUserId(userId);
        return ResponseEntity.ok(readStatusList);
    }
}
