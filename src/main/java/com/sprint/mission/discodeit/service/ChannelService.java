package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    // 채널 생성 - Public
    ChannelResponseDTO createPublicChannel(CreatePublicChannelRequestDTO dto);

    // 채널 생성 - Private
    ChannelResponseDTO createPrivateChannel(CreatePrivateChannelRequestDTO dto);

    // 특정 채널 조회 (find)
    ChannelResponseDTO getChannels(UUID channelId);

    // 채널 전체 조회 (findAll)
    FindChannelsResponseDTO findAllByUserId(UUID userId);

    // 내 채널 목록 조회
    FindChannelsResponseDTO getUserChannels(UUID userId);

    // 채널 정보 수정
    ChannelResponseDTO updateChannel(UpdateChannelRequestDTO dto);

    // 채널 삭제
    void deleteChannel(DeleteChannelRequestDTO dto);

    // 채널 가입
    void joinChannel(JoinChannelRequestDTO dto);

    // 채널 탈퇴
    void leaveChannel(LeaveChannelRequestDTO dto);
}
