package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@RestController
public class ChannelController {
    private final ChannelService channelService;

    @RequestMapping(
            path = "public",
            method = RequestMethod.POST
    )
    public ResponseEntity<ChannelResponse> create(
            @RequestBody PublicChannelCreateRequest publicChannelCreateRequest
    ) {
        ChannelResponse createdChannel = this.channelService.createPublicChannel(publicChannelCreateRequest);
        URI location = MvcUriComponentsBuilder
                .fromController(ChannelController.class)
                .path("/{id}")
                .buildAndExpand(createdChannel.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdChannel);
    }

    @RequestMapping(
            path = "private",
            method = RequestMethod.POST
    )
    public ResponseEntity<ChannelResponse> create(
            @RequestBody PrivateChannelCreateRequest privateChannelCreateRequest
    ) {
        ChannelResponse createdChannel = this.channelService.createPrivateChannel(privateChannelCreateRequest);
        URI location = MvcUriComponentsBuilder
                .fromController(ChannelController.class)
                .path("/{id}")
                .buildAndExpand(createdChannel.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdChannel);
    }

    @RequestMapping(
            path = "{channelId}",
            method = RequestMethod.PATCH
    )
    public ResponseEntity<ChannelResponse> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest channelUpdateRequest
    ) {
        ChannelResponse updatedChannel = this.channelService.updateChannel(channelId, channelUpdateRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedChannel);
    }
}
