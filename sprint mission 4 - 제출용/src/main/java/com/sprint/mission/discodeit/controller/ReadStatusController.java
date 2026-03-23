package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/readstatus")
@RequiredArgsConstructor
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    // create
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusService.create(request));
    }

    // readAllByUserId
    @RequestMapping (method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> readAllByUserId(@RequestParam UUID userId){
        return ResponseEntity.ok(readStatusService.readAllByUserId(userId));
    }

    // update
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody ReadStatusUpdateRequest request){
        readStatusService.update(request);
        return ResponseEntity.ok().build();
    }

}
