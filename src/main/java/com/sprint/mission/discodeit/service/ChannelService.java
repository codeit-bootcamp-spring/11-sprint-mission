package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channeldto.*;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    PrivateChannelInfoDto createPrivate(CreatePrivateChannelDto createPrivateChannelDto);
    PublicChannelInfoDto createPublic(CreatePublicChannelDto createPublicChannelDto);

    PrivateChannelInfoDto findPrivate(UUID channelId, UUID memberId);
    PublicChannelInfoDto findPublic(UUID channelId);

    List<PublicChannelInfoDto> findAllById(UUID userId);

    PublicChannelInfoDto updateChannel(UpdateChannelDto updateChannelDto);


    void addMember(ChannelMemberDto channelMemberDto);
    void removeMember(ChannelMemberDto channelMemberDto);


    void deleteChannel(DeleteChannelDto deleteChannelDto);


}
