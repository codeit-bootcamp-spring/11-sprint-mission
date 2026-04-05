package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@RestController
public class ChannelController implements ChannelApi {
    private final ChannelService channelService;

    @PostMapping(path = "public")
    public ResponseEntity<RestResponse<ChannelResponse>> create(@RequestBody PublicChannelCreateRequest publicChannelCreateRequest) {
        ChannelResponse createdChannel = this.channelService.createPublicChannel(publicChannelCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponse.ok(createdChannel));
    }

    @PostMapping(path = "private")
    public ResponseEntity<RestResponse<ChannelResponse>> create(@RequestBody PrivateChannelCreateRequest privateChannelCreateRequest) {
        ChannelResponse createdChannel = this.channelService.createPrivateChannel(privateChannelCreateRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponse.ok(createdChannel));
    }

    @PatchMapping(path = "{channelId}")
    public ResponseEntity<RestResponse<ChannelResponse>> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest channelUpdateRequest
    ) {
        ChannelResponse updatedChannel = this.channelService.updateChannel(channelId, channelUpdateRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(updatedChannel));
    }

    @DeleteMapping(path = "{channelId}")
    public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
        this.channelService.deleteChannel(channelId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<ChannelResponse>>> findAllByUserId(@RequestParam(value = "userId") UUID userId) {
        List<ChannelResponse> channels = this.channelService.findAllByUserId(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestResponse.ok(channels));
    }
}
