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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelDto createPublic(@RequestBody PublicChannelCreateRequest request) {
        return channelService.createPublic(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelDto createPrivate(@RequestBody PrivateChannelCreateRequest request) {
        return channelService.createPrivate(request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ChannelDto update(@PathVariable UUID channelId,
                                   @RequestBody ChannelUpdateRequest request) {
        return channelService.update(new ChannelUpdateParam(channelId, request));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelDto> findAllByUserId(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }
}