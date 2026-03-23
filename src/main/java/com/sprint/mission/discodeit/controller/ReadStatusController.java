package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    @RequestMapping(value = "/channel/{channelId}/readStatus", method = RequestMethod.POST)
    public ResponseEntity<Void> create(@PathVariable UUID channelId, @RequestBody ReadStatusCreateRequestDto dto) {
        readStatusService.create(channelId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @RequestMapping(value = "/channel/{channelId}/readStatus", method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID channelId, @RequestBody ReadStatusUpdateRequestDto dto) {
        readStatusService.updateByChannelId(channelId, dto);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/user/{userId}/readStatus", method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatusResponseDto>> findAllByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }

}
