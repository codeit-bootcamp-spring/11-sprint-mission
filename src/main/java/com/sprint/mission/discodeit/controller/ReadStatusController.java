package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(EndPoints.READ_STATUS)
@RequiredArgsConstructor
public class ReadStatusController {
    private final ReadStatusService readStatusService;

    // 특정 채널의 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ReadStatus> create(@RequestBody ReadStatusCreateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readStatusService.create(dto));
    }

    // 특정 채널의 메시지 수신 정보 수정
    @RequestMapping(value = "/{readStatus-id}", method = RequestMethod.PUT)
    public ResponseEntity<ReadStatus> update(@PathVariable("readStatus-id") UUID id, @RequestBody ReadStatusUpdateRequest dto) {
        return ResponseEntity.ok(readStatusService.update(id, dto));
    }

    // 특정 사용자의 메시지 수신 정보 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> readMessageByUserId(@RequestParam("user-id") UUID userId) {
        return ResponseEntity.ok(readStatusService.findAllByUserId(userId));
    }
}
