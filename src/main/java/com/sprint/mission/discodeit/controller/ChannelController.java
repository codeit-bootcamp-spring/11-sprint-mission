package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelResponse;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    // 공개 채널 생성
    @ResponseBody
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelResponse createPublicChannel(
            @RequestBody PublicChannelCreateRequest request
    ) {
        return channelService.createPublicChannel(request);
    }

    // 비공개 채널 생성
    @ResponseBody
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelResponse createPrivateChannel(
            @RequestBody PrivateChannelCreateRequest request
    ) {
        return channelService.createPrivateChannel(request);
    }

    @ResponseBody
    @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
    public ChannelResponse findById(@PathVariable UUID channelId) {
        return channelService.findById(channelId);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelResponse> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return channelService.findAllByUserId(userId);
    }

    @ResponseBody
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public ChannelResponse update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        return channelService.update(channelId, request);
    }

    @ResponseBody
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }
}
