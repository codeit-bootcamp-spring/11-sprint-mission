package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFUserRepository extends CommonJCFRepository<User> implements UserRepository {

    private final Map<String, UUID> usernameToId;

    public JCFUserRepository() {
        super();
        usernameToId = new ConcurrentHashMap<>();
    }

    @Override
    public void save(User obj) {
        Optional<User> oldUserOpt = findById(obj.getId());

        if(oldUserOpt.isPresent()) {
            User oldUser = oldUserOpt.get();
            if(!oldUser.getUsername().equals(obj.getUsername())) {
                usernameToId.remove(oldUser.getUsername());
            }
        }

        super.save(obj);
        usernameToId.put(obj.getUsername(), obj.getId());
    }

    @Override
    public void delete(User obj) {
        super.delete(obj);
        usernameToId.remove(obj.getUsername());
    }

    @Override
    public boolean existsByName(String name) {
        return usernameToId.containsKey(name);
    }

    @Override
    public boolean existsByEmail(String email) {
        List<User> userList = findAll();
        for(User user : userList) {
            if(user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<User> findByName(String username) {
        return findById(usernameToId.get(username));
    }

}
