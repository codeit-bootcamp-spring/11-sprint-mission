package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.DeleteMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageInfoDto> sendMessage(@ModelAttribute CreateMessageDto createMessageDto){

        MessageInfoDto messageInfoDto = messageService.create(createMessageDto);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(messageInfoDto.messageId())
                .toUri();
        return ResponseEntity.created(uri).body(messageInfoDto);




    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.GET)
    public ResponseEntity<List<MessageInfoDto>> getMessage(@PathVariable("channelId") UUID channelId){

        return ResponseEntity.status(HttpStatus.OK).body(messageService.findAllById(channelId));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<MessageInfoDto> updateMessage(@ModelAttribute UpdateMessageDto updateMessageDto){
        messageService.updateMessage(updateMessageDto);
        return ResponseEntity.status(HttpStatus.OK).body(messageService.find(updateMessageDto.messageId()));

    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(@RequestBody DeleteMessageDto deleteMessageDto){

        messageService.deleteMessage(deleteMessageDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
