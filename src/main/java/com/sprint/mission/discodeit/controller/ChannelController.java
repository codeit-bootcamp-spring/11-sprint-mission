package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponseDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequestDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequestDto;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequestDto;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(value = "/channels/public", method = RequestMethod.POST)
    public ResponseEntity<Void> createPublicChannel(@RequestBody PublicChannelCreateRequestDto dto) {
        channelService.createPublicChannel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @RequestMapping(value = "/channels/private", method = RequestMethod.POST)
    public ResponseEntity<Void> createPrivateChannel(@RequestBody PrivateChannelCreateRequestDto dto) {
        channelService.createPrivateChannel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @RequestMapping(value = "/channels/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody ChannelUpdateRequestDto dto) {
        channelService.update(id, dto);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/channels/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/users/{userId}/channels", method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponseDto>> findAllByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

}
