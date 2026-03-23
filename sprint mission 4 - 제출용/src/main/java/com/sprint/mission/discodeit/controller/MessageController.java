package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    // create
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Message> create(@RequestBody MessageCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(request));
    }

    // read
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Message> read(@PathVariable UUID id){
        return ResponseEntity.ok(messageService.read(id));
    }

    // readAllByChannelId
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Message>> readAllByChannelId(@RequestParam UUID channelId){
        return ResponseEntity.ok(messageService.readAllByChannelId(channelId));
    }

    // update
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody MessageUpdateRequest request){
        messageService.update(request);
        return ResponseEntity.ok().build();
    }

    // delete
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}