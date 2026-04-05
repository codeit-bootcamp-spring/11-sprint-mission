package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequestDto;
import com.sprint.mission.discodeit.dto.ReadStatusResponseDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequestDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/channels/{channelId}/messages/{messageId}/read-status")
@RequiredArgsConstructor
public class MessageReadStatusController {
    private final ReadStatusService messageReadStatusController;
    private final ReadStatusService readStatusService;

    // 메시지 수신정보(읽음/미읽음) 등록
    @RequestMapping(method = RequestMethod.POST)
    public ReadStatusResponseDto createReadStatus(
            @PathVariable UUID channelId,
            @PathVariable UUID messageId,
            @RequestBody ReadStatusCreateRequestDto dto) {
        return messageReadStatusController.createReadStatus(channelId, messageId, dto);
    }

    // 수신정보 수정
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PUT)
    public ReadStatusResponseDto updateReadStatus(
            @PathVariable UUID channelId,
            @PathVariable UUID messageId,
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequestDto dto) {
        return readStatusService.updateReadStatus(channelId, messageId, readStatusId, dto);
    }

    // 특정 사용자의 수신정보 목록 조회(엔드포인트 자유)
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public List<ReadStatusResponseDto> findReadStatusByUserId(
            @RequestParam UUID userId) {
        return readStatusService.findReadStatusByUserId(userId);
    }
}
