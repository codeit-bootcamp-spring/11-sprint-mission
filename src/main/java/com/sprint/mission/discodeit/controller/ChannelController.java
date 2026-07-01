package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    // 공개 채널 생성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ApiResponse<ChannelResponseDTO> createPublicChannel(
            @Valid @RequestBody CreatePublicChannelRequestDTO dto
    ) {
        return ApiResponse.success(channelService.createPublicChannel(dto));
    }

    // 비공개 채널 생성
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ApiResponse<ChannelResponseDTO> createPrivateChannel(
            @Valid @RequestBody CreatePrivateChannelRequestDTO dto
    ) {
        return ApiResponse.success(channelService.createPrivateChannel(dto));
    }

    // 공개 채널 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ApiResponse<ChannelResponseDTO> updatePublicChannel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateChannelRequestDTO dto
    ) {
        return ApiResponse.success(channelService.updateChannel(id, dto));
    }

    // 채널 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<Void> deleteChannel(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteChannelRequestDTO dto
    ) {
        channelService.deleteChannel(id, dto);
        return ApiResponse.success();
    }

    // 특정 사용자가 볼 수 있는 채널 조회
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public ApiResponse<FindChannelsResponseDTO> findChannelByUserId(
            @Valid @PathVariable UUID userId
    ) {
        return ApiResponse.success(channelService.findAllByUserId(userId));
    }
}
