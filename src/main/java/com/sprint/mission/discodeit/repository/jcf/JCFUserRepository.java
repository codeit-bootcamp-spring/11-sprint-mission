package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.dto.userdto.CreateUserDto;
import com.sprint.mission.discodeit.dto.userdto.UpdateUserDto;
import com.sprint.mission.discodeit.dto.userdto.UserInfoDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFUserRepository implements UserRepository {


    private final Map<UUID, User> data;

    public JCFUserRepository() {
        data = new HashMap<>();
    }

    @Override
    public boolean saveUser(User user) {
        data.put(user.getId(), user);
        return true;
    }

    @Override
    public Optional<User> getUser(UUID userId) {
        return data.containsKey(userId) ? Optional.of(data.get(userId)) : Optional.empty();
    }

    @Override
    public List<User> getAllUser() {
        return data.values().stream().toList();
    }

    @Override
    public Optional<User> getUserByNickname(String nickname) {
        return data.values().stream().filter(user -> user.getNickname().equals(nickname)).findFirst();
    }


    @Override
    public boolean deleteUser(UUID userId) {
        return data.remove(userId) != null;
    }

    @Override
    public boolean isExistUserByNickname(String nickname) {
        return data.values().stream().anyMatch(user -> user.getNickname().equals(nickname));
    }

    @Override
    public boolean isExistUserByEmail(String Email) {
        return data.values().stream().anyMatch(user -> user.getEmail().equals(Email));
    }

    @Override
    public boolean isExistUser(UUID userId) {
        return data.containsKey(userId);
    }
}
