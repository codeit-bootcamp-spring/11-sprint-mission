package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.baseentity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommonJCFRepository<T extends BaseEntity> {

    private Map<UUID, T> repo;

    public CommonJCFRepository() {
        init();
    }

    private void init() {
        repo = new ConcurrentHashMap<>();
    }

    public T save(T obj) {
        repo.put(obj.getId(), obj);
        return obj;
    }

    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(repo.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(repo.values());
    }

    public void deleteById(UUID id) {
        repo.remove(id);
    }

}
