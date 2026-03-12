package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommonJCFRepository<T extends Common> {

    private List<T> repo;
    private Class<T> type;

    public CommonJCFRepository(Class<T> type) {
        this.type = type;
        init();
    }

    public void init() {
        repo = new ArrayList<>();
    }

    public void save(T obj) {
        repo.add(obj);
    }

    public T findById(UUID id) {
        List<T> list;
        list = repo.stream()
                .filter(p -> p.getId().equals(id))
                .toList();
        if(list.isEmpty()) {
            throw new IllegalArgumentException(type.getSimpleName() + " Not Found: " + id);
        }
        return list.get(0);
    }

    public List<T> findAll() {
        return repo;
    }

    public void delete(T obj) {
        repo.remove(obj);
    }

}
