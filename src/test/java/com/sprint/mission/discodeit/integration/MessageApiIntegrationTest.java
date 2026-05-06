package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
public class MessageApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ChannelRepository channelRepo;

    @Autowired
    private UserRepository userRepo;

    @Test
    void createMessage_success() throws Exception {
        Channel channel = savePublicChannel();
        User author = saveUser();

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "content": "hello",
                  "channelId": "%s",
                  "authorId": "%s"
                }
                """.formatted(channel.getId(), author.getId()).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channel.getId().toString()));

        assertThat(messageRepo.findAll()).hasSize(1);
        assertThat(messageRepo.findAll().get(0).getContent()).isEqualTo("hello");
    }

    @Test
    void findAllByChannelId_success() throws Exception {
        Channel channel = savePublicChannel();
        User author = saveUser();

        messageRepo.save(new Message(
                "hello",
                channel,
                author,
                List.of()
        ));

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channel.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("hello"))
                .andExpect(jsonPath("$.content[0].channelId").value(channel.getId().toString()))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void updateMessage_success() throws Exception {
        Channel channel = savePublicChannel();
        User author = saveUser();

        Message message = messageRepo.save(new Message(
                "old message",
                channel,
                author,
                List.of()
        ));

        mockMvc.perform(patch("/api/messages/{messageId}", message.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newContent": "updated message"
                        }
                        """))
                .andExpect(status().isOk());

        Message updatedMessage = messageRepo.findById(message.getId()).orElseThrow();
        assertThat(updatedMessage.getContent()).isEqualTo("updated message");
    }

    @Test
    void deleteMessage_success() throws Exception {
        Channel channel = savePublicChannel();
        User author = saveUser();

        Message message = messageRepo.save(new Message(
                "hello",
                channel,
                author,
                List.of()
        ));

        mockMvc.perform(delete("/api/messages/{messageId}", message.getId()))
                .andExpect(status().isNoContent());

        assertThat(messageRepo.findById(message.getId())).isEmpty();
    }

    @Test
    void createMessage_fail_whenChannelNotFound() throws Exception {
        User author = saveUser();
        UUID unknownChannelId = UUID.randomUUID();

        MockMultipartFile messageCreateRequest = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "content": "hello",
                  "channelId": "%s",
                  "authorId": "%s"
                }
                """.formatted(unknownChannelId, author.getId()).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/messages")
                        .file(messageCreateRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"));

        assertThat(messageRepo.findAll()).isEmpty();
    }

    private Channel savePublicChannel() {
        return channelRepo.save(new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        ));
    }

    private User saveUser() {
        return userRepo.save(new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        ));
    }
}
