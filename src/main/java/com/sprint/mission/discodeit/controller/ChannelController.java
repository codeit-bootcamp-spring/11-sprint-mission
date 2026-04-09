package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController implements ChannelApi {

    private final ChannelService channelService;

    @PostMapping(value = "/public")
    public ResponseEntity<ChannelDto> createPublicChannel(
            @Valid @RequestBody PublicChannelCreateRequest dto
    ) {
        ChannelDto result = channelService.createPublicChannel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/private")
    public ResponseEntity<ChannelDto> createPrivateChannel(
            @RequestBody PrivateChannelCreateRequest dto
    ) {
        ChannelDto result = channelService.createPrivateChannel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping(value = "/{channelId}")
    public ResponseEntity<Void> update(
            @PathVariable UUID channelId,
            @Valid @RequestBody ChannelUpdateRequest dto
    ) {
        channelService.update(channelId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{channelId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    ) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ChannelDto>> findAllByUserId(
            @RequestParam UUID userId
    ) {
        List<ChannelDto> result = channelService.findAllByUserId(userId);
        return ResponseEntity.ok(result);
    }

}
