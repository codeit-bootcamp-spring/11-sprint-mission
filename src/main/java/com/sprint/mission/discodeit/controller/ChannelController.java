package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateParam;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @RequestMapping(value = "/channels/public", method = RequestMethod.POST)
    public ChannelDto createPublic(@RequestBody PublicChannelCreateRequest request) {
        return channelService.createPublic(request);
    }

    @RequestMapping(value = "/channels/private", method = RequestMethod.POST)
    public ChannelDto createPrivate(@RequestBody PrivateChannelCreateRequest request) {
        return channelService.createPrivate(request);
    }

    @RequestMapping(value = "/channels/public/{channelId}", method = RequestMethod.PUT)
    public ChannelDto updatePublic(@PathVariable UUID channelId,
                                   @RequestBody ChannelUpdateRequest request) {
        return channelService.update(new ChannelUpdateParam(channelId, request));
    }

    @RequestMapping(value = "/channels/{channelId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    @RequestMapping(value = "/users/{userId}/channels", method = RequestMethod.GET)
    public List<ChannelDto> findAllByUserId(@PathVariable UUID userId) {
        return channelService.findAllByUserId(userId);
    }
}