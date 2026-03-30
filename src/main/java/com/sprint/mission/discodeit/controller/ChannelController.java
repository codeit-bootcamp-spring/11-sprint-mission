package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channel")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    //공개 채널 생성
    @RequestMapping(method = RequestMethod.POST, value = "/public")
    public ResponseEntity<Void> createPublicChannel(@RequestBody PublicChannelCreateRequest request) {
        UUID channelId = channelService.createPublicChannel(request);
        return ResponseEntity.created(URI.create("/api/channel/" + channelId)).build();
    }

    //비공개 채널 생성
    @RequestMapping(method = RequestMethod.POST, value = "/private")
    public ResponseEntity<Void> createPrivateChannel(@RequestBody PrivateChannelCreateRequest request) {
        UUID channelId = channelService.createPrivateChannel(request);
        return ResponseEntity.created(URI.create("/api/channel/" + channelId)).build();
    }

    //공개 채널 정보 수정
    @RequestMapping(method = RequestMethod.PUT, value = "/{channelId}")
    public ResponseEntity<Void> updateChannel(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        channelService.update(channelId, request);
        return ResponseEntity.ok().build();
    }

    //채널 삭제
    @RequestMapping(method = RequestMethod.DELETE, value = "/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }

    //특정 사용자가 볼 수 있는 모든 채널 목록 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelResponse>> getChannelsForUser(@RequestParam UUID userId) {
        List<ChannelResponse> channels = channelService.findAllByUserId(userId);
        return ResponseEntity.ok(channels);
    }

    //채널에 유저 추가
    @RequestMapping(method = RequestMethod.POST, value = "/{channelId}/user/{userId}")
    public ResponseEntity<Void> addUserToChannel(
            @PathVariable UUID channelId,
            @PathVariable UUID userId
    ) {
        channelService.addUserToChannel(userId, channelId);
        return ResponseEntity.ok().build();
    }

    //채널에서 유저 제외
    @RequestMapping(method = RequestMethod.DELETE, value = "/{channelId}/user/{userId}")
    public ResponseEntity<Void> removeUserFromChannel(
            @PathVariable UUID channelId,
            @PathVariable UUID userId
    ) {
        channelService.removeUserFromChannel(userId, channelId);
        return ResponseEntity.noContent().build();
    }
}
