package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    //create, read, readAll, update, delete
    UUID create(UserCreateRequest request); // 유저 생성
    UserResponse read(UUID id); // 유저 ID입력 -> 유저 객체 반환
    List<UserResponse> readAll(); // 모든 유저 객체 리스트로 반환
    void update(UUID userId, UserUpdateRequest request); // 유저 객체 -> 기존 유저 덮어쓰기
    void delete(UUID id); // 유저 ID -> 삭제

    void setChannelService(ChannelService channelService);
    void setMessageService(MessageService messageService);

}
