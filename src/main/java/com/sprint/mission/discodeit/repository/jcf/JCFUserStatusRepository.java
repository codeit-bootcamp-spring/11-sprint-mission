package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JCFUserStatusRepository extends CommonJCFRepository<UserStatus> implements UserStatusRepository {
    public JCFUserStatusRepository() {
        super();
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID id) {
        List<UserStatus> userStatusList = findAll();
        for(UserStatus userStatus : userStatusList) {
            if(userStatus.getUserId().equals(id)) {
                return Optional.of(userStatus);
            }
        }
        return Optional.empty();
    }
}
