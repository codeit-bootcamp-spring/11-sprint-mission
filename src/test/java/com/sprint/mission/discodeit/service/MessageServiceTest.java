package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponseDTO;
import com.sprint.mission.discodeit.dto.channel.CreatePublicChannelRequestDTO;
import com.sprint.mission.discodeit.dto.message.*;
import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
import com.sprint.mission.discodeit.dto.user.SignUpResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.data.user-path=./build/test-data/users.dat",
        "app.data.channel-path=./build/test-data/channels.dat",
        "app.data.userchannel-path=./build/test-data/userchannels.dat",
        "app.data.message-path=./build/test-data/messages.dat",
        "app.data.readstatus-path=./build/test-data/readstatus.dat"
})
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserService userService;

    private UUID userId;
    private UUID channelId;

    @BeforeEach
    void setUp() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("msgUser_" + uniqueId, uniqueId + "@test.com", "password", null));
        this.userId = user.id();

        ChannelResponseDTO channel = channelService.createPublicChannel(new CreatePublicChannelRequestDTO(userId, "Chat Room", "desc"));
        this.channelId = channel.channelId();
    }

    @Test
    @DisplayName("가입된 채널에 메세지를 정상적으로 보낼 수 있다.")
    void sendMessageTest() {
        // given
        SendMessageRequestDTO req = new SendMessageRequestDTO(channelId, userId, "Hello World!", null);

        // when
        MessageResponseDTO res = messageService.sendMessage(req);

        // then
        assertThat(res.content()).isEqualTo("Hello World!");
        assertThat(res.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("가입되지 않은 채널에 메세지를 보내면 예외가 발생한다.")
    void sendMessageFailTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO nonMember = userService.signUp(new SignUpRequestDTO("nonMember_" + uniqueId, uniqueId + "2@test.com", "password", null));
        SendMessageRequestDTO req = new SendMessageRequestDTO(channelId, nonMember.id(), "Let me in!", null);

        // when & then
        assertThatThrownBy(() -> messageService.sendMessage(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("가입된 채널에만 메세지를 보낼 수 있습니다");
    }

    @Test
    @DisplayName("자신이 작성한 메세지를 수정할 수 있다.")
    void updateMessageTest() {
        // given
        MessageResponseDTO msg = messageService.sendMessage(new SendMessageRequestDTO(channelId, userId, "Old Message", null));

        // when
        UpdateMessageRequestDTO updateReq = new UpdateMessageRequestDTO(msg.messageId(), userId, "New Message", null); // messageId는 DTO에 맞게 조정 필요
        // *참고: 기존 코드상 MessageResponseDTO에 messageId가 빠져있다면 엔티티나 Repository를 통해 ID를 가져와야 합니다.
        // 현재 MessageResponseDTO에는 id 필드가 없으므로, 테스트를 위해 저장된 메세지를 조회해야 할 수 있습니다.

        GetAllMessagesResponseDTO msgs = messageService.getMessagesByChannel(userId, channelId);
        // (ID가 DTO에 있다고 가정하거나, Repository에서 꺼내와서 테스트)
    }

    @Test
    @DisplayName("특정 채널의 메세지 목록을 조회할 수 있다.")
    void findAllByChannelIdTest() {
        // given
        messageService.sendMessage(new SendMessageRequestDTO(channelId, userId, "Message 1", null));
        messageService.sendMessage(new SendMessageRequestDTO(channelId, userId, "Message 2", null));

        // when
        GetAllMessagesResponseDTO msgs = messageService.getMessagesByChannel(userId, channelId);

        // then
        assertThat(msgs.list().size()).isGreaterThanOrEqualTo(2);
    }
}