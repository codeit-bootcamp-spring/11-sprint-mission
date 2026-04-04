package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.constant.EndPoints;
import com.sprint.mission.discodeit.dto.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.ChannelReadDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(EndPoints.CHANNEL)
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    // 공개 채널 생성
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<Channel> createPublic(@RequestBody ChannelCreatePublicRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPublic(dto));
    }

    // 비공개 채널 생성
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<Channel> createPrivate(@RequestBody ChannelCreatePrivateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createPrivate(dto));
    }

    // 특정 공개 채널의 정보 수정(channels/{channel-id}?)
    @RequestMapping(value = "/{channel-id}", method = RequestMethod.PUT)
    public ResponseEntity<Channel> updatePublic(@PathVariable("channel-id") UUID id, @RequestBody ChannelUpdateRequest dto) {
        return ResponseEntity.ok(channelService.update(id, dto));
    }

    // 특정 채널 삭제(channels/{channel-id})
    @RequestMapping(value = "/{channel-id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable("channel-id") UUID id) {
        channelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 특정 사용자가 속한 모든 채널 목록 조회(channels?user-id=...)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelReadDto>> readAllByUser(@RequestParam("user-id") UUID userId) {
        return ResponseEntity.ok(channelService.findAllByUserId(userId));
    }
}
