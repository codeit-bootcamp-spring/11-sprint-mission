package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    // public create
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity <ChannelResponse> createPublicChannel(@RequestBody PublicChannelCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPublicChannel(request));
    }

    // private create
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<ChannelResponse> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPrivateChannel(request));
    }

    // read
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ChannelResponse> read(@PathVariable UUID id){
        return ResponseEntity.ok(channelService.read(id));
    }

    // readAllByUserId
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponse>> readAllByUserId(@RequestParam UUID userId){
        return ResponseEntity.ok(channelService.readAllByUserId(userId));
    }

    // delete
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // update
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID id,
                                       @RequestBody ChannelUpdateRequest request){
        channelService.update(request);
        return ResponseEntity.ok().build();
    }

    // restore
    @RequestMapping(value = "/{id}/restore", method = RequestMethod.POST)
    public ResponseEntity<Void> restore(@PathVariable UUID id){
        channelService.restore(id);
        return ResponseEntity.ok().build();
    }

}
