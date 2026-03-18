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
            postLoad(); // 이거 추후 어떻게 바꿀 수 있을 것 같은데 고민 중 (Lazy Loading?) / 현재 인덱스 생성 관련 문제있음을 인지
        }

        @Override
        protected void postLoad() {
            for (User user : dataMap.values()) {
                emailIndex.put(user.getEmail(), user.getId());
                usernameIndex.put(user.getUsername(), user.getId());
            }
        }

        @Override
        protected void postSave(User newEntity, User oldEntity) {
            if (oldEntity != null) { // 기존 인덱스 삭제
                postDelete(oldEntity);
            }

            emailIndex.put(newEntity.getEmail(), newEntity.getId());
            usernameIndex.put(newEntity.getUsername(), newEntity.getId());
        }

        @Override
        protected void postDelete(User entity) {
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

    /*
    > 발견한 문제점: update 시 인덱스 갱신의 문제
    - old -> new
    - new로 갱신되서 인덱스에 추가 생성되는 것은 확인 (앞 단에서 email 중복을 막고 있어서 중복은 걱정하지 않아도 되는 문제)
    - 단, old가 현재 인덱스에서 남아있게 됨
     */