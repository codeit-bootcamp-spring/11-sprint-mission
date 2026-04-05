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
    private final Map<String, UUID> emailToId;

    public JCFUserRepository() {
        super();
        usernameToId = new ConcurrentHashMap<>();
        emailToId = new ConcurrentHashMap<>();
    }

    @Override
    public void save(User obj) {
        Optional<User> oldUserOpt = findById(obj.getId());

        if(oldUserOpt.isPresent()) {
            User oldUser = oldUserOpt.get();
            if(!oldUser.getUsername().equals(obj.getUsername())) {
                usernameToId.remove(oldUser.getUsername());
            }
            if(!oldUser.getEmail().equals(obj.getEmail())) {
                emailToId.remove(oldUser.getEmail());
            }
        }

        super.save(obj);
        usernameToId.put(obj.getUsername(), obj.getId());
        emailToId.put(obj.getEmail(), obj.getId());
    }

    @Override
    public boolean deleteById(UUID id) {
        Optional<User> userOpt = findById(id);

        if(userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        usernameToId.remove(user.getUsername());
        emailToId.remove(user.getEmail());

        return super.deleteById(id);
    }

    @Override
    public Optional<User> findByName(String username) {
        return findById(usernameToId.get(username));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findById(emailToId.get(email));
    }

}
