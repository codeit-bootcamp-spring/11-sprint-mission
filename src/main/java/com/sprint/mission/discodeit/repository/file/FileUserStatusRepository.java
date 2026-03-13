package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FileUserStatusRepository extends CommonFileRepository<UserStatus> implements UserStatusRepository {
    public FileUserStatusRepository() {
        super("userstatuses", UserStatus.class);
    }
}
