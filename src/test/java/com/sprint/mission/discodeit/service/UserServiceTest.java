package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.repository.UserRepository;
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
        "app.data.userstatus-path=./build/test-data/userstatus.dat",
        "app.data.binarycontent-path=./build/test-data/binarycontent.dat"
})
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입이 정상적으로 완료되어야 한다.")
    void signUpTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpRequestDTO req = new SignUpRequestDTO("user_" + uniqueId, uniqueId + "@test.com", "password", null);

        // when
        SignUpResponseDTO res = userService.signUp(req);

        // then
        assertThat(res.id()).isNotNull();
        assertThat(res.username()).isEqualTo(req.username());
        assertThat(res.status()).isEqualTo("OFFLINE");
    }

    @Test
    @DisplayName("중복된 이메일로 가입 시 BusinessException이 발생해야 한다.")
    void signUpDuplicateEmailTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpRequestDTO req1 = new SignUpRequestDTO("user_" + uniqueId, uniqueId + "@test.com", "password", null);
        userService.signUp(req1);

        SignUpRequestDTO req2 = new SignUpRequestDTO("user2_" + uniqueId, uniqueId + "@test.com", "password", null);

        // when & then
        assertThatThrownBy(() -> userService.signUp(req2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 이메일은 이미 사용중입니다");
    }

    @Test
    @DisplayName("유저 정보를 수정할 수 있어야 한다.")
    void updateUserInfoTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("user_" + uniqueId, uniqueId + "@test.com", "password", null));

        UpdateUserInfoRequestDTO updateReq = new UpdateUserInfoRequestDTO(user.id(), "new_" + uniqueId, "new_" + uniqueId + "@test.com", "new_password", null);

        // when
        UpdateUserInfoResponseDTO updateRes = userService.updateUserInfo(updateReq);

        // then
        assertThat(updateRes.username()).isEqualTo(updateReq.username());
        assertThat(updateRes.email()).isEqualTo(updateReq.email());
    }

    @Test
    @DisplayName("회원 탈퇴 시 유저 정보가 삭제되어야 한다.")
    void deleteUserTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("user_" + uniqueId, uniqueId + "@test.com", "password", null));

        // when
        userService.deleteUser(user.id());

        // then
        assertThatThrownBy(() -> userService.findUser(user.id()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("중복된 유저명(username)으로 가입 시 BusinessException이 발생해야 한다.")
    void signUpDuplicateUsernameTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        userService.signUp(new SignUpRequestDTO("sameName", uniqueId + "1@test.com", "password", null));

        // when & then
        assertThatThrownBy(() -> userService.signUp(new SignUpRequestDTO("sameName", uniqueId + "2@test.com", "password", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("해당 유저명은 이미 사용중입니다");
    }

    @Test
    @DisplayName("단건 및 다건 유저 조회 시 패스워드는 없고 상태(status) 정보가 포함되어야 한다.")
    void findAndFindAllUserTest() {
        // given
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpResponseDTO user = userService.signUp(new SignUpRequestDTO("user_" + uniqueId, uniqueId + "@test.com", "password", null));

        // when (단건 조회)
        FindUserByIdResponseDTO foundUser = userService.findUser(user.id());
        // then
        assertThat(foundUser.status()).isNotNull(); // 상태 정보 포함 확인 (패스워드는 DTO에 아예 없음)

        // when (다건 조회)
        FindAllUserResponseDTO allUsers = userService.findAllUser();
        // then
        assertThat(allUsers.userList()).isNotEmpty();
        assertThat(allUsers.userList().stream().anyMatch(u -> u.id().equals(user.id()))).isTrue();
    }
}