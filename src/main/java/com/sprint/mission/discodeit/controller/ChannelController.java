package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channeldto.*;
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
public class ChannelController
{
    private final ChannelService channelService;



    @RequestMapping(value = "public",method = RequestMethod.POST)
    public ResponseEntity<PublicChannelInfoDto> createPublicChannel(@RequestBody CreatePublicChannelDto createPublicChannelDto){

        PublicChannelInfoDto channelInfo = channelService.createPublic(createPublicChannelDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(channelInfo);

    }
    @RequestMapping(value = "private", method = RequestMethod.POST)
    public ResponseEntity<PrivateChannelInfoDto> createPrivateChannel(@RequestBody CreatePrivateChannelDto createPrivateChannelDto){

        PrivateChannelInfoDto channelInfo = channelService.createPrivate(createPrivateChannelDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(channelInfo);

    }

    @RequestMapping(value = "/{memberId}", method = RequestMethod.GET)
    public ResponseEntity<List<PublicChannelInfoDto>> readChannel(@PathVariable UUID memberId){
        return ResponseEntity.status(HttpStatus.OK).body(channelService.findAllById(memberId));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PublicChannelInfoDto> updatePublicChannel(@RequestBody UpdateChannelDto updateChannelDto){

        PublicChannelInfoDto channelInfoDto = channelService.updateChannel(updateChannelDto);

        return ResponseEntity.status(HttpStatus.OK).body(channelInfoDto);

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteChannel(@RequestBody DeleteChannelDto deleteChannelDto){

        channelService.deleteChannel(deleteChannelDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
