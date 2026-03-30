package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/read-status")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 특정 채널의 메시지 수신 정보 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createReadStatus(@RequestBody ReadStatusCreateRequest request) {
        readStatusService.create(request);
        return ResponseEntity.created(URI.create("/api/read-status")).build(); // id 반환이 없다면 임의의 URI
    }

    //특정 채널의 메시지 수신 정보 수정
    @RequestMapping(method = RequestMethod.PUT, value = "/{readStatusId}")
    public ResponseEntity<Void> updateReadStatus(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest request
    ) {
        readStatusService.update(readStatusId, request);
        return ResponseEntity.ok().build();
    }

    //특정 사용자의 메시지 수신 정보 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ReadStatus>> getReadStatusByUser(@RequestParam UUID userId) {
        List<ReadStatus> readStatuses = readStatusService.readAllByUserId(userId);
        return ResponseEntity.ok(readStatuses);
    }
}
