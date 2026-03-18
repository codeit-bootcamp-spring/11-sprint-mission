package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
import com.sprint.mission.discodeit.dto.user.SignUpResponseDTO;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.data.user-path=./build/test-data/users.dat",
        "app.data.channel-path=./build/test-data/channels.dat",
        "app.data.userchannel-path=./build/test-data/userchannels.dat",
        "app.data.readstatus-path=./build/test-data/readstatus.dat"
})
class ChannelServiceTest {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    private UUID masterId;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO master = userService.signUp(new SignUpRequestDTO("master_" + uniqueId, uniqueId + "@test.com", "password", null));
        this.masterId = master.id();
    }

    @Test
    @DisplayName("PUBLIC 채널을 생성할 수 있다.")
    void createPublicChannelTest() {
        // given
        CreatePublicChannelRequestDTO req = new CreatePublicChannelRequestDTO(masterId, "Java Study", "자바 스터디 방입니다.");

        // when
        ChannelResponseDTO res = channelService.createPublicChannel(req);

        // then
        assertThat(res.channelId()).isNotNull();
        assertThat(res.type()).isEqualTo(ChannelType.PUBLIC);
        assertThat(res.name()).isEqualTo("Java Study");
    }

    @Test
    @DisplayName("PRIVATE 채널 생성 시 참가자 목록이 정상적으로 등록된다.")
    void createPrivateChannelTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO friend = userService.signUp(new SignUpRequestDTO("friend_" + uniqueId, uniqueId + "2@test.com", "password", null));

        CreatePrivateChannelRequestDTO req = new CreatePrivateChannelRequestDTO(masterId, Set.of(friend.id()));

        // when
        ChannelResponseDTO res = channelService.createPrivateChannel(req);

        // then
        assertThat(res.type()).isEqualTo(ChannelType.PRIVATE);
        assertThat(res.participantIds()).contains(masterId, friend.id());
    }

    @Test
    @DisplayName("방장이 아닌 유저가 채널을 수정하려고 하면 예외가 발생한다.")
    void updateChannelUnauthorizedTest() {
        // given
        ChannelResponseDTO channel = channelService.createPublicChannel(new CreatePublicChannelRequestDTO(masterId, "Test Room", "Desc"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO otherUser = userService.signUp(new SignUpRequestDTO("other_" + uniqueId, uniqueId + "3@test.com", "password", null));

        // when & then
        UpdateChannelRequestDTO updateReq = new UpdateChannelRequestDTO(otherUser.id(), channel.channelId(), "Hack Room", "Hacked");
        assertThatThrownBy(() -> channelService.updateChannel(updateReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    @Test
    @DisplayName("PRIVATE 채널은 수정하려고 하면 예외가 발생한다.")
    void updatePrivateChannelThrowsException() {
        // given
        CreatePrivateChannelRequestDTO req = new CreatePrivateChannelRequestDTO(masterId, Set.of());
        ChannelResponseDTO privateChannel = channelService.createPrivateChannel(req);

        // when & then
        UpdateChannelRequestDTO updateReq = new UpdateChannelRequestDTO(masterId, privateChannel.channelId(), "New Name", "New Desc");
        assertThatThrownBy(() -> channelService.updateChannel(updateReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Private 채널은 수정할 수 없습니다");
    }

    @Test
    @DisplayName("특정 유저의 채널 목록 조회 시 본인이 속한 PRIVATE과 모든 PUBLIC 채널이 조회된다.")
    void findAllByUserIdTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO otherUser = userService.signUp(new SignUpRequestDTO("other_" + uniqueId, uniqueId + "@test.com", "password", null));

        channelService.createPublicChannel(new CreatePublicChannelRequestDTO(masterId, "Pub Room", "desc"));
        channelService.createPrivateChannel(new CreatePrivateChannelRequestDTO(masterId, Set.of(otherUser.id()))); // otherUser 참여
        ChannelResponseDTO myPrivate = channelService.createPrivateChannel(new CreatePrivateChannelRequestDTO(masterId, Set.of())); // otherUser 미참여

        // when
        FindChannelsResponseDTO res = channelService.findAllByUserId(otherUser.id());

        // then
        List<UUID> visibleChannelIds = res.list().stream().map(ChannelResponseDTO::channelId).toList();
        assertThat(visibleChannelIds).doesNotContain(myPrivate.channelId()); // 참여하지 않은 PRIVATE 채널은 안 보여야 함
    }
}