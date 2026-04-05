package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/channels")
public class ChannelController {
    private final ChannelService channelService;

    // 1. 공개 채널 상성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponseDto createPublicChannel(@RequestBody PublicChannelCreateRequestDto dto) {
        return channelService.createPublic(dto);
    }

    // 2. 비공개 채널 생성(참여자 목록 포함)
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponseDto createPrivateChannel(@RequestBody PrivateChannelCreateRequestDto dto) {
        return channelService.createPrivate(dto);
    }

    // 3. 채널 정보 수정
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public ChannelResponseDto updateChannel(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequestDto dto) {
        return channelService.update(channelId, dto);
    }

    // 4. 채널 삭제
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void deleteChannel(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    // 5. 특정 사용자가 볼 수 있는 채널 목록(공개+참여 비공개)
    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponseDto> findAllChannels(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }
}
