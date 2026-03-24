package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusRepository {


    boolean saveUserStatus(UserStatus userStatus);
    Optional<UserStatus> getUserStatus(UUID userId);
    List<UserStatus> getAllUserStatus();
    boolean deleteUserStatus(UUID userId);
    boolean isExistUserStatus(UUID userId);





}
