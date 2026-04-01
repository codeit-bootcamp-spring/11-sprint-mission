package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {
    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelDto createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        return channelService.createPublic(request);
    }

    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelDto createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        return channelService.createPrivate(request);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelDto> getChannelByUserId(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public void updateChannel(@PathVariable UUID channelId,
                              @RequestBody ChannelUpdateRequest request) {
        channelService.update(channelId, request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void deleteChannel(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }
}
