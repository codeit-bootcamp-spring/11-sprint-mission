package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("PUBLIC 채널을 생성하고 수정할 수 있다")
    void channel_createAndUpdate_success() throws Exception {
        // when & then - create
        String createResponse = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "general",
                          "description": "general channel"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("general"))
                .andExpect(jsonPath("$.description").value("general channel"))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(createResponse);
        String channelId = jsonNode.get("id").asText();

        // when & then - update
        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "newName": "notice",
                          "newDescription": "notice channel"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId))
                .andExpect(jsonPath("$.name").value("notice"))
                .andExpect(jsonPath("$.description").value("notice channel"));
    }

    @Test
    @DisplayName("채널을 삭제할 수 있다")
    void channel_delete_success() throws Exception {
        // given
        Channel channel = channelRepository.saveAndFlush(
                new Channel("general", "general channel")
        );

        // when & then
        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId()))
                .andExpect(status().isNoContent());
    }
}