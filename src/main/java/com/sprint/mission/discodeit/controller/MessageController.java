package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.DeleteMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.MessageInfoDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    MessageService messageService;


    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageInfoDto> sendMessage(@RequestBody CreateMessageDto createMessageDto){

        MessageInfoDto messageInfoDto = messageService.create(createMessageDto);


        return ResponseEntity.status(HttpStatus.CREATED).body(messageInfoDto);

    }

    @RequestMapping(value = "/{channelId}",method = RequestMethod.GET)
    public ResponseEntity<List<MessageInfoDto>> getMessage(UUID channelId){

        return ResponseEntity.status(HttpStatus.OK).body(messageService.findAllById(channelId));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<MessageInfoDto> updateMessage(@RequestBody UpdateMessageDto updateMessageDto){
        messageService.updateMessage(updateMessageDto);
        return ResponseEntity.status(HttpStatus.OK).body(messageService.find(updateMessageDto.messageId()));

    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(@RequestBody DeleteMessageDto deleteMessageDto){

        messageService.deleteMessage(deleteMessageDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
