package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusOfUserNotFoundException;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file"
)
public class FileUserStatusRepository extends CommonFileRepository<UserStatus> implements UserStatusRepository {
    public FileUserStatusRepository(
            @Value("${discodeit.repository.file.base-dir}") String basedir
    ) {
        super("userstatuses", UserStatus.class, basedir);
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
