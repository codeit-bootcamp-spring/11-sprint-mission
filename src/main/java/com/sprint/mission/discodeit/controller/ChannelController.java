package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @PostMapping("/public")
    public ResponseEntity<Channel> createPublicChannel(
            @Valid @RequestBody PublicChannelCreateRequest request
    ) {
        Channel createdChannel = channelService.createPublicChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
    }

    @PostMapping("/private")
    public ResponseEntity<Channel> createPrivateChannel(
            @Valid @RequestBody PrivateChannelCreateRequest request
    ) {
        Channel createdChannel = channelService.createPrivateChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable UUID channelId,
            @Valid @RequestBody PublicChannelUpdateRequest request
    ) {
        Channel updatedChannel = channelService.updateChannel(channelId, request);
        return ResponseEntity.ok(updatedChannel);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable UUID channelId
    ) {
        channelService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }
}