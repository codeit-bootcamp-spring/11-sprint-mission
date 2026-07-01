//package com.sprint.mission.discodeit.service;
//
//import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
//import com.sprint.mission.discodeit.dto.user.SignUpResponseDTO;
//import com.sprint.mission.discodeit.dto.userstatus.UpdateUserStatusRequestDTO;
//import com.sprint.mission.discodeit.entity.UserStatus;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.Instant;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@ActiveProfiles("test")
//@SpringBootTest(properties = {
//        "app.data.user-path=./build/test-data/users.dat",
//        "app.data.userstatus-path=./build/test-data/userstatus.dat"
//})
//class UserStatusServiceTest {
//
//    @Autowired private UserStatusService userStatusService;
//    @Autowired private UserService userService;
//
//    @Test
//    @DisplayName("유저 접속 시 상태 정보(마지막 접속 시간)가 갱신되어야 한다.")
//    void updateByUserIdTest() {
//        // given
//        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
//        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("usUser_" + uniqueId, uniqueId + "@test.com", "password", null));
//
//        // when
//        UserStatus updatedStatus = userStatusService.updateByUserId(user.id());
//
//        // then
//        assertThat(updatedStatus.getUserId()).isEqualTo(user.id());
//        assertThat(updatedStatus.getLastOnlineTime()).isNotNull();
//        // 갱신된 시간이 현재 시간(Instant.now())과 유사한지 확인
//        assertThat(updatedStatus.getLastOnlineTime()).isAfter(Instant.now().minusSeconds(10));
//    }
//
//    @Test
//    @DisplayName("수동으로 UserStatusType을 업데이트 할 수 있다.")
//    void updateUserStatusTypeTest() {
//        // given
//        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
//        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("usUser2_" + uniqueId, uniqueId + "2@test.com", "password", null));
//
//        // (userService.signUp 내부에서 UserStatus가 생성됨을 가정)
//        UUID statusId = userStatusService.findAll().stream()
//                .filter(s -> s.getUserId().equals(user.id()))
//                .findFirst().orElseThrow().getId();
//
//        UpdateUserStatusRequestDTO updateReq = new UpdateUserStatusRequestDTO(statusId);
//
//        // when
//        UserStatus updated = userStatusService.update(updateReq);
//
//        // then
//        // update 메서드가 lastOnlineTime을 갱신하는 요구사항인지 확인 (BasicUserStatusService.update 로직 기준)
//        assertThat(updated.getLastOnlineTime()).isNotNull();
//    }
//}