package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponseDTO;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.ReadStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/readstatus")
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;

    // 특정 채널의 메시지 수신 정보를 생성
    @RequestMapping(method = RequestMethod.POST)
    public ApiResponse<ReadStatusResponseDTO> create(
            @Valid @RequestBody CreateReadStatusRequestDTO dto
    ) {
        return ApiResponse.success(readStatusService.create(dto));
    }

    // 특정 채널의 메시지 수신 정보를 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ApiResponse<ReadStatusResponseDTO> update(
            @PathVariable UUID id
    ) {
        return ApiResponse.success(readStatusService.update(id));
    }

    // 특정 사용자의 메세지 수신 정보를 조회
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public ApiResponse<List<ReadStatusResponseDTO>> findAllByUserId(
            @PathVariable UUID userId
    ) {
        return ApiResponse.success(readStatusService.findAllByUserId(userId));
    }
}
