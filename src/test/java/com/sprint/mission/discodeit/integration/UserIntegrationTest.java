package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
// - MockMvc를 SpringBootTest에서 사용하고 싶을 때는 @AutoConfigureMockMvc를 사용!!
@Transactional
// - 테스트 격리를 위해 @Transactional을 붙임
public class UserIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository; // 실제 데이터를 확인해보기 위해서 가져오는 것

    @Test
    void createUser_IntegrationSuccess() throws Exception {
        // given
        UserCreateRequest requestDto = new UserCreateRequest("userA", "userA@gmail.com", "password123");

        MockMultipartFile userCreateRequest = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(requestDto)
        );

        MockMultipartFile profile = new MockMultipartFile(
                "profile",
                "profile.png",
                MediaType.IMAGE_PNG_VALUE, // VALUE는 문자열 / 없으면 객체 타입
                "dummy image byte array".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .file(userCreateRequest)
                        .file(profile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("userA"))
                .andExpect(jsonPath("$.profile.fileName").value("profile.png"));

        Optional<User> userA = userRepository.findByUsername("userA");
        assertThat(userA.get().getUsername()).isEqualTo("userA");
    }
}
