package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChannelApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChannelRepository channelRepo;

    @Autowired
    private UserRepository userRepo;

    @Test
    void createPublicChannel_success() throws Exception {
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "general",
                          "description": "general channel"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("general channel"));

        assertThat(channelRepo.findAllByChannelType(ChannelType.PUBLIC))
                .hasSize(1);
    }

    @Test
    void createPrivateChannel_success() throws Exception {
        User user1 = userRepo.save(new User(
                "user1",
                "user1@test.com",
                "password",
                null
        ));

        User user2 = userRepo.save(new User(
                "user2",
                "user2@test.com",
                "password",
                null
        ));

        mockMvc.perform(post("/api/channels/private")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "participantIds": ["%s", "%s"]
                        }
                        """.formatted(user1.getId(), user2.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("PRIVATE"));

        assertThat(channelRepo.findAllByChannelType(ChannelType.PRIVATE))
                .hasSize(1);
    }

    @Test
    void updateChannel_success() throws Exception {
        Channel channel = channelRepo.save(new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        ));

        mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "notice",
                          "newDescription": "notice channel"
                        }
                        """))
                .andExpect(status().isOk());

        Channel updatedChannel = channelRepo.findById(channel.getId()).orElseThrow();

        assertThat(updatedChannel.getName()).isEqualTo("notice");
        assertThat(updatedChannel.getDescription()).isEqualTo("notice channel");
    }

    @Test
    void deleteChannel_success() throws Exception {
        Channel channel = channelRepo.save(new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        ));

        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId()))
                .andExpect(status().isNoContent());

        assertThat(channelRepo.findById(channel.getId())).isEmpty();
    }

    @Test
    void updateChannel_fail_whenChannelNotFound() throws Exception {
        mockMvc.perform(patch("/api/channels/{channelId}", java.util.UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "notice",
                          "newDescription": "notice channel"
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.exceptionType").value("ChannelNotFoundException"));
    }
}
