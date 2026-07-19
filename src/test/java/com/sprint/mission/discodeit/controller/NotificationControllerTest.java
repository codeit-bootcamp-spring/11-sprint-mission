package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = NotificationController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = ".*\\.security\\.jwt\\..*"))
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  @DisplayName("알림 목록 조회 성공 테스트")
  void findAllByReceiverId_Success() throws Exception {
    // Given
    UUID receiverId = UUID.randomUUID();
    UserDto currentUser = new UserDto(receiverId, "testuser", "test@example.com", null, true,
        Role.USER);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(currentUser, "password");

    List<NotificationDto> notifications = List.of(
        new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "제목1", "내용1"),
        new NotificationDto(UUID.randomUUID(), Instant.now(), receiverId, "제목2", "내용2")
    );

    given(notificationService.findAllByReceiverId(eq(receiverId))).willReturn(notifications);

    // When & Then
    mockMvc.perform(get("/api/notifications")
            .contentType(MediaType.APPLICATION_JSON)
            .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].title").value("제목1"))
        .andExpect(jsonPath("$[1].title").value("제목2"));
  }

  @Test
  @DisplayName("알림 확인(삭제) 성공 테스트")
  void delete_Success() throws Exception {
    // Given
    UUID notificationId = UUID.randomUUID();
    willDoNothing().given(notificationService).delete(notificationId);

    // When & Then
    mockMvc.perform(delete("/api/notifications/{notificationId}", notificationId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("알림 확인(삭제) 실패 테스트 - 존재하지 않는 알림")
  void delete_Failure_NotificationNotFound() throws Exception {
    // Given
    UUID nonExistentId = UUID.randomUUID();
    willThrow(NotificationNotFoundException.withId(nonExistentId))
        .given(notificationService).delete(nonExistentId);

    // When & Then
    mockMvc.perform(delete("/api/notifications/{notificationId}", nonExistentId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}
