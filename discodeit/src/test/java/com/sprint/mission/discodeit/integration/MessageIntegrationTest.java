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
import com.sprint.mission.discodeit.service.dto.message.CreateMessageRequest;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import java.util.List;
import java.util.UUID;
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
class MessageIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private UUID authorId;
    private UUID channelId;

    @BeforeEach
    void setUp() throws Exception {
        // 사용자 생성
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username("msguser")
                .email("msguser@example.com")
                .password("password1234")
                .build();
        MockMultipartFile userPart = new MockMultipartFile(
                "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(userRequest)
        );
        MvcResult userResult = mockMvc.perform(multipart("/api/users").file(userPart))
                .andExpect(status().isCreated())
                .andReturn();
        authorId = UUID.fromString(JsonPath.parse(userResult.getResponse().getContentAsString()).read("$.id"));

        // 채널 생성
        CreatePublicChannelRequest channelRequest = new CreatePublicChannelRequest("메시지채널", null);
        MvcResult channelResult = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(channelRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        channelId = UUID.fromString(JsonPath.parse(channelResult.getResponse().getContentAsString()).read("$.id"));
    }

    private MockMultipartFile messagePart(String content) throws Exception {
        CreateMessageRequest request = new CreateMessageRequest(authorId, channelId, content, List.of());
        return new MockMultipartFile(
                "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private String createMessage(String content) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/messages").file(messagePart(content)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");
    }

    @Test
    void 메시지_생성_성공_201() throws Exception {
        mockMvc.perform(multipart("/api/messages").file(messagePart("안녕하세요")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("안녕하세요"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void 메시지_생성_내용없음_400() throws Exception {
        CreateMessageRequest request = new CreateMessageRequest(authorId, channelId, "", List.of());
        MockMultipartFile part = new MockMultipartFile(
                "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/messages").file(part))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void 메시지_목록_조회_성공_200() throws Exception {
        createMessage("첫 번째 메시지");
        createMessage("두 번째 메시지");

        mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 메시지_수정_성공_200() throws Exception {
        String messageId = createMessage("원본 내용");

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newContent\":\"수정된 내용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    void 메시지_수정_없는메시지_404() throws Exception {
        mockMvc.perform(patch("/api/messages/{messageId}", "00000000-0000-0000-0000-000000000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newContent\":\"수정 시도\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }

    @Test
    void 메시지_삭제_성공_204() throws Exception {
        String messageId = createMessage("삭제할 메시지");

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }
}
