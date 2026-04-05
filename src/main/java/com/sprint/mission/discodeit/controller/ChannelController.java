package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.response.ApiResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllyByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }

    @PostMapping("/public")
    public ResponseEntity<Channel> createPublicChannel(
            @RequestParam UUID requestUserId,
            @Valid @RequestBody PublicChannelCreateRequest request
    ) {
        Channel createChannel = channelService.createPublicChannel(requestUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createChannel);
    }

    @PostMapping("/private")
    public ResponseEntity<Channel> createPrivateChannel(
            @RequestParam UUID requestUserId,
            @Valid @RequestBody PrivateChannelCreateRequest request
    ) {
        Channel createdChannel = channelService.createPrivateChannel(requestUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<Channel> updateChannel(
            @PathVariable UUID channelId,
            @RequestParam UUID requestUserId,
            @Valid @RequestBody PublicChannelUpdateRequest request
    ) {
        Channel updatedChannel = channelService.updateChannel(requestUserId, channelId, request);
        return ResponseEntity.ok(updatedChannel);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable UUID channelId,
            @RequestParam UUID requestUserId
    ) {
        channelService.deleteChannel(requestUserId, channelId);
        return ResponseEntity.noContent().build();
    }
}
