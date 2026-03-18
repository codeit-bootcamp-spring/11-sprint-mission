package com.sprint.mission.discodeit.repository.jcf;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> data = new HashMap<>();

    @Override
    public UserStatus create(UserStatus userStatus){
        data.put(userStatus.getUserId(), userStatus);
        return userStatus;
    }

    @Override
    public UserStatus readByUserId(UUID userId){
        return data.get(userId);
    }

    @Override
    public List<UserStatus> readAll(){
        return data.values().stream().toList();

    }

    @Override
    public UserStatus update(UUID userId, Instant lastOnlineAt){
        UserStatus userStatus = data.get(userId);
        if(userStatus==null){
            throw new IllegalArgumentException("존재하지 않는 UserStatus입니다.");
        }
        userStatus.updateLastOnlineAt(lastOnlineAt);
        return userStatus;
    }

    @Override
    public void delete(UUID userId){
        data.remove(userId);
    }
}
