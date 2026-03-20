package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.baseentity.Common;

import java.util.*;
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

    public void delete(T obj) {
        repo.remove(obj.getId());
    }

}
