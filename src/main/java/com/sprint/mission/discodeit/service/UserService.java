package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.*;

import java.util.UUID;

public interface UserService {

    SignUpResponseDTO signUp(SignUpRequestDTO dto);

    FindUserByIdResponseDTO findUser(UUID userId);

    FindAllUserResponseDTO findAllUser();

    // 아하 지금 유저 정보 자체를 업데이트 하도록 수정해야함
    // - 현재는 프로필만 수정하도록 되어있음
    UpdateUserInfoResponseDTO updateUserInfo(UpdateUserInfoRequestDTO dto);

    void deleteUser(UUID userId);
}
