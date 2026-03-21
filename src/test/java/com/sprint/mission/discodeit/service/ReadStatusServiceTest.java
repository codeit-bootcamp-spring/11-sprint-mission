package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponseDTO;
import com.sprint.mission.discodeit.dto.channel.CreatePublicChannelRequestDTO;
import com.sprint.mission.discodeit.dto.readstatus.CreateReadStatusRequestDTO;
import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
import com.sprint.mission.discodeit.dto.user.SignUpResponseDTO;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.data.user-path=./build/test-data/users.dat",
        "app.data.channel-path=./build/test-data/channels.dat",
        "app.data.userchannel-path=./build/test-data/userchannels.dat",
        "app.data.readstatus-path=./build/test-data/readstatus.dat"
})
class ReadStatusServiceTest {

    @Autowired private ReadStatusService readStatusService;
    @Autowired private UserService userService;
    @Autowired private ChannelService channelService;

    private UUID masterId;
    private UUID channelId;

    @BeforeEach
    void setUp() {
        // 방장 유저 생성 및 퍼블릭 채널 생성 (생성 시 방장의 ReadStatus도 자동 생성됨)
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("rsMaster_" + uniqueId, uniqueId + "@test.com", "password", null));
        masterId = user.id();

        ChannelResponseDTO channel = channelService.createPublicChannel(new CreatePublicChannelRequestDTO(masterId, "RS Room", "ReadStatus Test Room"));
        channelId = channel.channelId();
    }

    @Test
    @DisplayName("새로운 유저의 ReadStatus를 채널에 대해 정상적으로 생성할 수 있다.")
    void createReadStatusTest() {
        // given: 새로운 유저
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO newUser = userService.signUp(new SignUpRequestDTO("rsUser_" + uniqueId, uniqueId + "2@test.com", "password", null));
        CreateReadStatusRequestDTO req = new CreateReadStatusRequestDTO(channelId, newUser.id());

        // when
        ReadStatus rs = readStatusService.create(req);

        // then
        assertThat(rs).isNotNull();
        assertThat(rs.getUserId()).isEqualTo(newUser.id());
        assertThat(rs.getChannelId()).isEqualTo(channelId);
    }

    @Test
    @DisplayName("특정 유저가 속한 채널들의 ReadStatus 목록을 가져올 수 있다.")
    void findAllByUserIdTest() {
        // when: 방장(masterId)은 setUp에서 채널을 만들 때 ReadStatus가 같이 생성되었음
        List<ReadStatus> list = readStatusService.findAllByUserId(masterId);

        // then
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getChannelId()).isEqualTo(channelId);
    }

    @Test
    @DisplayName("이미 존재하는 ReadStatus를 중복으로 생성하려 하면 예외가 발생한다.")
    void createDuplicateReadStatusThrowsException() {
        // given: 이미 방장은 ReadStatus를 가지고 있음
        CreateReadStatusRequestDTO req = new CreateReadStatusRequestDTO(channelId, masterId);

        // when & then
        assertThatThrownBy(() -> readStatusService.create(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 이 채널의 읽기 상태를 가지고 있습니다");
    }

    @Test
    @DisplayName("ReadStatus를 단건 조회하고, 업데이트(시간 갱신) 및 삭제할 수 있다.")
    void findUpdateDeleteReadStatusTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("rsUser2_" + uniqueId, uniqueId + "3@test.com", "password", null));
        ReadStatus rs = readStatusService.create(new CreateReadStatusRequestDTO(channelId, user.id()));

        // when & then (Find)
        ReadStatus found = readStatusService.find(rs.getId());
        assertThat(found.getId()).isEqualTo(rs.getId());

        // when & then (Update)
        ReadStatus updated = readStatusService.update(rs.getId());
        assertThat(updated.getLastReadAt()).isAfterOrEqualTo(rs.getLastReadAt());

        // when & then (Delete)
        readStatusService.delete(rs.getId());
        assertThatThrownBy(() -> readStatusService.find(rs.getId()))
                .isInstanceOf(RuntimeException.class);
    }
}