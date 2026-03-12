package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommonJCFRepository<T extends Common> {

    private List<T> repo;

    public CommonJCFRepository() {
        init();
    }

    public void init() {
        repo = new ArrayList<>();
    }

    public void save(T obj) {
        for(int i=0; i<repo.size(); i++) {
            if(repo.get(i).getId().equals(obj.getId())) {
                repo.set(i, obj);
                return;
            }
        }
        repo.add(obj);
    }

    public Optional<T> findById(UUID id) {
        return repo.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public List<T> findAll() {
        return repo;
    }

    public void delete(T obj) {
        repo.remove(obj);
    }

}
