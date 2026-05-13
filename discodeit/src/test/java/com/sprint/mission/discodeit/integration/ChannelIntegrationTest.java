package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.sprint.mission.discodeit.service.dto.channel.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String userId;

    @BeforeEach
    void setUp() throws Exception {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username("channeluser")
                .email("channeluser@example.com")
                .password("password1234")
                .build();
        MockMultipartFile userPart = new MockMultipartFile(
                "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(userRequest)
        );
        MvcResult result = mockMvc.perform(multipart("/api/users").file(userPart))
                .andExpect(status().isCreated())
                .andReturn();
        userId = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");
    }

    private String createPublicChannel(String name, String description) throws Exception {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest(name, description);
        MvcResult result = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");
    }

    @Test
    void 공개채널_생성_성공_201() throws Exception {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("일반", "일반 채널");

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("일반"))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void 공개채널_생성_이름없음_400() throws Exception {
        CreatePublicChannelRequest request = new CreatePublicChannelRequest("", "설명");

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 채널_수정_성공_200() throws Exception {
        String channelId = createPublicChannel("수정전", "기존 설명");

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newName\":\"수정후\",\"newDescription\":\"새 설명\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정후"))
                .andExpect(jsonPath("$.description").value("새 설명"));
    }

    @Test
    void 채널_수정_없는채널_404() throws Exception {
        mockMvc.perform(patch("/api/channels/{channelId}", "00000000-0000-0000-0000-000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newName\":\"수정\",\"newDescription\":null}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }

    @Test
    void 채널_삭제_성공_204() throws Exception {
        String channelId = createPublicChannel("삭제할채널", null);

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());
    }

    @Test
    void 사용자별_채널_목록_조회_200() throws Exception {
        createPublicChannel("조회채널1", null);
        createPublicChannel("조회채널2", null);

        mockMvc.perform(get("/api/channels").param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
