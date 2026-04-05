package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<ChannelDto.Response> create(
            @Valid @RequestBody ChannelDto.CreatePublicRequest request) {
        ChannelDto.Response response = channelService.createPublicChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequestMapping(value = "/private", method = RequestMethod.POST)
        public ResponseEntity<ChannelDto.Response> create(
            @Valid @RequestBody ChannelDto.CreatePrivateRequest request) {
            ChannelDto.Response response = channelService.createPrivateChannel(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ChannelDto.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ChannelDto.UpdateRequest request) {
        ChannelDto.Response response = channelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelDto.Response>> findAllByUserid(
            @RequestParam UUID userId) {
        List<ChannelDto.Response> responseList = channelService.findAllByUserId(userId);
        return ResponseEntity.ok(responseList);
    }

}
