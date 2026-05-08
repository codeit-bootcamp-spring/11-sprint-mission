package com.sprint.mission.discodeit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
// - ControllerAdvice가 등록이 안되는 경우도 있음 (기본적으로 가져옴 / 위에 지정한 것은 특정 컨트롤러(@Controller / @RestController는 지정한 것만 가져옴)
// - json 테스트 시 주의할 점: json은 전부 다 문자열로 취급함!!
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    UserService userService;
    @MockitoBean
    UserStatusService userStatusService;

    @Test
    @DisplayName("성공: 프로필 사진과 가입 정보를 함께 보내면 파일이 Optional에 담겨 Service로 전달된다.")
    void createUser_WithProfile_Success() throws Exception {
        // given
        UserCreateRequest userCreateRequest = new UserCreateRequest("tester", "test@email.com", "password123");
        UserDto userDto = new UserDto(UUID.randomUUID(), "tester", "test@email.com", null, true);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "userCreateRequest",    // 파라미터 이름 (Controller의 @RequestPart 속성과 일치해야 함
                        "",                   // 원래 파일명 (JSON은 의미 없음)
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(userCreateRequest)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "profile",
                "avatar.png",      // 클라이언트가 올린 원본 파일명
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes() // 가짜 파일 데이터
        );

        given(userService.create(any(), any())).willReturn(userDto);

        // when & then
        mockMvc.perform(multipart("/api/users")
                .file(jsonPart)
                .file(filePart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Optional<BinaryContentCreateRequest>> profileCaptor =
                ArgumentCaptor.forClass(Optional.class);

        verify(userService).create(any(UserCreateRequest.class), profileCaptor.capture());

        Optional<BinaryContentCreateRequest> capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile).isPresent();
        assertThat(capturedProfile.get().fileName()).isEqualTo("avatar.png");
        assertThat(capturedProfile.get().contentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
    }

    @Test
    @DisplayName("성공: 프로필 사진 없이 가입 정보만 보내면 Optional.empty()가 Service로 전달된다")
    void createUser_WithoutProfile_Success() throws Exception {
        // given
        UserCreateRequest requestDto = new UserCreateRequest("tester", "test@email.com", "password123");
        MockMultipartFile jsonPart = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(requestDto)
        );

        // 파일은 전송하지 않음
        UserDto userDto = mock(UserDto.class); // 가짜 객체 생성
        given(userService.create(any(), any())).willReturn(userDto);

        // when & then
        mockMvc.perform(multipart("/api/users")
                        .file(jsonPart)) // filePart 생략
                .andDo(print())
                .andExpect(status().isCreated());

        // 컨트롤러 내부 로직에 의해 Optional.empty()가 넘어갔는지 확인
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Optional<BinaryContentCreateRequest>> profileCaptor = ArgumentCaptor.forClass(Optional.class);

        verify(userService).create(any(UserCreateRequest.class), profileCaptor.capture());
        assertThat(profileCaptor.getValue()).isEmpty();
    }

    @Test
    void update_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest requestDto = new UserUpdateRequest(
                "updatedUser", "updatedUser@gmail.com", "updatedPassword");
        MockMultipartFile userUpdateRequest = new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE, // VALUE는 문자열 / 없으면 객체 타입
                objectMapper.writeValueAsBytes(requestDto) // 객체를 JSON Byte 배열로 변환
        );

        MockMultipartFile profile = new MockMultipartFile(
                "profile", // @RequestPart("profile")와 이름 동일
                "profile.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image byte array".getBytes()
        );

        UserDto responseDto = new UserDto(
                userId, "updatedUser", "updatedUser@gmail.com", null, true);
        given(userService.update(any(), any(), any())).willReturn(responseDto);

        // when & then
        mockMvc.perform(multipart("/api/users/{userId}", userId.toString())
                        .file(userUpdateRequest)
                        .file(profile)
                        .with(request -> { // multipart는 post 요청이므로 patch로 변경해서 던져줘야함
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString())) // JSON으로 파싱된 UUID는 String임
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.email").value("updatedUser@gmail.com"))
                .andExpect(jsonPath("$.profile").isEmpty()) // null인 경우
                .andExpect(jsonPath("$.online").value(true));
    }

    @Test
    void delete_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void findAll_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDto user = new UserDto(userId, "userA", "userA@gmail.com", null, true);
        List<UserDto> users = new ArrayList<>();
        users.add(user);

        given(userService.findAll()).willReturn(users);

        // when & then
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("[0].username").value("userA"));
    }

    @Test
    void updateUserStatusByUserId_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID userStatusId = UUID.randomUUID();
        Instant timestamp = Instant.now();
        UserStatusUpdateRequest userStatusUpdateRequest = new UserStatusUpdateRequest(timestamp);
        UserStatusDto responseDto = new UserStatusDto(userStatusId, userId, timestamp);
        given(userStatusService.updateByUserId(userId, userStatusUpdateRequest)).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/users/{userId}/userStatus", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(userStatusUpdateRequest))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userStatusId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.lastActiveAt").value(timestamp.toString()));
    }
}