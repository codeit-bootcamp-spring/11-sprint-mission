package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public Channel createPublic(@RequestBody PublicChannelCreateRequest request) {
        return channelService.create(request);
    }

    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public Channel createPrivate(@RequestBody PrivateChannelCreateRequest request) {
        return channelService.create(request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PUT)
    public Channel update(@PathVariable UUID channelId,
                          @RequestBody PublicChannelUpdateRequest request) {
        return channelService.update(channelId, request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    public List<ChannelDto> findByUser(@PathVariable UUID userId) {
        return channelService.findAllByUserId(userId);
    }


}
