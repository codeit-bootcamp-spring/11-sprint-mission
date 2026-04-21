package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.dto.ChannelUpdateApiRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.service.dto.channel.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.service.dto.channel.UpdateChannelRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {
    private final ChannelService channelService;

    @RequestMapping(method = RequestMethod.GET)
    public List<ChannelDto> findAllByUserId(@RequestParam UUID userId) {
        return channelService.findAllByUserId(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ChannelDto createPublic(@RequestBody CreatePublicChannelRequest request) {
        return channelService.createPublicChannel(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ChannelDto createPrivate(@RequestBody CreatePrivateChannelRequest request) {
        return channelService.createPrivateChannel(request);
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ChannelDto update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateApiRequest request
    ) {
        return channelService.update(new UpdateChannelRequest(
                channelId,
                request.newName(),
                request.newDescription()
        ));
    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID channelId) {
        channelService.delete(channelId);
    }
}
