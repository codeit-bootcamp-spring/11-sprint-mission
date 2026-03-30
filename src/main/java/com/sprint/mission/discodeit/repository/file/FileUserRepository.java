    package com.sprint.mission.discodeit.repository.file;

    import com.sprint.mission.discodeit.entity.User;
    import com.sprint.mission.discodeit.repository.UserRepository;
    import com.sprint.mission.discodeit.repository.base.FileRepository;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Repository;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.Optional;
    import java.util.UUID;

    @Repository
    public class FileUserRepository extends FileRepository<User> implements UserRepository {

        private final Map<String, UUID> emailIndex = new HashMap<>();
        private final Map<String, UUID> usernameIndex = new HashMap<>();

        protected FileUserRepository(@Value("${app.data.user-path}") String filePath) {
            super(filePath);
            postLoad();
        }

        @Override
        protected void postLoad() {
            for (User user : dataMap.values()) {
                addToIndex(user);
            }
        }

        @Override
        protected void postSave(User newEntity, User oldEntity) {
            if (oldEntity != null) {
                postDelete(oldEntity);
            }

            addToIndex(newEntity);
        }

        @Override
        protected void postDelete(User entity) {
            removeFromIndex(entity);
        }

        private void addToIndex(User newEntity) {
            emailIndex.put(newEntity.getEmail(), newEntity.getId());
            usernameIndex.put(newEntity.getUsername(), newEntity.getId());
        }

        private void removeFromIndex(User entity) {
            emailIndex.remove(entity.getEmail());
            usernameIndex.remove(entity.getUsername());
        }

        @Override
        public Optional<User> findByEmail(String email) {
            readLock.lock();
            try {
                UUID targetId = emailIndex.get(email);
                if (targetId == null) {
                    return Optional.empty();
                }
                return super.findById(targetId);
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public Optional<User> findByUsername(String username) {
            readLock.lock();
            try {
                UUID targetId = usernameIndex.get(username);
                if (targetId == null) {
                    return Optional.empty();
                }
                return super.findById(targetId);
            } finally {
                readLock.unlock();
            }
        }
    }
