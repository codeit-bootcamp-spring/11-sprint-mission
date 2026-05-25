package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    MessageRepository messageRepository;

    @Test
    @DisplayName("메시지를 생성하고 목록에서 조회할 수 있다")
    void message_createAndFindAll_success() throws Exception {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "authorId": "%s",
                  "channelId": "%s",
                  "content": "hello"
                }
                """.formatted(author.getId(), channel.getId()).getBytes(StandardCharsets.UTF_8)
        );

        // when & then - create
        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channel.getId().toString()))
                .andExpect(jsonPath("$.author.username").value("evan"));

        // when & then - findAllByChannelId
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channel.getId().toString())
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].channelId").value(channel.getId().toString()));
    }

    @Test
    @DisplayName("메시지를 수정하고 삭제할 수 있다")
    void message_updateAndDelete_success() throws Exception {
        // given
        User author = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        Message message = messageRepository.saveAndFlush(
                new Message(author, channel, "old message")
        );

        // when & then - update
        mockMvc.perform(patch("/api/messages/{messageId}", message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": "updated message"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(message.getId().toString()))
                .andExpect(jsonPath("$.content").value("updated message"));

        // when & then - delete
        mockMvc.perform(delete("/api/messages/{messageId}", message.getId()))
                .andExpect(status().isNoContent());
    }
}