package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Role;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @DisplayName("알림 목록 조회 성공 - 200 응답")
  void getNotifications_success() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto(userId, "tester", "test@test.com", null, false, Role.USER);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, "password");

    NotificationDto notificationDto = new NotificationDto(
        UUID.randomUUID(), Instant.now(), userId, "제목", "내용"
    );

    given(notificationService.findAllByReceiverId(userId)).willReturn(List.of(notificationDto));

    mockMvc.perform(get("/api/notifications")
            .with(user(userDetails))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].title").value("제목"));
  }

  @Test
  @DisplayName("알림 삭제 성공 - 204 응답")
  void deleteNotification_success() throws Exception {
    UUID notificationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto(userId, "tester", "test@test.com", null, false, Role.USER);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, "password");

    willDoNothing().given(notificationService).delete(notificationId);

    mockMvc.perform(
            delete("/api/notifications/{notificationId}", notificationId)
                .with(user(userDetails))
                .with(csrf())
        )
        .andExpect(status().isNoContent());
  }
}