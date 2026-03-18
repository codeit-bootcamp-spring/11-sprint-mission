package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.LoginRequestDTO;
import com.sprint.mission.discodeit.dto.auth.LoginResponseDTO;
import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
import com.sprint.mission.discodeit.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "app.data.user-path=./build/test-data/users.dat",
        "app.data.userstatus-path=./build/test-data/userstatus.dat"
})
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("올바른 정보로 로그인 시 성공하고 상태가 ONLINE으로 변경된다.")
    void loginSuccessTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String username = "loginUser_" + uniqueId;
        String password = "password123";
        userService.signUp(new SignUpRequestDTO(username, uniqueId + "@test.com", password, null));

        // when
        LoginRequestDTO loginReq = new LoginRequestDTO(username, password);
        LoginResponseDTO loginRes = authService.login(loginReq);

        // then
        assertThat(loginRes.username()).isEqualTo(username);
        assertThat(loginRes.status()).isEqualTo("ONLINE");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다.")
    void loginFailTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String username = "failUser_" + uniqueId;
        userService.signUp(new SignUpRequestDTO(username, uniqueId + "@test.com", "password123", null));

        // when & then
        LoginRequestDTO loginReq = new LoginRequestDTO(username, "wrongPassword");
        assertThatThrownBy(() -> authService.login(loginReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");
    }
}