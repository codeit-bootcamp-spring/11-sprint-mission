package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.channeldto.*;
import com.sprint.mission.discodeit.dto.error.ExceptionDto;
import com.sprint.mission.discodeit.exception.service.WrongChannelTypeException;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.LineNumberInputStream;
import java.net.URI;
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

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(channelInfo.channelId())
                .toUri();



        return ResponseEntity.created(uri).body(channelInfo);

    }
    @RequestMapping(value = "private", method = RequestMethod.POST)
    public ResponseEntity<PrivateChannelInfoDto> createPrivateChannel(@RequestBody CreatePrivateChannelDto createPrivateChannelDto){

        PrivateChannelInfoDto channelInfo = channelService.createPrivate(createPrivateChannelDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(channelInfo);

    }

    @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
    public ResponseEntity<List<PublicChannelInfoDto>> readChannel(@PathVariable UUID channelId){
        return ResponseEntity.status(HttpStatus.OK).body(channelService.findAllById(channelId));
    }

    @RequestMapping(value = "/findAll/{userId}", method = RequestMethod.GET)
    public ResponseEntity<List<PublicChannelInfoDto>> readAllChannelById(@PathVariable UUID userId){

        List <PublicChannelInfoDto> channels = channelService.findAllById(userId);

        return ResponseEntity.status(HttpStatus.OK).body(channels);

    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<PublicChannelInfoDto> updatePublicChannel(@RequestBody UpdateChannelDto updateChannelDto){

        PublicChannelInfoDto channelInfoDto = channelService.updateChannel(updateChannelDto);

        return ResponseEntity.status(HttpStatus.OK).body(channelInfoDto);

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteChannel(@RequestBody DeleteChannelDto deleteChannelDto){

        channelService.deleteChannel(deleteChannelDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }



    @ExceptionHandler
    public ResponseEntity<ExceptionDto> wrongChannelTypeHandler(WrongChannelTypeException e, HttpServletRequest request){

        ExceptionDto exceptionDto = ExceptionDto.of(
            HttpStatus.BAD_REQUEST,
                e.getMessage(),
                request.getRequestURI()
        );


        return ResponseEntity.status(exceptionDto.code()).body(exceptionDto);







    }

}
