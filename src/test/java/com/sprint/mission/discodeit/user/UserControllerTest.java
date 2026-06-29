package com.sprint.mission.discodeit.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.UserController;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 💡 최신 버전용 import
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Test
  @DisplayName("사용자 생성 성공 - 201 Created 응답 반환")
  void create_success() throws Exception {
    //given
    UserCreateRequest request = new UserCreateRequest("이마크", "mark@test.com", "pass1234");
    UserDto userDto = new UserDto(UUID.randomUUID(), "이마크", "mark@test.com", null, null, null);

    given(userService.create(any(UserCreateRequest.class), any())).willReturn(
        userDto);

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest", // 컨트롤러의 @RequestPart("userCreateRequest") 이름과 정확히 일치해야 함
        "", // 원본 파일명 (JSON 객체 전달이므로 비워둠)
        MediaType.APPLICATION_JSON_VALUE, // 이 파트의 타입은 JSON이라고 명시
        objectMapper.writeValueAsBytes(request) // MockMultipartFile의 요청에 따라 객체를 바이트 배열로 변환해서 삽입
    );

    //when & then
    // 컨트롤러가 @RequestPart를 사용하고 있기 때문에, 테스트에서도 JSON이 아니라 MockMultipartFile이라는 가짜 파일 껍데기에 JSON을 담아서 보내야함
    mockMvc.perform(
            multipart("/api/users")
                .file(userCreateRequestPart) // 만들어둔 MockMultipartFile 첨부
                .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("사용자 생성 실패 - 이메일 형식 오류 시 400 Bad Request 응답")
  void create_fail_invalid_email() throws Exception {
    //given
    UserCreateRequest request = new UserCreateRequest("이마크", "mark", "pass1234");

    MockMultipartFile userCreateRequestPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    //when & then
    mockMvc.perform(
            multipart("/api/users")
                .file(userCreateRequestPart)
        )
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}
