package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.baseentity.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommonJCFRepository<T extends Common> {

    private Map<UUID, T> repo;

    public CommonJCFRepository() {
        init();
    }

    private void init() {
        repo = new ConcurrentHashMap<>();
    }

    public void save(T obj) {
        repo.put(obj.getId(), obj);
    }

    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(repo.get(id));
    }

    public List<T> findAll() {
        return new ArrayList<>(repo.values());
    }

    public boolean deleteById(UUID id) {
        return repo.remove(id) != null;
    }

}
