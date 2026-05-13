package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.controller.UserController;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistException;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

//@Transactional
@WebMvcTest(UserController.class)
//@Import(ErrorCodeStatusMapper.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Test
  @DisplayName("유저 생성 성공")
  void create_success() throws Exception {
    // given
    User user = User.create("test", "test@naver.com", "12345678");

    given(userService.create(any(), any())).willReturn(user);

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile("userCreateRequest", "", "application/json",
                """
                    {"username":"test", "email":"test@naver.com", "password":"12345678"}
                    """.getBytes())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("test"))
        .andExpect(jsonPath("$.email").value("test@naver.com"));
  }

  @Test
  @DisplayName("유저 생성 실패(Username 중복)")
  void create_fail() throws Exception {
    // given
    given(userService.create(any(), any())).willThrow(new UsernameAlreadyExistException("test"));

    mockMvc.perform(multipart("/api/users")
            .file(new MockMultipartFile("userCreateRequest", "", "application/json",
                """
                    {"username":"test", "email":"test@naver.com", "password":"12345678"}
                    """.getBytes())))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("유저 수정 성공")
  void update_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    User user = User.create("test2", "test@naver.com", "12345678");

    given(userService.update(any(), any(), any())).willReturn(user);

    MockMultipartFile request = new MockMultipartFile(
        "userUpdateRequest", "", "application/json",
        """
            
            {"username":"test2"}
            """.getBytes()
    );

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(request)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("test2"));
  }

  @Test
  @DisplayName("유저 수정 실패(유저가 없음)")
  void update_fail() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    given(userService.update(any(), any(), any())).willThrow(new UserNotFoundException(userId));

    MockMultipartFile request = new MockMultipartFile(
        "userUpdateRequest", "", "application/json",
        """
            {"username":"test2"}
            """.getBytes()
    );

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(request)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            })).andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("유저 삭제 성공")
  void delete_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("유저 삭제 실패")
  void delete_fail() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    willThrow(new UserNotFoundException(userId)).given(userService).delete(userId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId))
        .andExpect(status().isNotFound());
  }
}
